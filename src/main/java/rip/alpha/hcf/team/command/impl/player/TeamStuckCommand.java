package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.timer.impl.StuckTimer;

public class TeamStuckCommand {
    @Command(names = {"team stuck", "t stuck", "f stuck", "faction stuck"}, async = true)
    public static void teamStuck(Player player) {
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);

        StuckTimer timer = new StuckTimer(HCF.getInstance().getConfiguration().getStuckTimer(), player.getLocation());
        teamProfile.addTimer(timer);

        player.sendMessage(CC.YELLOW + "We are finding a safe location for you, you will be teleported shortly.");
    }
}
