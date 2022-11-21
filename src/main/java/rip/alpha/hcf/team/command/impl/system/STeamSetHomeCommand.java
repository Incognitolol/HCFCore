package rip.alpha.hcf.team.command.impl.system;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.team.impl.SystemTeam;

public class STeamSetHomeCommand {
    @Command(names = {"systeamteam sethome", "systeam sethome", "steam sethome"}, async = true, permission = "op")
    public static void systemTeamSetHomeCommand(Player player, @Param(name = "team") SystemTeam team) {
        team.setHome(player.getLocation());
        team.setSave(true);
        player.sendMessage(CC.GREEN + "You have updated that team system team homes location");
    }
}
