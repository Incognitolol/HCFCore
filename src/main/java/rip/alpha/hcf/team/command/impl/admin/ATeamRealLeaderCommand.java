package rip.alpha.hcf.team.command.impl.admin;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class ATeamRealLeaderCommand {
    @Command(names = "teamadmin getleader", permission = "team.admin.getleader", async = true)
    public static void getLeaderCommand(CommandSender sender, @Param(name = "team") PlayerTeam playerTeam){
        sender.sendMessage(CC.GREEN + playerTeam.getName() + "'s real leader is " + CC.YELLOW + playerTeam.getLeaderName());
    }
}
