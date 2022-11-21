package rip.alpha.hcf.team.event.player;

import lombok.Getter;
import rip.alpha.hcf.team.event.PlayerTeamEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

@Getter
public class TeamInviteExpireEvent extends PlayerTeamEvent {

    private final PlayerTeam.TeamInviteEntry teamInviteEntry;

    public TeamInviteExpireEvent(PlayerTeam.TeamInviteEntry teamInviteEntry, PlayerTeam playerTeam) {
        super(playerTeam);
        this.teamInviteEntry = teamInviteEntry;
    }
}
