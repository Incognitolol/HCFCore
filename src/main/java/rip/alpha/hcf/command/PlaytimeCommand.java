package rip.alpha.hcf.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TimeUtil;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;

import java.util.UUID;

public class PlaytimeCommand {
    private static final String SELF_PLAYTIME_FORMAT = CC.translate("&6Playtime: &d%s");
    private static final String OTHER_PLAYTIME_FORMAT = CC.translate("&6Playtime of %s: &d%s");

    @Command(names = {"playtime", "pt"}, async = true)
    public static void playtimeCommand(CommandSender sender, @Param(name = "target", defaultValue = "self") UUID targetUUID) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfileOrLoad(targetUUID);
        boolean self = false;

        if (sender instanceof Player) {
            if (((Player) sender).getUniqueId().equals(targetUUID)) {
                self = true;
            }
        }

        int playTime = profile.getPlayTime();
        String name = self ? null : UUIDFetcher.getName(targetUUID);
        String formattedPlaytime = TimeUtil.formatIntoDetailedString(playTime);

        String message = self ? String.format(SELF_PLAYTIME_FORMAT, formattedPlaytime) : String.format(OTHER_PLAYTIME_FORMAT, name, formattedPlaytime);
        sender.sendMessage(message);
    }
}
