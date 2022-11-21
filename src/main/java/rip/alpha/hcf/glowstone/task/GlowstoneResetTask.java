package rip.alpha.hcf.glowstone.task;

import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.Team;

public class GlowstoneResetTask implements Runnable {
    @Override
    public void run() {
        try {
            Team team = HCF.getInstance().getTeamHandler().getSystemTeamByName("Glowstone");
            if (team == null || team.getClaim() == null) {
                return;
            }
            if (HCF.getInstance().getGlowstoneHandler().getBlockMap().isEmpty()) {
                return;
            }
            HCF.getInstance().getGlowstoneHandler().resetBlocks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
