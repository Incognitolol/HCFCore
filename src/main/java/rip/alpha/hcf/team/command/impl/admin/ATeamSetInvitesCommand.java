package rip.alpha.hcf.team.command.impl.admin;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class ATeamSetInvitesCommand {
    @Command(names = {"teamadmin setinvites", "tadmin setinvites", "ta setinvites"}, permission = "team.admin.setinvites", async = true)
    public static void setInvitesCommand(CommandSender sender, @Param(name = "team") PlayerTeam team, @Param(name = "amount") int amount) {
        if (!HCF.getInstance().getConfiguration().isUseMaxInvites()) {
            sender.sendMessage(CC.RED + "Max invites not enabled on this map!");
            return;
        }
        team.setInvites(amount);
        team.setSave(true);
        sender.sendMessage(CC.GREEN + "You have set that teams lives to " + amount);
    }
}
