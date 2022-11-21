package rip.alpha.hcf.team.command.impl.admin;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.command.TeamCommandConstants;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.UUID;

public class ATeamForcePromoteCommand {
    @Command(names = {"teamadmin forcepromote", "tadmin forcepromote", "ta forcepromote"}, permission = "teamadmin.forcepromote", async = true)
    public static void teamAdminForcePromote(Player player, @Param(name = "target", defaultValue = "self") UUID targetUUID) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(targetUUID);

        if (team == null) {
            player.sendMessage(CC.RED + "That player is not in a team!");
            return;
        }

        PlayerTeam.TeamMember playerMember = team.getMember(player.getUniqueId());
        PlayerTeam.TeamMember targetMember = team.getMember(targetUUID);

        if (targetMember == null) {
            player.sendMessage(CC.RED + "That player is not in the team");
            return;
        }

        int targetRole = targetMember.getRole() + 1;
        TeamCommandConstants.handlePromote(player, team, playerMember, targetMember, targetRole, true);
        player.sendMessage(CC.GREEN + "You have demoted " + UUIDFetcher.getName(targetUUID) + ".");
    }
}
