package rip.alpha.hcf.team.command.impl.admin;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class ATeamSetDTRCommand {
    @Command(names = {"teamadmin setdtr", "tadmin setdtr", "ta setdtr"}, permission = "team.admin.setdtr", async = true)
    public static void setDTRCommand(CommandSender sender, @Param(name = "team") PlayerTeam team, @Param(name = "dtr") double dtr) {
        team.setDtr(dtr);
        team.setSave(true);
        sender.sendMessage(CC.GREEN + "You have successfully changed the dtr of that team");
    }
}
