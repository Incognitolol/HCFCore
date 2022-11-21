package rip.alpha.hcf.listener;

import net.mcscrims.libraries.util.BlockUtil;
import net.mcscrims.libraries.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.settings.Setting;
import rip.alpha.hcf.timer.impl.PvPTimer;
import rip.alpha.modsuite.ModSuiteMode;
import rip.alpha.modsuite.ModSuitePlugin;

import java.util.HashSet;
import java.util.Set;

public class PlayerListener implements Listener {

    private final ItemStack bookItemStack;
    private final ItemStack foodItemStack;

    public static final Set<Material> MOB_DROPS;

    static {
        MOB_DROPS = new HashSet<>();
        MOB_DROPS.add(Material.ROTTEN_FLESH);
        MOB_DROPS.add(Material.SLIME_BALL);
        MOB_DROPS.add(Material.BONE);
        MOB_DROPS.add(Material.SPIDER_EYE);
        MOB_DROPS.add(Material.STRING);
    }

    public PlayerListener() {
        this.bookItemStack = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta itemMeta = this.bookItemStack.getItemMeta();
        BookMeta bookMeta = (BookMeta) itemMeta;
        bookMeta.addPage(CC.translate("Welcome to Alpha Teams"));
        bookMeta.setAuthor("Alpha Network");
        bookMeta.setTitle(CC.translate("&cWelcome to Alpha Teams Map " + HCF.getInstance().getConfiguration().getMapNumber()));

        this.bookItemStack.setItemMeta(bookMeta);
        this.foodItemStack = new ItemStack(Material.COOKED_BEEF, 16);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
            boolean kitmap = HCF.getInstance().getConfiguration().isKitmap();
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            if (!kitmap) {
                if (ModSuitePlugin.getInstance().getModSuiteAPI().getCurrentMode(player.getPlayer()) == ModSuiteMode.NONE) {
                    profile.addTimer(new PvPTimer());
                    player.getInventory().addItem(this.bookItemStack, this.foodItemStack);
                    player.sendMessage(CC.GREEN + "Hey " + player.getName() + ". Welcome to Alpha Map " + HCF.getInstance().getConfiguration().getMapNumber() + ", we hope you enjoy our beta :)");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        ItemStack itemStack = event.getItem().getItemStack();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }
        Player player = event.getPlayer();
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);

        if (itemStack.getType() == Material.COBBLESTONE || itemStack.getType() == Material.STONE) {
            boolean cobble = teamProfile.getSetting(Setting.COBBLE);
            if (!cobble) {
                event.setCancelled(true);
            }
        } else if (MOB_DROPS.contains(itemStack.getType())) {
            boolean mobDrops = teamProfile.getSetting(Setting.MOB_DROPS);
            if (!mobDrops) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        if (block == null || block.getType() == Material.AIR) {
            return;
        }
        if (BlockUtil.isNonSolidBlock(block)) {
            return; //check if its actually a block
        }
        Player player = event.getPlayer();
        int ping = ((CraftPlayer) player).getHandle().ping;
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        teamProfile.setBlockGlitchDelay(System.currentTimeMillis() + Math.min(2000, 800L + ping)); //interact delay for sign.
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            TeamProfile damagerProfile = HCF.getInstance().getProfileHandler().getProfile(damager);
            if (damagerProfile.isNotBlockGlitch()) {
                return;
            }
            event.setCancelled(true);
        }
    }
}
