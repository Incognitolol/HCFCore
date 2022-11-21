package rip.alpha.hcf.elevator;

import net.mcscrims.libraries.util.BlockUtil;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TaskUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;

public class ElevatorHandler {

    public ElevatorHandler(HCF instance) {
        instance.getServer().getPluginManager().registerEvents(new ElevatorListener(this), instance);
    }

    public void useElevatorUp(final Player player, final Location location) { //we should final because we are going to use async
        location.getWorld().getChunkAtAsync(location, chunk -> {
            for (int i = location.getBlockY() + 2; i < 256; i++) {
                Block block = chunk.getBlock(location.getBlockX(), i, location.getBlockZ());
                if (!this.isValidBlock(block)) {
                    continue;
                }
                Block upBlock = block.getRelative(BlockFace.UP);
                if (!this.isValidBlock(upBlock)) {
                    continue;
                }
                Block downBlock = block.getRelative(BlockFace.DOWN);
                if (BlockUtil.isNonSolidBlock(downBlock)) {
                    continue;
                }
                TaskUtil.runSync(() -> {
                    Location tp = block.getLocation().clone().add(0.5, 0, 0.5);
                    tp.setYaw(player.getLocation().getYaw());
                    tp.setPitch(player.getLocation().getPitch());
                    player.teleport(tp);
                }, HCF.getInstance());
                return;
            }
            player.sendMessage(CC.RED + "No location above found.");
        });
    }

    public void useElevatorDown(final Player player, final Location location) { //we should final because we are going to use async
        location.getWorld().getChunkAtAsync(location, chunk -> {
            for (int i = location.getBlockY() - 2; i > 0; i--) {
                Block block = chunk.getBlock(location.getBlockX(), i, location.getBlockZ());
                if (!this.isValidBlock(block)) {
                    continue;
                }
                Block relBlock = block.getRelative(BlockFace.DOWN);
                if (!this.isValidBlock(relBlock)) {
                    continue;
                }
                Block downBlock = relBlock.getRelative(BlockFace.DOWN);
                if (BlockUtil.isNonSolidBlock(downBlock)) {
                    continue;
                }
                TaskUtil.runSync(() -> {
                    Location tp = relBlock.getLocation().clone().add(0.5, 0, 0.5);
                    tp.setYaw(player.getLocation().getYaw());
                    tp.setPitch(player.getLocation().getPitch());
                    player.teleport(tp);
                }, HCF.getInstance());
                return;
            }
            player.sendMessage(CC.RED + "No location below found.");

        });
    }

    private boolean isValidBlock(Block block) {
        if (block.isLiquid()) {
            return false;
        }
        return BlockUtil.isNonSolidBlock(block);
    }
}
