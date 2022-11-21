package rip.alpha.hcf.team.event.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import rip.alpha.hcf.team.event.PlayerTeamEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

@Getter
public class TeamJoinEvent extends PlayerTeamEvent {
    private final Player player;

    public TeamJoinEvent(PlayerTeam team, Player player) {
        super(team);
        this.player = player;
    }
}
