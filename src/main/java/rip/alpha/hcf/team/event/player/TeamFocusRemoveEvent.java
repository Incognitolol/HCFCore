package rip.alpha.hcf.team.event.player;

import rip.alpha.hcf.team.event.PlayerTeamEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamFocusRemoveEvent extends PlayerTeamEvent {
    public TeamFocusRemoveEvent(PlayerTeam team) {
        super(team);
    }
}