package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;

public class TeamMapCommand {
    @Command(names = {"team map", "t map", "f map", "faction map"}, async = true)
    public static void teamMap(Player player) {
        HCF instance = HCF.getInstance();
        TeamProfile teamProfile = instance.getProfileHandler().getProfile(player);
        if (!teamProfile.isPillars()) {
            instance.getTeamHandler().showMap(player, teamProfile);
        } else {
            instance.getTeamHandler().hideMap(player, teamProfile);
        }
    }
}
