package rip.alpha.hcf;

import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import net.mcscrims.basic.Basic;
import net.mcscrims.configuration.Configurations;
import net.mcscrims.libraries.Libraries;
import net.mcscrims.libraries.board.BoardHandler;
import net.mcscrims.libraries.mongo.MongoHelper;
import net.mcscrims.libraries.nametag.NametagHandler;
import net.mcscrims.libraries.tablist.TabListHandler;
import net.mcscrims.libraries.util.listeners.ClassUtils;
import net.mcscrims.libraries.util.listeners.ListenerHandler;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.github.paperspigot.PaperSpigotWorldConfig;
import org.spigotmc.SpigotWorldConfig;
import rip.alpha.hcf.adapter.BoardAdapter;
import rip.alpha.hcf.adapter.NametagAdapter;
import rip.alpha.hcf.adapter.TabListAdapter;
import rip.alpha.hcf.border.BorderHandler;
import rip.alpha.hcf.combatlogger.CombatLoggerHandler;
import rip.alpha.hcf.crates.CrateHandler;
import rip.alpha.hcf.crowbar.CrowbarHandler;
import rip.alpha.hcf.deathban.DeathbanHandler;
import rip.alpha.hcf.effect.PlayerEffectHandler;
import rip.alpha.hcf.elevator.ElevatorHandler;
import rip.alpha.hcf.enchantment.EnchantmentHandler;
import rip.alpha.hcf.end.EndHandler;
import rip.alpha.hcf.game.GameHandler;
import rip.alpha.hcf.glowstone.GlowstoneHandler;
import rip.alpha.hcf.handler.SoundPacketHandler;
import rip.alpha.hcf.items.CItemHandler;
import rip.alpha.hcf.kitmap.KitmapHandler;
import rip.alpha.hcf.leaderboard.LeaderboardHandler;
import rip.alpha.hcf.mobstack.MobstackHandler;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.TeamProfileHandler;
import rip.alpha.hcf.pvpclass.PvPClass;
import rip.alpha.hcf.pvpclass.PvPClassHandler;
import rip.alpha.hcf.pvpclass.kits.KitHandler;
import rip.alpha.hcf.reclaim.ReclaimHandler;
import rip.alpha.hcf.shop.ShopHandler;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.TeamPlayerCache;
import rip.alpha.hcf.team.grid.ClaimGrid;
import rip.alpha.hcf.timer.TimerHandler;
import rip.alpha.hcf.visual.VisualHandler;
import rip.alpha.modsuite.ModSuitePlugin;
import rip.foxtrot.spigot.fSpigot;

import javax.security.auth.login.Configuration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Getter
public class HCF extends JavaPlugin {

    public static void log(Consumer<Logger> loggerConsumer) {
        loggerConsumer.accept(instance.getLogger());
    }

    @Getter
    private static HCF instance;
    public static final JsonParser PARSER = new JsonParser();

    private HCFConfiguration configuration;
    private MongoHelper mongoHelper;
    private ScheduledExecutorService scheduledExecutorService;

    private ClaimGrid claimGrid;
    private TeamPlayerCache teamPlayerCache;
    private TeamHandler teamHandler;
    private TeamProfileHandler profileHandler;
    private VisualHandler visualHandler;
    private TimerHandler timerHandler;
    private PlayerEffectHandler playerEffectHandler;
    private PvPClassHandler pvPClassHandler;
    private BorderHandler borderHandler;
    private MobstackHandler mobstackHandler;
    private DeathbanHandler deathbanHandler;
    private CombatLoggerHandler combatLoggerHandler;
    private GlowstoneHandler glowstoneHandler;
    private GameHandler gameHandler;
    private EnchantmentHandler enchantmentHandler;
    private CrowbarHandler crowbarHandler;
    private KitHandler kitHandler;
    private ShopHandler shopHandler;
    private CrateHandler crateHandler;
    private ElevatorHandler elevatorHandler;
    private EndHandler endHandler;
    private LeaderboardHandler leaderboardHandler;
    private ReclaimHandler reclaimHandler;
    private CItemHandler itemHandler;

    @Getter @Setter private boolean eotw = false;

