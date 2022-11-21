package rip.alpha.hcf.team.task;

import lombok.RequiredArgsConstructor;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;

@RequiredArgsConstructor
public class TeamSaveTask implements Runnable {

    private final TeamHandler handler;

    @Override
    public void run() {
        try {
            int i = 0;
            for (Team team : handler.getTeams()) {
                if (team.isSave()) {
                    handler.saveTeam(team);
                    i++;
                }
            }

            if (i > 0) {
                int count = i;
                HCF.log(logger -> logger.info("Successfully saved " + count + " teams"));
            } else {
                HCF.log(logger -> logger.info("No teams where saved"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
