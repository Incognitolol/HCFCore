package rip.alpha.hcf.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.deathban.Death;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.TeamProfileHandler;

public class InvRestoreCommand {

    private static final TeamProfileHandler PROFILE_HANDLER = HCF.getInstance().getProfileHandler();

    @Command(names = "invrestore", permission = "hcf.invrestore", async = true)
    public static void invRestore(CommandSender sender, @Param(name = "target") Player player) {
        TeamProfile targetProfile = PROFILE_HANDLER.getProfile(player);

        if (targetProfile == null) {
            sender.sendMessage(CC.RED + "That player must be online.");
            return;
        }

        if (targetProfile.getRecentDeaths().isEmpty()){
            sender.sendMessage(CC.RED + "Couldn't find an inventory for that player.");
            return;
        }

        Death death = targetProfile.getRecentDeaths().get(0);
        if (death == null) {
            sender.sendMessage(CC.RED + "Couldn't find an inventory for that player.");
            return;
        }

        Player target = targetProfile.toPlayer();
        targetProfile.loadDeathInventory(death);

        sender.sendMessage(CC.GREEN + "You have given " + player.getInventory() + " their inventory back.");
        target.sendMessage(CC.GREEN + "You have been given your inventory back.");
    }
}
