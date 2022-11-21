package rip.alpha.hcf.team.command.impl.admin;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.command.TeamCommandConstants;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.UUID;

public class ATeamForceDemoteCommand {
    @Command(names = {"teamadmin forcedemote", "tadmin forcedemote", "ta forcedemote"}, permission = "teamadmin.forcedemote", async = true)
    public static void teamAdminForceDemote(Player player, @Param(name = "target") UUID targetUUID, @Param(name = "team") PlayerTeam playerTeam) {

        PlayerTeam.TeamMember targetMember = playerTeam.getMember(targetUUID);

        if (targetMember == null) {
            player.sendMessage(CC.RED + "That player is not in the team");
            return;
        }

        int targetRole = targetMember.getRole() - 1;
        if (targetRole < 0) {
            player.sendMessage(CC.RED + "You cannot demote this player any further");
            return;
        }

        playerTeam.demoteMember(targetMember);
        playerTeam.broadcast("&a" + targetMember.getName() + " &ehas been demoted to &a" + targetMember.getRoleNameById());
        player.sendMessage(CC.GREEN + "You have demoted " + UUIDFetcher.getName(targetUUID) + ".");
        playerTeam.setSave(true);
    }
}
