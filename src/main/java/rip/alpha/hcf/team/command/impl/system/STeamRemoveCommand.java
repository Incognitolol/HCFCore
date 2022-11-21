package rip.alpha.hcf.team.command.impl.system;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.event.shared.TeamDisbandEvent;
import rip.alpha.hcf.team.impl.SystemTeam;

public class STeamRemoveCommand {
    @Command(names =
            {
                    "systemteam remove", "systeam remove", "steam remove",
                    "systemteam disband", "systeam disband", "steam disband"
            }, async = true, permission = "op")
    public static void systemTeamRemove(Player player, @Param(name = "team") SystemTeam team) {
        HCF.getInstance().getTeamHandler().removeTeam(team);
        new TeamDisbandEvent(player, team).call(HCF.getInstance());
        player.sendMessage(CC.RED + "You have removed that system team");
    }

}
