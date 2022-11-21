package rip.alpha.hcf.team.event;

import rip.alpha.hcf.team.impl.PlayerTeam;

public class PlayerTeamEvent extends TeamEvent {
    public PlayerTeamEvent(PlayerTeam team) {
        super(team);
    }
}
