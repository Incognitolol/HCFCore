package rip.alpha.hcf.glowstone;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;

@RequiredArgsConstructor
public class GlowstoneListener implements Listener {

    private final TeamHandler teamHandler;

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();
        if (block == null || block.getType() != Material.GLOWSTONE) {
            return;
        }

        if (HCF.getInstance().getBorderHandler().isBypass(event.getPlayer())) {
            return;
        }

        Team blockTeam = this.teamHandler.getTeamByLocation(block.getLocation());
        if (blockTeam == null) {
            return;
        }
        if (blockTeam.getName().equalsIgnoreCase("Glowstone")) {
            event.setCancelled(false);
        }
    }
}
