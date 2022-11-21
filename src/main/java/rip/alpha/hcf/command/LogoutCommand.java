package rip.alpha.hcf.command;

import net.mcscrims.command.annotation.Command;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.timer.impl.LogoutTimer;

public class LogoutCommand {

    @Command(names = {"logout", "safelogout"}, async = true)
    public static void logoutCommand(Player player) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        profile.addTimer(new LogoutTimer(HCF.getInstance().getConfiguration().getLogoutTimer()));
    }
}
