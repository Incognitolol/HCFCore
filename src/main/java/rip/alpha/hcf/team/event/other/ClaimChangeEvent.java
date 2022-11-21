package rip.alpha.hcf.team.event.other;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.event.BaseEvent;
import org.bukkit.entity.Player;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;

@Getter
@RequiredArgsConstructor
public class ClaimChangeEvent extends BaseEvent {

    private final Player player;
    private final TeamProfile teamProfile;
    private final Team from;
    private final Team to;

}