    @Override
    public void onEnable() {
        instance = this;

        this.configuration = Configurations.computeIfAbsent(new HCFConfiguration());
        this.mongoHelper = new MongoHelper(this.configuration.getDatabaseName());
        this.scheduledExecutorService = Executors.newScheduledThreadPool(20);

        this.claimGrid = new ClaimGrid();
        this.teamPlayerCache = new TeamPlayerCache();
        this.teamHandler = new TeamHandler(this.teamPlayerCache, this);
        this.borderHandler = new BorderHandler(this);
        this.profileHandler = new TeamProfileHandler(this);
        this.visualHandler = new VisualHandler(this);
        this.timerHandler = new TimerHandler(this);
        this.playerEffectHandler = new PlayerEffectHandler(this);
        this.enchantmentHandler = new EnchantmentHandler(this);
        this.pvPClassHandler = new PvPClassHandler(this);
        this.mobstackHandler = new MobstackHandler(this);
        this.deathbanHandler = new DeathbanHandler(this);
        this.combatLoggerHandler = new CombatLoggerHandler(this);
        this.glowstoneHandler = new GlowstoneHandler(this, this.teamHandler);
        this.gameHandler = new GameHandler(this);
        this.enchantmentHandler = new EnchantmentHandler(this);
        this.crowbarHandler = new CrowbarHandler(this);
        this.kitHandler = new KitHandler();
        this.shopHandler = new ShopHandler(this);
        this.crateHandler = new CrateHandler(this);
        this.elevatorHandler = new ElevatorHandler(this);
        this.endHandler = new EndHandler(this);
        this.leaderboardHandler = new LeaderboardHandler(this);
        this.reclaimHandler = new ReclaimHandler(this);
        this.itemHandler = new CItemHandler(this);

        new BoardHandler(this, new BoardAdapter(this.profileHandler));
        new TabListHandler(this, new TabListAdapter());
        new NametagHandler(this, new NametagAdapter(this.teamHandler, this.profileHandler));

        ListenerHandler.loadListenersFromPackage(this, "rip.alpha.hcf.listener");
        Libraries.getInstance().getCommandFramework().registerClasses(ClassUtils.getClassesInPackage(this, "rip.alpha.hcf.command"));

        if (this.getConfiguration().isKitmap()) {
            new KitmapHandler(this);
        }

        for (World world : Bukkit.getWorlds()) {
            CraftWorld craftWorld = (CraftWorld) world;
            net.minecraft.server.v1_7_R4.World nmsWorld = craftWorld.getHandle();
            PaperSpigotWorldConfig paperSpigotConfig = nmsWorld.paperSpigotConfig;
            SpigotWorldConfig spigotConfig = nmsWorld.spigotConfig;

            if (world.getName().equalsIgnoreCase("world_the_end")) {
                paperSpigotConfig.disableEndCredits = true;
            }

            world.setDifficulty(Difficulty.HARD);

            spigotConfig.arrowDespawnRate = 20;
            spigotConfig.itemDespawnRate = (20 * 60) * 2;
            spigotConfig.antiXray = false;

            if (this.configuration.isKitmap()) {
                spigotConfig.itemDespawnRate = (20 * 30);
                world.setGameRuleValue("doMobSpawning", "false");
                world.getEntitiesByClasses(Creature.class).forEach(Entity::remove);
            }

            world.setThundering(false);
            world.setStorm(false);
            world.setWeatherDuration(Integer.MAX_VALUE);
            world.setGameRuleValue("doFireTick", "false");
            world.setGameRuleValue("mobGriefing", "false");
        }

        fSpigot.INSTANCE.addPacketHandler(new SoundPacketHandler());
        ModSuitePlugin.getInstance().setEnabledModSuiteOnJoin(true);
        Basic.getInstance().enableClearLag();
    }

    @Override
    public void onDisable() {
        this.scheduledExecutorService.shutdown();

        int teamSaved = 0;
        int profileSaved = 0;

        this.gameHandler.save();
        this.gameHandler.getGameScheduleHandler().save();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.hasMetadata("CombatLogger")) {
                    entity.remove();
                }
            }
        }

        for (Team team : this.teamHandler.getTeams()) {
            if (team.isSave()) {
                this.teamHandler.saveTeam(team);
                teamSaved++;
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
            if (profile.getEquipPvPClass() != null) {
                PvPClass pvPClass = HCF.getInstance().getPvPClassHandler().getPvPClass(profile.getEquipPvPClass());
                this.pvPClassHandler.unequipClass(player, profile, pvPClass);
            }
        }

        for (TeamProfile profile : this.profileHandler.getProfiles()) {
            if (profile.isSave()) {
                this.profileHandler.saveProfile(profile);
                profileSaved++;
            }
        }

        int teamSavedCounter = teamSaved;
        int profilesSavedCounter = profileSaved;
        log(logger -> logger.info("Successfully saved " + teamSavedCounter + " teams on quit."));
        log(logger -> logger.info("Successfully saved " + profilesSavedCounter + " profiles on quit."));
    }
}
