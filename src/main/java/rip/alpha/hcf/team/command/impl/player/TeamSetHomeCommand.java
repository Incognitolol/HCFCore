package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.player.TeamSetHomeEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamSetHomeCommand {
    @Command(names =
            {
                    "team sethome", "t sethome", "f sethome", "faction sethome",
                    "team sethq", "t sethq", "f sethq", "faction sethq"
            }, async = true)
    public static void teamSetHome(Player player) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        if (team.getClaim() == null) {
            player.sendMessage(CC.RED + "Your team doesn't have a claim, use /team claim to begin claiming");
            return;
        }

        PlayerTeam.TeamMember member = team.getMember(player.getUniqueId());
        if (!member.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_CAPTAIN)) {
            player.sendMessage(CC.RED + "You are not high enough in the team hierarchy to set the teams home");
            return;
        }

        if (!team.getClaim().contains(player.getLocation())) {
            player.sendMessage(CC.RED + "You are not standing in your teams claim");
            return;
        }

        team.setHome(player.getLocation());
        team.broadcast("&2" + player.getName() + "&e has updated the team homes location");
        team.setSave(true);

        new TeamSetHomeEvent(player, team).call(HCF.getInstance());
    }
}
