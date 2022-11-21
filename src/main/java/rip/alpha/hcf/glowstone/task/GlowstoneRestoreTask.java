package rip.alpha.hcf.glowstone.task;

import net.mcscrims.libraries.util.cuboid.SafeBlock;
import net.mcscrims.libraries.util.cuboid.SafeLocation;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;
import rip.alpha.hcf.HCF;

import java.util.ArrayList;
import java.util.Map;

public class GlowstoneRestoreTask implements Runnable {

    private static BukkitTask activeTask;

    private final ArrayList<SafeBlock> blockList;
    private int index;

    public GlowstoneRestoreTask(Map<SafeLocation, SafeBlock> blockMap) {
        ACuboid cuboid = HCF.getInstance().getTeamHandler().getSystemTeamByName("Glowstone").getClaim();
        if (cuboid == null) {
            throw new UnsupportedOperationException();
        }
        this.blockList = new ArrayList<>(blockMap.values());
    }

    public static void start(Map<SafeLocation, SafeBlock> blockMap) {
        if (activeTask != null) {
            activeTask.cancel();
            activeTask = null;
        }
        activeTask = Bukkit.getScheduler().runTaskTimer(HCF.getInstance(), new GlowstoneRestoreTask(blockMap), 1L, 1L);
    }

    @Override
    public void run() {
        if (this.index >= blockList.size()) {
            activeTask.cancel();
            activeTask = null;
            Bukkit.broadcastMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "[Nether] " + ChatColor.YELLOW + "Glowstone has been reset!");
            return;
        }

        for (int i = 0; i < 16; i++) {
            if (this.index >= blockList.size()) {
                continue;
            }
            SafeBlock safeBlock = this.blockList.get(this.index++);
            if (safeBlock == null) {
                continue;
            }
            Block block = safeBlock.toBukkit();
            if (block.getType() == Material.GLOWSTONE) {
                continue;
            }
            block.setTypeIdAndData(safeBlock.getTypeId(), safeBlock.getData(), true);
        }
    }
}
