package rip.alpha.hcf.combatlogger;

import lombok.Getter;
import net.mcscrims.libraries.villager.VillagerRegister;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.timer.impl.CombatTagTimer;
import rip.alpha.modsuite.ModSuiteMode;
import rip.alpha.modsuite.ModSuitePlugin;
import rip.alpha.modsuite.profile.ModSuiteProfile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
public class CombatLoggerHandler {

    private final Map<UUID, CombatLoggerVillager> loggerMap;

    public CombatLoggerHandler(HCF instance) {
        new VillagerRegister(CombatLoggerVillager.class, "CombatLogger", 120);
        this.loggerMap = new ConcurrentHashMap<>();

        HCF.getInstance().getScheduledExecutorService()
                .scheduleAtFixedRate(new CombatLoggerTask(this), 1L, 1L, TimeUnit.SECONDS);

        PluginManager pluginManager = instance.getServer().getPluginManager();
        pluginManager.registerEvents(new CombatLoggerListener(this), instance);
    }

    public void spawnVillager(Player player) {
        ModSuiteProfile modSuiteProfile = ModSuitePlugin.getInstance().getModSuiteAPI().getProfile(player);
        if (modSuiteProfile != null) {
            if (modSuiteProfile.getModSuiteMode() != ModSuiteMode.NONE) {
                return;
            }
        }

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        List<Entity> entities = player.getNearbyEntities(35, 35, 35);
        entities.removeIf(entity -> !(entity instanceof Player));
        if (teamProfile.getTimer(CombatTagTimer.class) == null || entities.size() <= 0) {
            return;
        }

        Location location = player.getLocation();
        CombatLoggerVillager combatLoggerVillager = new CombatLoggerVillager(player, location.getWorld(), location);
        combatLoggerVillager.spawn(player.getWorld());
        this.loggerMap.put(player.getUniqueId(), combatLoggerVillager);
    }

    public CombatLoggerVillager getVillager(UUID uuid) {
        return this.loggerMap.get(uuid);
    }

    public void removeVillager(UUID uuid) {
        this.loggerMap.remove(uuid);
    }
}
