package rip.alpha.hcf.crowbar;

import net.mcscrims.libraries.spawner.SpawnerEntry;
import net.mcscrims.libraries.util.CC;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.impl.SystemTeam;

public class CrowbarListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (block.getType() != Material.MOB_SPAWNER) {
            return;
        }
        if (HCF.getInstance().getBorderHandler().isBypass(player)) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(CC.RED + "You cannot break a spawner, please use a crowbar");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!event.hasItem() || !event.hasBlock()) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block.getType() != Material.MOB_SPAWNER && block.getType() != Material.ENDER_PORTAL_FRAME) {
            return;
        }

        PlayerTeam playerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
        if (!HCF.getInstance().getBorderHandler().isBypass(player)) {
            if (!block.getWorld().getName().equals("world")) {
                player.sendMessage(CC.RED + "You cannot crowbar anything outside of overworld.");
                return;
            }

            Team blockTeam = HCF.getInstance().getTeamHandler().getTeamByLocation(block.getLocation());
            if (blockTeam instanceof PlayerTeam) {
                PlayerTeam playerBlockTeam = (PlayerTeam) blockTeam;
                if (!playerBlockTeam.isRaidable()) {
                    if (playerTeam == null || !playerBlockTeam.getId().equals(playerTeam.getId())) {
                        player.sendMessage(CC.RED + "You cannot crowbar spawners from a team claim.");
                        return;
                    }
                }
            } else if (blockTeam instanceof SystemTeam) {
                player.sendMessage(CC.RED + "You cannot crowbar spawners from a system claim.");
                return;
            }
        }

        ItemStack itemStack = event.getItem();
        CrowbarItem crowbarItem = CrowbarItem.fromItemStack(itemStack);
        if (crowbarItem == null) {
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(CC.RED + "You need an empty slot!");
            return;
        }

        if (block.getType() == Material.MOB_SPAWNER) {
            if (!crowbarItem.hasSpawnerUse()) {
                player.sendMessage(CC.RED + "That crowbar has no more spawner uses left.");
                return;
            }

            BlockState state = block.getState();
            if (state instanceof CreatureSpawner) {
                CreatureSpawner spawner = (CreatureSpawner) state;
                EntityType type = spawner.getSpawnedType();
                SpawnerEntry entry = new SpawnerEntry(type);
                ItemStack spawnerItem = entry.toItemStack();
                player.getInventory().addItem(spawnerItem);
                block.setType(Material.AIR);
                crowbarItem.decrementSpawnerUse();
                player.getInventory().removeItem(itemStack);
                player.getInventory().addItem(crowbarItem.toItemStack());
            }
        } else if (block.getType() == Material.ENDER_PORTAL_FRAME) {
            if (!crowbarItem.hasEndFrameUse()) {
                player.sendMessage(CC.RED + "That crowbar has no more end frame uses left.");
                return;
            }

            player.getInventory().addItem(new ItemStack(Material.ENDER_PORTAL_FRAME));
            block.setType(Material.AIR);
            crowbarItem.decrementEndFrameUse();
            player.getInventory().removeItem(itemStack);
            player.getInventory().addItem(crowbarItem.toItemStack());
        }
    }
}
