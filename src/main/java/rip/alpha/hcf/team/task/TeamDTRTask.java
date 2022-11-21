package rip.alpha.hcf.team.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;

@Getter
@RequiredArgsConstructor
public class TeamDTRTask implements Runnable {

    private final TeamHandler teamHandler;

    @Override
    public void run() {
        try {
            for (PlayerTeam team : this.teamHandler.getPlayerTeams()) {
                if (!team.hasDTRFreeze()) {
                    team.incrementDTR();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
