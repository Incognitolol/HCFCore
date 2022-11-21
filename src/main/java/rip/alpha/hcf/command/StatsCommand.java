package rip.alpha.hcf.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.statistics.ProfileStatTypes;

import java.util.UUID;

public class StatsCommand {
    private static final String SELF_STATS_FORMAT = CC.translate("&6Stats: ");
    private static final String OTHER_STATS_FORMAT = CC.translate("&6Stats of %s: ");

    @Command(names = {"stats", "ores"}, async = true)
    public static void stats(Player player, @Param(name = "target", defaultValue = "self") UUID targetUUID) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfileOrLoad(targetUUID);

        boolean self = player.getUniqueId().equals(targetUUID);
        String name = self ? null : UUIDFetcher.getName(targetUUID);
        String message = self ? SELF_STATS_FORMAT : String.format(OTHER_STATS_FORMAT, name);
        player.sendMessage(message);

        for (ProfileStatTypes statType : ProfileStatTypes.getValues()) { //do this so we get a sorted list
            int amount = profile.getStat(statType);
            player.sendMessage(CC.YELLOW + statType.getFormattedName() + CC.GRAY + amount);
        }
    }
}
