package rip.alpha.hcf.border.listener;

import com.google.common.collect.Sets;
import net.mcscrims.libraries.util.CC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import rip.alpha.hcf.border.BorderHandler;

import java.util.Set;

public class WarzoneListener implements Listener {

    private final Set<Material> whitelistedMaterials = Sets.newHashSet();
    private final BorderHandler borderHandler;

    public WarzoneListener(BorderHandler borderHandler) {
        this.borderHandler = borderHandler;

        this.whitelistedMaterials.add(Material.SUGAR_CANE_BLOCK);
        this.whitelistedMaterials.add(Material.LOG);
        this.whitelistedMaterials.add(Material.LOG_2);
        this.whitelistedMaterials.add(Material.DOUBLE_PLANT);
        this.whitelistedMaterials.add(Material.RED_ROSE);
        this.whitelistedMaterials.add(Material.YELLOW_FLOWER);
        this.whitelistedMaterials.add(Material.LONG_GRASS);
        this.whitelistedMaterials.add(Material.LEAVES);
        this.whitelistedMaterials.add(Material.LEAVES_2);
    }


    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (this.borderHandler.isBypass(player)) {
            return;
        }
        Block block = event.getBlock();
        if (block == null) {
            return;
        }

        Location location = block.getLocation();

        int x = Math.abs(location.getBlockX());
        int z = Math.abs(location.getBlockZ());

        if (!this.borderHandler.inWarzone(x, z)) {
            return;
        }
        if (this.borderHandler.inBuildRadius(block.getLocation())) {
            return;
        }

        if (location.getWorld().getEnvironment() == World.Environment.NORMAL) {
            if (x >= 250 || z >= 250) {
                if (this.whitelistedMaterials.contains(block.getType())) {
                    return;
                }
            }
        }

        if (block.getType() == Material.GLOWSTONE) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(CC.RED + "You cannot break blocks in warzone.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (this.borderHandler.isBypass(player)) {
            return;
        }
        Block block = event.getBlock();
        if (block == null) {
            return;
        }
        if (!this.borderHandler.inWarzone(block.getLocation())) {
            return;
        }
        if (this.borderHandler.inBuildRadius(block.getLocation())) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(CC.RED + "You cannot place blocks in warzone.");
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
        Player player = event.getPlayer();
        if (this.borderHandler.isBypass(player)) {
            return;
        }
        Block block = event.getBlockClicked();
        if (block == null) {
            return;
        }
        if (!this.borderHandler.inWarzone(block.getLocation())) {
            return;
        }
        if (this.borderHandler.inBuildRadius(block.getLocation())) {
            return;
        }
        event.setCancelled(true);
    }
}
