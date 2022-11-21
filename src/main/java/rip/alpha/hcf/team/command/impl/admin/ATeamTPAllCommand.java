package rip.alpha.hcf.team.command.impl.admin;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class ATeamTPAllCommand {
    @Command(names = {"teamadmin tpall", "tadmin tpall", "ta tpall"}, permission = "op")
    public static void teamAdminTPAll(Player player, @Param(name = "team") PlayerTeam team) {
        int i = 0;
        for (PlayerTeam.TeamMember member : team.getOnlineMembers()) {
            Player online = member.toPlayer();
            if (online == null) {
                continue;
            }
            online.teleport(player.getLocation());
            i++;
        }
        if (i <= 0) {
            return;
        }
        player.sendMessage(CC.GREEN + "You have successfully teleported " + i + " players");
    }
}
