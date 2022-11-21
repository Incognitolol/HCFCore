package rip.alpha.hcf.team.command;

import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.regex.Pattern;

public class TeamCommandConstants {
    public static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    public static void handlePromote(Player player, PlayerTeam team, PlayerTeam.TeamMember playerMember, PlayerTeam.TeamMember targetMember, int targetRole) {
        handlePromote(player, team, playerMember, targetMember, targetRole, false);
    }

    public static void handlePromote(Player player, PlayerTeam team, PlayerTeam.TeamMember playerMember, PlayerTeam.TeamMember targetMember, int targetRole, boolean force) {
        if (targetRole == PlayerTeam.TeamMember.TEAM_LEADER) {
            player.sendMessage("You cannot promote any further, use /team leader <target>");
            return;
        }

        if (targetMember.isEqual(targetRole)) {
            player.sendMessage(CC.RED + "That player is already that rank");
            return;
        }

        if (!force) {
            if (targetMember.isHigherOrEqual(playerMember.getRole())) {
                player.sendMessage(CC.RED + "You do not have permission to promote that player in the team");
                return;
            }

            if (playerMember.isLowerOrEqual(targetRole)) {
                player.sendMessage(CC.RED + "You cannot promote to a higher or equal role to yours");
                return;
            }
        }

        team.updateMemberRole(targetMember, targetRole);
        team.broadcast("&a" + targetMember.getName() + " &ehas been promoted to &a" + targetMember.getRoleNameById());
        team.setSave(true);
    }
}
