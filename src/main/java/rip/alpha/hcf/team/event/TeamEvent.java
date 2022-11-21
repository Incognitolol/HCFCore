package rip.alpha.hcf.team.event;

import lombok.Getter;
import net.mcscrims.libraries.event.BaseEvent;
import rip.alpha.hcf.team.Team;

@Getter
public class TeamEvent extends BaseEvent {

    private final Team team;

    public TeamEvent(Team team) {
        super(true);
        this.team = team;
    }
}
