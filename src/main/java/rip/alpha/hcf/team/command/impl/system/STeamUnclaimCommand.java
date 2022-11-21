package rip.alpha.hcf.team.command.impl.system;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.event.shared.TeamRemoveClaimEvent;
import rip.alpha.hcf.team.impl.SystemTeam;

public class STeamUnclaimCommand {
    @Command(names = {"systemteam unclaim", "systeam unclaim", "steam unclaim"}, async = true, permission = "op")
    public static void systemRemoveClaim(Player player, @Param(name = "team") SystemTeam team) {
        if (team.getClaim() == null) {
            player.sendMessage(CC.RED + "That faction doesn't have a claim");
            return;
        }

        team.setClaim(null);
        team.setSave(true);
        player.sendMessage(CC.GREEN + "You have removed that teams claim.");

        new TeamRemoveClaimEvent(player, team).call(HCF.getInstance());
    }
}
