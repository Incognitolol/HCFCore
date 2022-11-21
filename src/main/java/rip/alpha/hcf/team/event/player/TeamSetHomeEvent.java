package rip.alpha.hcf.team.event.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import rip.alpha.hcf.team.event.PlayerTeamEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

@Getter
public class TeamSetHomeEvent extends PlayerTeamEvent {

    private final Player player;

    public TeamSetHomeEvent(Player player, PlayerTeam team) {
        super(team);
        this.player = player;
    }
}
