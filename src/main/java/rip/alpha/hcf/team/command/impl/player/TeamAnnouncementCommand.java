package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamAnnouncementCommand {
    @Command(names =
            {
                    "team announcement", "t announcement", "f announcement", "faction announcement",
                    "team announce", "t announce", "f announce", "faction announce",
                    "team a", "t a", "f a", "faction a"
            }, async = true)
    public static void teamAnnouncementCommand(Player player, @Param(name = "announcement", wildcard = true) String announcement) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        PlayerTeam.TeamMember member = team.getMember(player.getUniqueId());
        if (!member.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_CAPTAIN)) {
            player.sendMessage(CC.RED + "You are not high enough in the team hierarchy to withdraw from the teams balance");
            return;
        }

        team.setAnnouncement(announcement);
        team.setSave(true);
        team.broadcast("&a" + player.getName() + "&e has updated the teams announcement.");
    }

}
