package rip.alpha.hcf.team.task;

import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.event.player.TeamInviteExpireEvent;
import rip.alpha.hcf.team.event.player.TeamRallyExpireEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.Set;
import java.util.stream.Collectors;

public class TeamCacheTask implements Runnable {
    @Override
    public void run() {
        try {
            for (PlayerTeam playerTeam : HCF.getInstance().getTeamHandler().getPlayerTeams()) {
                Set<PlayerTeam.TeamInviteEntry> expired =
                        playerTeam.getInviteEntries().stream().filter(PlayerTeam.TeamInviteEntry::isExpired).collect(Collectors.toSet());

                for (PlayerTeam.TeamInviteEntry entryEntry : expired) {
                    playerTeam.getInviteEntries().remove(entryEntry);
                    new TeamInviteExpireEvent(entryEntry, playerTeam).call(HCF.getInstance());
                }

                if (playerTeam.isRallyExpired() && playerTeam.getRally() != null) {
                    playerTeam.setRally(null);
                    playerTeam.setRallyTime(-1);
                    playerTeam.broadcast("&cYour teams rally point has expired!");
                    new TeamRallyExpireEvent(playerTeam).call(HCF.getInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
