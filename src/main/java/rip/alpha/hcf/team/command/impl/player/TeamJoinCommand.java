package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.player.TeamJoinEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamJoinCommand {
    @Command(names = {"team join", "t join", "f join", "faction join"}, async = true)
    public static void teamJoin(Player player, @Param(name = "team") PlayerTeam team) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam playerTeam = teamHandler.getPlayerTeamByPlayer(player);

        if (playerTeam != null) {
            player.sendMessage(CC.RED + "You are already in a team, Please leave your current team");
            return;
        }

        if (team.isFull()) {
            player.sendMessage(CC.RED + "That team is full");
            return;
        }

        if (!team.hasInvite(player.getUniqueId())) {
            player.sendMessage(CC.RED + "You do not have a valid invite to that team");
            return;
        }

        if (team.hasDTRFreeze()) {
            player.sendMessage(CC.RED + "You cannot join that team because it is on dtr freeze.");
            return;
        }

        team.broadcast("&a" + player.getName() + " has joined the team!");
        team.addMember(player.getUniqueId(), player.getName());
        team.removeInvite(player.getUniqueId());
        team.removeInviteCount();
        player.sendMessage(CC.translate("&2[Team] &aYou have joined team " + team.getName() + "!"));
        team.setSave(true);

        new TeamJoinEvent(team, player).call(HCF.getInstance());
    }
}
