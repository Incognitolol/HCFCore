package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.UUID;

public class TeamUninviteCommand {
    @Command(names = {"team uninvite", "t uninvite", "f uninvite", "faction uninvite"}, async = true)
    public static void teamUninvite(Player player, @Param(name = "target") UUID targetUUID) {
        if (player.getUniqueId().equals(targetUUID)) {
            player.sendMessage(CC.RED + "You cannot uninvite yourself!");
            return;
        }

        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You need to be in a team to do this command");
            return;
        }

        PlayerTeam.TeamMember member = team.getMember(player.getUniqueId());
        if (!member.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_CAPTAIN)) {
            player.sendMessage(CC.RED + "You are not high enough in the team hierarchy to remove an invite");
            return;
        }

        if (team.isMember(targetUUID)) {
            player.sendMessage(CC.RED + "That player is already in the team");
            return;
        }

        if (!team.hasInvite(targetUUID)) {
            player.sendMessage(CC.RED + "That player doesn't have an invite");
            return;
        }

        team.removeInvite(targetUUID);
        team.broadcast("&2" + player.getName() + " &7has uninvited &2" + UUIDFetcher.getName(targetUUID) + "&7 from the team.");

        Player target = Bukkit.getPlayer(targetUUID);
        if (target != null) {
            target.sendMessage(CC.RED + "You have been uninvited from " + team.getName());
        }
    }
}
