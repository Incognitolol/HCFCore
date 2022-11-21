package rip.alpha.hcf.team.command.impl.admin;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class ATeamThawCommand {
    @Command(names = {"teamadmin thaw", "tadmin thaw", "ta thaw"}, permission = "team.admin.setthaw", async = true)
    public static void setThawCommand(CommandSender sender, @Param(name = "team") PlayerTeam team) {
        team.setRegen(System.currentTimeMillis());
        team.setSave(true);
        sender.sendMessage(CC.GREEN + "You have successfully changed thawed that teams dtr regen");
    }
}
