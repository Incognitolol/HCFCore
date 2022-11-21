package rip.alpha.hcf.deathban;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TimeUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.deathban.menu.DeathMenu;
import rip.alpha.hcf.profile.TeamProfile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DeathbanCommand {

    @Command(names = {"deathban revive", "staffrevive"}, permission = "deathban.revive", async = true)
    public static void deathbanReviveCommand(CommandSender sender, @Param(name = "target") UUID targetUUID) {
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfileOrLoad(targetUUID);

        if (!teamProfile.isDeathban()) {
            sender.sendMessage(CC.RED + "That player is not deathbanned.");
            return;
        }

        teamProfile.setDeathbanTime(-1);
        teamProfile.setSave(true);
        sender.sendMessage(CC.GREEN + "You have revived that player");
    }

    @Command(names = {"deathban check", "checkdeathban"}, permission = "deathban.check", async = true)
    public static void deathbanCheckCommand(CommandSender sender, @Param(name = "target") UUID targetUUID) {
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfileOrLoad(targetUUID);

        if (!teamProfile.isDeathban()) {
            sender.sendMessage(CC.RED + "That player is not deathban.");
            return;
        }

        sender.sendMessage(CC.GREEN + "That player is currently deathbanned for " +
                TimeUtil.formatLongIntoDetailedString(TimeUnit.MILLISECONDS.toSeconds(teamProfile.remainingDeathbanTime())));
    }

    @Command(names = "deaths", permission = "hcf.deaths", async = true)
    public static void deathsCommand(Player player, @Param(name = "target") UUID targetUUID) {
        new DeathMenu(targetUUID).openMenu(player);
    }
}
