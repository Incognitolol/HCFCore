package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.command.TeamCommandConstants;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.UUID;

public class TeamPromoteCommand {
    @Command(names = {"team promote", "t promote", "f promote", "faction promote"}, async = true)
    public static void teamPromoteCommand(Player player, @Param(name = "target") UUID targetUUID) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        PlayerTeam.TeamMember playerMember = team.getMember(player.getUniqueId());
        PlayerTeam.TeamMember targetMember = team.getMember(targetUUID);

        if (targetMember == null) {
            player.sendMessage(CC.RED + "That player is not in the team");
            return;
        }

        int targetRole = targetMember.getRole() + 1;
        TeamCommandConstants.handlePromote(player, team, playerMember, targetMember, targetRole);
    }
}
