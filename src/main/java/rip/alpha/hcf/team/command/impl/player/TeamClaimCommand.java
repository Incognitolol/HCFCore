package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.listener.TeamClaimListener;

public class TeamClaimCommand {
    @Command(names = {"team claim", "t claim", "f claim", "faction claim"}, async = true)
    public static void teamClaimCommand(Player player) {
        if (HCF.getInstance().getConfiguration().isKitmap()) {
            player.sendMessage(CC.RED + "You cannot claim on kitmap.");
            return;
        }

        if (HCF.getInstance().isEotw()){
            player.sendMessage(CC.RED + "You cannot claim during eotw.");
            return;
        }

        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        PlayerTeam.TeamMember member = team.getMember(player.getUniqueId());
        if (!member.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_COLEADER)) {
            player.sendMessage(CC.RED + "You are not high enough in the team hierarchy to create a claim");
            return;
        }

        if (!HCF.getInstance().getBorderHandler().inClaimRadius(player.getLocation())) {
            player.sendMessage(CC.RED + "You have to be " + HCF.getInstance().getBorderHandler().getClaimRadius() + " blocks out to begin claiming");
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(CC.RED + "Your inventory is full, please remove an item from your inventory to start claiming");
            return;
        }

        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        profile.setSelectedLocations(new Location[2]);
        profile.setClaimingFor(team.getId());

        if (!player.getInventory().contains(TeamClaimListener.CLAIM_WAND)) {
            player.getInventory().addItem(TeamClaimListener.CLAIM_WAND);
            player.updateInventory();
        }

        teamHandler.showMap(player, profile);
        player.sendMessage(CC.YELLOW + "You are now claiming for " + CC.GOLD + team.getName());
    }
}
