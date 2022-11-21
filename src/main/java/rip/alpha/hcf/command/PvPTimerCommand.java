package rip.alpha.hcf.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.timer.impl.PvPTimer;

import java.util.UUID;

public class PvPTimerCommand {

    @Command(names = {"pvp give", "pvptimer give"}, permission = "op", async = true)
    public static void pvpTimerCommand(CommandSender sender, @Param(name = "target") UUID uuid, @Param(name = "mins", defaultValue = "30") int mins) {
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfileOrLoad(uuid);
        if (teamProfile == null) {
            return;
        }
        teamProfile.addTimer(new PvPTimer(0, mins, 0));
        sender.sendMessage(CC.GREEN + "You have given " + UUIDFetcher.getCachedName(uuid) + " pvptimer for " + mins + " mins");
    }

    @Command(names = {"pvp enable"}, async = true)
    public static void pvpEnableCommand(Player player) {
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (teamProfile == null) {
            return;
        }

        if (!teamProfile.hasTimer(PvPTimer.class)) {
            player.sendMessage(CC.RED + "You do not have pvp timer.");
            return;
        }

        teamProfile.removeTimer(PvPTimer.class);
        player.sendMessage(CC.GREEN + "You have enabled pvp!");
    }

}
