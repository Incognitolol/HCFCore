package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.shared.TeamDisbandEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamDisbandCommand {
    @Command(names = {"team disband", "t disband", "f disband", "faction disband"}, async = true)
    public static void teamDisbandCommand(Player player) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        if (!team.isLeader(player.getUniqueId())) {
            player.sendMessage(CC.RED + "You need to be leader to disband this team");
            return;
        }

        if (team.hasDTRFreeze()) {
            player.sendMessage(CC.RED + "You cannot disband the team on dtr freeze");
            return;
        }

        teamHandler.removeTeam(team);
        new TeamDisbandEvent(player, team).call(HCF.getInstance());
    }
}
