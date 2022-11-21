package rip.alpha.hcf.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.timer.impl.GoppleTimer;

import java.util.UUID;

public class GoppleCommand {

    @Command(names = {"gopple add"}, async = true, permission = "op")
    public static void goppleAddCommand(Player player, @Param(name = "target", defaultValue = "self") UUID targetUUID) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfileOrLoad(targetUUID);
        new GoppleTimer(1, 0, 0).addTimer(profile);
        player.sendMessage(CC.GREEN + "You have added gopple timer to " + UUIDFetcher.getName(targetUUID));
    }

    @Command(names = {"gopple remove"}, async = true, permission = "op")
    public static void goppleRemoveCommand(Player player) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        profile.removeTimer(GoppleTimer.class);
        player.sendMessage(CC.GREEN + "Gopple timer removed.");
    }

    @Command(names = {"gopple"}, async = true)
    public static void goppleCommand(Player player) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        GoppleTimer goppleTimer = profile.getTimer(GoppleTimer.class);

        if (goppleTimer == null) {
            player.sendMessage(CC.RED + "You do not have an active gopple timer");
            return;
        }

        player.sendMessage(CC.GOLD + "Your gopple timer has " + goppleTimer.formatDetailedRemaining() + " remaining");
    }
}
