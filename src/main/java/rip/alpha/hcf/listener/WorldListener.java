package rip.alpha.hcf.listener;

import net.mcscrims.libraries.util.CC;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import rip.alpha.hcf.HCF;

public class WorldListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.BUILD_WITHER ||
                reason == CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN ||
                reason == CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (HCF.getInstance().getBorderHandler().inWarzone(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!event.toWeatherState()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().clear();
    }

    @EventHandler
    public void onBlockBreak(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        Block block = event.getBlock();
        if (block.getWorld().getEnvironment() == World.Environment.NETHER) {
            if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                player.sendMessage(CC.RED + "You cannot place chests in nether.");
                event.setCancelled(true);
            }
        }
    }

}
