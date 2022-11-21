package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.player.TeamPreLeaveEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamLeaveCommand {
    @Command(names = {"team leave", "t leave", "f leave", "faction leave"}, async = true)
    public static void teamLeaveCommand(Player player) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        if (team.isLeader(player.getUniqueId())) {
            player.sendMessage(CC.RED + "You cannot leave the team as leader, please us /team promote or /team disband");
            return;
        }

        if (team.hasDTRFreeze()) {
            player.sendMessage(CC.RED + "You cannot leave the team on dtr freeze");
            return;
        }

        new TeamPreLeaveEvent(team, player).call(HCF.getInstance());
        team.removeMember(player.getUniqueId());
        team.broadcast("&e" + player.getName() + "&c has left the team!");
        player.sendMessage(CC.translate("&cYou have left the team!"));
        team.setSave(true);
    }

}
