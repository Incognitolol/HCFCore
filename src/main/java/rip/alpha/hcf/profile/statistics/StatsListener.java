package rip.alpha.hcf.profile.statistics;

import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import net.mcscrims.basic.Basic;
import net.mcscrims.basic.profile.BasicProfile;
import net.mcscrims.libraries.util.TaskUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.TeamProfileHandler;
import rip.alpha.hcf.profile.settings.Setting;

import java.util.Set;

@RequiredArgsConstructor
public class StatsListener implements Listener {

    private final TeamProfileHandler profileHandler;

    public static final Set<BlockFace> CHECK_FACES = ImmutableSet.of(
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.NORTH_EAST,
            BlockFace.NORTH_WEST,
            BlockFace.SOUTH_EAST,
            BlockFace.SOUTH_WEST,
            BlockFace.UP,
            BlockFace.DOWN);

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!block.getType().name().contains("_ORE")) {
            return;
        }
        if (block.hasMetadata("placedOre")) {
            return;
        }
        Player player = event.getPlayer();
        this.incrementStats(player, block);
        if (block.hasMetadata("detectedDiamond")) {
            return;
        }

        if (block.getType() == Material.DIAMOND_ORE) {
            TaskUtil.runAsync(() -> {
                int diamonds = countRelativeDiamonds(block);
                HCF.getInstance().getServer().getOnlinePlayers().forEach(target -> {
                    TeamProfile targetProfile = HCF.getInstance().getProfileHandler().getProfile(target);
                    if (targetProfile.getSetting(Setting.FOUND_DIAMONDS)) {
                        target.sendMessage("[FD] " + ChatColor.AQUA + player.getName() + " found " + diamonds + " diamond" + (diamonds == 1 ? "" : "s") + ".");
                    }
                });
            }, HCF.getInstance());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayer(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (!block.getType().name().contains("_ORE")) {
            return;
        }
        block.setMetadata("placedOre", new FixedMetadataValue(HCF.getInstance(), true));
    }

    private int countRelativeDiamonds(Block block) {
        int diamonds = 1;
        block.setMetadata("detectedDiamond", new FixedMetadataValue(HCF.getInstance(), true));
        for (BlockFace checkFace : CHECK_FACES) {
            Block relative = block.getRelative(checkFace);
            if (relative.getType() == Material.DIAMOND_ORE) {
                if (!relative.hasMetadata("detectedDiamond") && !relative.hasMetadata("placedOre")) {
                    relative.setMetadata("detectedDiamond", new FixedMetadataValue(HCF.getInstance(), true));
                    diamonds += countRelativeDiamonds(relative);
                }
            }
        }

        return (diamonds);
    }

    private void incrementStats(Player player, Block block) {
        ProfileStatTypes profileStatisticsType = ProfileStatTypes.getByMaterial(block.getType());
        if (profileStatisticsType == null) {
            return;
        }

        if (profileStatisticsType == ProfileStatTypes.DIAMOND_ORE) {
            BasicProfile basicProfile = Basic.getInstance().getBasicAPI().getProfile(player.getUniqueId());
            basicProfile.addXp(25);
        }

        TeamProfile profile = this.profileHandler.getProfile(player.getUniqueId());
        if (profile == null) {
            return;
        }
        profile.incrementStat(profileStatisticsType);
    }
}
