package rip.alpha.hcf.team.event.player;

import rip.alpha.hcf.team.event.PlayerTeamEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamRallyExpireEvent extends PlayerTeamEvent {
    public TeamRallyExpireEvent(PlayerTeam team) {
        super(team);
    }
}
