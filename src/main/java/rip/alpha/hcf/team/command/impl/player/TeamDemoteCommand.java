package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.UUID;

public class TeamDemoteCommand {
    @Command(names = {"team demote", "t demote", "f demote", "faction demote"}, async = true)
    public static void teamDemoteCommand(Player player, @Param(name = "target") UUID targetUUID) {
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

        if (targetMember.isHigherOrEqual(playerMember.getRole())) {
            player.sendMessage(CC.RED + "You do not have permission to demote that player in the team");
            return;
        }

        int targetRole = targetMember.getRole() - 1;
        if (targetRole < 0) {
            player.sendMessage(CC.RED + "You cannot demote this player any further");
            return;
        }

        team.demoteMember(targetMember);
        team.broadcast("&a" + targetMember.getName() + " &ehas been demoted to &a" + targetMember.getRoleNameById());
        team.setSave(true);
    }
}
