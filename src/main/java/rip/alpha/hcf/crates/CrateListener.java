package rip.alpha.hcf.crates;

import com.google.common.collect.ImmutableSet;
import io.netty.util.internal.ConcurrentSet;
import lombok.RequiredArgsConstructor;
import net.mcscrims.basic.shutdown.PreShutdownEvent;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TaskUtil;
import net.mcscrims.libraries.util.cuboid.SafeBlock;
import net.mcscrims.libraries.util.cuboid.SafeLocation;
import net.mcscrims.libraries.util.gson.GsonUtil;
import net.mcscrims.libraries.util.items.LoreUtil;
import net.mcscrims.monitor.util.FileConfig;
import net.mcscrims.permissions.PermissionsPlugin;
import net.mcscrims.permissions.profile.PermissionProfile;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.crates.menu.CrateMenu;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.statistics.StatsListener;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.*;

@RequiredArgsConstructor
public class CrateListener implements Listener {

    private final CrateHandler handler;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return;
        }
        Block block = event.getClickedBlock();
        SafeLocation safeLocation = new SafeLocation(block.getLocation());
        Crate crate = this.handler.getCrateByLocation(safeLocation);
        if (crate == null) {
            return;
        }
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.ENDER_CHEST) {
            if (crate.getKey() == null) {
                player.sendMessage(CC.RED + "This crate has no key set.");
                return;
            }

            event.setCancelled(true);

            ItemStack itemStack = event.getItem();
            if (itemStack != null && itemStack.getType() != Material.AIR && crate.getKey().isSimilar(itemStack)) {
                if (crate.getItems().isEmpty()) {
                    player.sendMessage(CC.RED + "There are no items in this crate.");
                    return;
                }

                int open = (int) Arrays.stream(event.getPlayer().getInventory().getContents())
                        .filter(invContents -> invContents == null || invContents.getType() == Material.AIR).count();
                if (open < 5) {
                    player.sendMessage(CC.RED + "You must have at least 5 open inventory slots to use a "
                            + crate.getName() + " key!");
                    event.setCancelled(true);
                    return;
                }

                if (itemStack.getAmount() > 1) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                } else {
                    player.setItemInHand(new ItemStack(Material.AIR));
                }

                this.openCrate(player, crate, crate.getOpenDelay());
            } else {
                player.sendMessage(CC.RED + "You must be holding a " + crate.getName() + " key!");
                TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
                long currentTime = System.currentTimeMillis();
                if (currentTime - profile.getLastCrateAttempt() > 1000) {
                    Location middleOfCrate = block.getLocation().add(0.5, 0.5, 0.5);
                    Vector pushback = player.getLocation().subtract(middleOfCrate).toVector();
                    pushback.normalize().multiply(0.75).setY(0.3);
                    player.setVelocity(pushback);
                    profile.setLastCrateAttempt(currentTime);
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (block.getType() != Material.ENDER_CHEST) {
                return;
            }
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            new CrateMenu(crate).openMenu(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getType() == Material.ENDER_CHEST) {
            SafeLocation safeLocation = new SafeLocation(block.getLocation());
            Crate crate = this.handler.getCrateByLocation(safeLocation);
            if (crate != null) {
                if (!player.isSneaking()) {
                    event.setCancelled(true);
                    return;
                }

                crate.setSafeLocation(null);
                FileConfig fileConfig = this.handler.getCratesConfig();
                fileConfig.getConfig().set("crates." + crate.getName() + ".location", null);
                fileConfig.save();
                player.sendMessage(CC.RED + "You have broken " + crate.getName() + "'s crate location");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getType() == Material.ENDER_CHEST) {
            ItemStack itemStack = event.getItemInHand();
            if (!itemStack.hasItemMeta()) {
                return;
            }

            String lore = LoreUtil.getFirstLoreLine(itemStack);
            if (lore == null || !lore.contains("Crate")) {
                return;
            }

            SafeLocation safeLocation = new SafeLocation(block.getLocation());
            Crate locationCrate = this.handler.getCrateByLocation(safeLocation);
            if (locationCrate != null) {
                player.sendMessage(CC.RED + "There is already a crate in this location");
                event.setCancelled(true);
                return;
            }

            Crate crate = HCF.getInstance().getCrateHandler()
                    .getCrateByName(ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()));
            if (crate == null) {
                return;
            }

            crate.setSafeLocation(safeLocation);
            FileConfig fileConfig = this.handler.getCratesConfig();
            fileConfig.getConfig().set("crates." + crate.getName() + ".location", GsonUtil.GSON.toJson(safeLocation));
            fileConfig.save();
            player.sendMessage(CC.GREEN + "You have placed " + crate.getName() + "'s crate location");
        }
    }

    @EventHandler
    public void onShutdown(PreShutdownEvent event) {
        HCF.getInstance().getCrateHandler().getCratesConfig().save();
    }

    private void openCrate(Player player, Crate crate, int delay) {
        if (delay <= 0) {
            this.openCrate0(player, crate);
            return;
        }

        String crateName = crate.getName();
        if (crateName.equalsIgnoreCase("KoTH") ||
                crateName.equalsIgnoreCase("Palace")
                || crateName.equalsIgnoreCase("CTP")) {
            PermissionProfile permissionsProfile = PermissionsPlugin.getInstance().getPermissionAPI().getProfile(player.getUniqueId());
            Bukkit.broadcastMessage(
                    CC.translate(CC.GOLD + "[Event] " + permissionsProfile.getCurrentRank().getChatColor() + player.getName() + " &eis opening a &6" + crateName + " Crate&e."));
        } else {
            player.sendMessage(CC.GRAY + "Opening crate...");
        }

        HCF.getInstance().getServer().getScheduler()
                .scheduleSyncDelayedTask(HCF.getInstance(), () -> this.openCrate0(player, crate), delay * 20L);
    }

    private void openCrate0(Player player, Crate crate) {
        if (player == null || !player.willBeOnline()) {
            return;
        }
        StringJoiner itemJoiner = new StringJoiner(CC.YELLOW + ", ");
        CrateItem[] crateItems = crate.getCrateItems(3);

        for (CrateItem crateItem : crateItems) {
            if (crateItem == null) {
                continue;
            }

            crateItem.giveItem(player);
            ItemStack item = crateItem.getDisplayItem();
            String itemName;

            if (item.hasItemMeta() && item.getItemMeta().getDisplayName() != null) {
                itemName = item.getItemMeta().getDisplayName();
            } else {
                itemName = WordUtils.capitalize(item.getType().toString().toLowerCase().replace("_", " "));
            }

            itemJoiner.add(CC.GRAY + item.getAmount() + "x " + CC.BLUE + itemName);
        }

        if (crate.getName().equalsIgnoreCase("KoTH") ||
                crate.getName().equalsIgnoreCase("Palace") ||
                crate.getName().equalsIgnoreCase("CTP")) {
            PermissionProfile permissionsProfile = PermissionsPlugin.getInstance().getPermissionAPI().getProfile(player.getUniqueId());
            Bukkit.broadcastMessage(CC.translate("&6[Event] " + permissionsProfile.getCurrentRank().getChatColor() + player.getName() +
                    " &ehas opened a &6" + crate.getName() + " Crate &eand received " + itemJoiner));
        } else {
            player.sendMessage(CC.GRAY + "You have received " + itemJoiner);
        }
    }
}
