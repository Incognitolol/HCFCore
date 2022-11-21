package rip.alpha.hcf.team.event.player;

import rip.alpha.hcf.team.event.PlayerTeamEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamRallySetEvent extends PlayerTeamEvent {
    public TeamRallySetEvent(PlayerTeam team) {
        super(team);
    }
}
