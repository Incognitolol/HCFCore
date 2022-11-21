package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.shared.TeamRemoveClaimEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamUnclaimCommand {
    @Command(names = {"team unclaim", "t unclaim", "f unclaim", "faction unclaim"}, async = true)
    public static void teamUnclaim(Player player) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        if (team.getClaim() == null) {
            player.sendMessage(CC.RED + "Your team does not have a claim");
            return;
        }

        PlayerTeam.TeamMember member = team.getMember(player.getUniqueId());
        if (!member.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_COLEADER)) {
            player.sendMessage(CC.RED + "You are not high enough in the team hierarchy to remove a claim");
            return;
        }

        if (team.hasDTRFreeze()) {
            player.sendMessage(CC.RED + "You cannot remove your claim on dtr freeze");
            return;
        }

        ACuboid cuboid = team.getClaim();
        int price = teamHandler.calculatePrice(cuboid);
        team.setBalance(team.getBalance() + price);
        team.setHome(null);
        team.setClaim(null);
        team.setSave(true);
        team.broadcast("&7Team claim has been removed, your team has been refunded for &2$" + price);

        new TeamRemoveClaimEvent(player, team).call(HCF.getInstance());
    }
}
