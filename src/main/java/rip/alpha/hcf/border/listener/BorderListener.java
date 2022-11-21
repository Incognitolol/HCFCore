package rip.alpha.hcf.border.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.border.BorderHandler;

@RequiredArgsConstructor
public class BorderListener implements Listener {

    private final BorderHandler borderHandler;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block == null) {
            return;
        }
        Location location = block.getLocation();
        boolean border = this.borderHandler.inBorder(location);
        if (!border) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block == null) {
            return;
        }
        Location location = block.getLocation();
        boolean border = this.borderHandler.inBorder(location);
        if (!border) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) {
            return;
        }
        Location location = entity.getLocation();
        boolean border = this.borderHandler.inBorder(location);
        if (!border) {
            entity.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        int borderSize = this.borderHandler.getWorldBorder();

        if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ() || from.getBlockY() != to.getBlockY()) {
            if (Math.abs(event.getTo().getBlockX()) > borderSize || Math.abs(event.getTo().getBlockZ()) > borderSize) {
                if (event.getPlayer().getVehicle() != null) {
                    event.getPlayer().getVehicle().eject();
                }

                Location newLocation = event.getTo().clone();
                int tries = 0;

                while (Math.abs(newLocation.getX()) > borderSize && tries++ < 100) {
                    newLocation.setX(newLocation.getX() - (newLocation.getX() > 0 ? 1 : -1));
                }

                if (tries >= 99) {
                    HCF.log(logger -> logger.warning("The server would have crashed while doing border checks! New X: " + newLocation.getX() + ", Old X: " + event.getTo().getBlockX()));
                    return;
                }

                tries = 0;

                while (Math.abs(newLocation.getZ()) > borderSize && tries++ < 100) {
                    newLocation.setZ(newLocation.getZ() - (newLocation.getZ() > 0 ? 1 : -1));
                }

                if (tries >= 99) {
                    HCF.log(logger -> logger.warning("The server would have crashed while doing border checks! New Z: " + newLocation.getZ() + ", Old Z: " + event.getTo().getBlockZ()));
                    return;
                }

                while (newLocation.getBlock().getType() != Material.AIR) {
                    newLocation.setY(newLocation.getBlockY() + 1);
                }

                event.setTo(newLocation);
                event.getPlayer().sendMessage(ChatColor.RED + "You have hit the border!");
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        int borderSize = this.borderHandler.getWorldBorder();

        if (!event.getTo().getWorld().equals(event.getFrom().getWorld())) {
            return;
        }

        if (event.getTo().distance(event.getFrom()) < 0 || event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            return;
        }

        if (Math.abs(event.getTo().getBlockX()) > borderSize || Math.abs(event.getTo().getBlockZ()) > borderSize) {
            Location newLocation = event.getTo().clone();

            while (Math.abs(newLocation.getX()) > borderSize) {
                newLocation.setX(newLocation.getX() - (newLocation.getX() > 0 ? 1 : -1));
            }

            while (Math.abs(newLocation.getZ()) > borderSize) {
                newLocation.setZ(newLocation.getZ() - (newLocation.getZ() > 0 ? 1 : -1));
            }

            while (newLocation.getBlock().getType() != Material.AIR) {
                newLocation.setY(newLocation.getBlockY() + 1);
            }

            event.setTo(newLocation);
            event.getPlayer().sendMessage(ChatColor.RED + "That location is past the border.");
        }
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        this.handleBucketEvent(event);
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        this.handleBucketEvent(event);
    }

    private void handleBucketEvent(PlayerBucketEvent event) {
        Block block = event.getBlockClicked();
        if (block == null) {
            return;
        }
        Location location = block.getLocation();
        boolean border = this.borderHandler.inBorder(location);
        if (!border) {
            event.setCancelled(true);
        }
    }
}
