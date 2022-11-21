package rip.alpha.hcf.team.event.player;

import lombok.Getter;
import rip.alpha.hcf.team.event.PlayerTeamEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

@Getter
public class TeamFocusEvent extends PlayerTeamEvent {
    private final PlayerTeam targetTeam;

    public TeamFocusEvent(PlayerTeam team, PlayerTeam targetTeam) {
        super(team);
        this.targetTeam = targetTeam;
    }
}
