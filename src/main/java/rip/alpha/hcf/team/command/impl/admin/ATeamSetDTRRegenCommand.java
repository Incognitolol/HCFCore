package rip.alpha.hcf.team.command.impl.admin;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TimeUtil;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class ATeamSetDTRRegenCommand {
    @Command(names = {"teamadmin setdtrregen", "tadmin setdtrregen", "ta setdtrregen"}, permission = "team.admin.setdtrregen", async = true)
    public static void setDTRRegenCommand(CommandSender sender, @Param(name = "team") PlayerTeam team, @Param(name = "thaw") String dtrRegen) {
        Long time = TimeUtil.parseTime(dtrRegen);

        if (time == null) {
            sender.sendMessage(CC.RED + "That time is an invalid format");
            return;
        }

        if (time <= 0) {
            time = 0L;
        }

        team.setRegen(System.currentTimeMillis() + time);
        team.setSave(true);
        sender.sendMessage(CC.GREEN + "You have successfully changed the dtr regen time of that team");
    }
}
