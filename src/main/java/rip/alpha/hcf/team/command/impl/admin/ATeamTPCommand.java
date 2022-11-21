package rip.alpha.hcf.team.command.impl.admin;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.team.Team;

public class ATeamTPCommand {
    @Command(names = {"teamadmin tp", "tadmin teleport", "ta tp"}, permission = "teamadmin.tp")
    public static void teamAdminTeleport(Player player, @Param(name = "team") Team team) {
        if (team.getHome() == null) {
            player.sendMessage(CC.RED + "That team must have a home for you to teleport");
            return;
        }

        player.teleport(team.getHome());
        player.sendMessage(CC.GREEN + "You have been teleported to the home of " + team.getName());
    }
}
