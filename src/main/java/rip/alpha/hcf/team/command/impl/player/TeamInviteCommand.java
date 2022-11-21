package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.SimpleText;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.UUID;

public class TeamInviteCommand {
    @Command(names = {"team invite", "t invite", "f invite", "faction invite"}, async = true)
    public static void teamInvite(Player player, @Param(name = "target") UUID targetUUID) {
        if (player.getUniqueId().equals(targetUUID)) {
            player.sendMessage(CC.RED + "You cannot invite yourself!");
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
            player.sendMessage(CC.RED + "You are not high enough in the team hierarchy to create an invite");
            return;
        }

        if (team.isMember(targetUUID)) {
            player.sendMessage(CC.RED + "That player is already in the team");
            return;
        }

        if (team.hasInvite(targetUUID)) {
            player.sendMessage(CC.RED + "That player already has an invite");
            return;
        }

        if (HCF.getInstance().getConfiguration().isUseMaxInvites()) {
            if (team.getInvites() <= 0) {
                player.sendMessage(CC.RED + "Your team has no invites left.");
                return;
            }
        }

        team.addInvite(targetUUID);
        team.broadcast("&a" + player.getName() + " has invited &e" + UUIDFetcher.getName(targetUUID) + "&a to the team!");

        Player target = Bukkit.getPlayer(targetUUID);
        if (target != null) {
            SimpleText simpleText = new SimpleText("&2[Team] &aYou have been invited to join &e" + team.getName() + "&a!");
            simpleText.click("/team join " + team.getName());
            simpleText.send(target);
        }
    }

}
