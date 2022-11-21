package rip.alpha.hcf.team.event.shared;

import lombok.Getter;
import org.bukkit.entity.Player;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.event.TeamEvent;

@Getter
public class TeamRemoveClaimEvent extends TeamEvent {

    private final Player player;

    public TeamRemoveClaimEvent(Player player, Team team) {
        super(team);
        this.player = player;
    }
}
