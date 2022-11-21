package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.UUID;

public class TeamLeaderCommand {
    @Command(names = {"team leader", "t leader", "f leader", "faction leader"}, async = true)
    public static void teamLeaderCommand(Player player, @Param(name = "target") UUID targetUUID) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        if (!team.isLeader(player.getUniqueId())) {
            player.sendMessage(CC.RED + "You are not leader to preform this command");
            return;
        }

        PlayerTeam.TeamMember targetMember = team.getMember(targetUUID);

        if (targetMember == null) {
            player.sendMessage(CC.RED + "That player is not in the team");
            return;
        }

        team.setLeader(targetUUID, UUIDFetcher.getName(targetUUID));
        team.broadcast("&2" + targetMember.getName() + "&7 has been promoted to &2" + targetMember.getRoleNameById());
        team.setSave(true);
    }
}
