package rip.alpha.hcf.team.command.impl.admin;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.UUID;

public class ATeamForceLeaderCommand {
    @Command(names = "teamadmin forceleader", permission = "team.admin.forceleader", async = true)
    public static void forceLeaderCommand(CommandSender sender, @Param(name = "target")UUID targetUUID, @Param(name = "team")PlayerTeam playerTeam) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();

        PlayerTeam.TeamMember targetMember = playerTeam.getMember(targetUUID);

        if (targetMember == null) {
            sender.sendMessage(CC.RED + "That player is not in the team");
            return;
        }

        playerTeam.setLeader(targetUUID, UUIDFetcher.getName(targetUUID));
        playerTeam.broadcast("&2" + targetMember.getName() + "&7 has been promoted to &2" + targetMember.getRoleNameById());
        playerTeam.setSave(true);
    }
}
