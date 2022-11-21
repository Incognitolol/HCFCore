package rip.alpha.hcf.team.command.impl.system;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.impl.SystemTeam;
import rip.alpha.hcf.team.listener.TeamClaimListener;

public class STeamClaimCommand {
    @Command(names = {"systemteam claim", "systeam claim", "steam claim"}, async = true, permission = "op")
    public static void systemTeamClaim(Player player, @Param(name = "team") SystemTeam team) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);

        if (profile.getClaimingFor() != null) {
            player.sendMessage(CC.RED + "You are already claiming for another team");
            return;
        }

        profile.setSelectedLocations(new Location[2]);
        profile.setClaimingFor(team.getId());

        if (!player.getInventory().contains(TeamClaimListener.CLAIM_WAND)) {
            player.getInventory().addItem(TeamClaimListener.CLAIM_WAND);
        }

        HCF.getInstance().getTeamHandler().showMap(player, profile);
        player.sendMessage(CC.GREEN + "You are now claiming for " + team.getName());
    }
}
