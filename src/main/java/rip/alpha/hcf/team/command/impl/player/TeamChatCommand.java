package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamChatCommand {
    @Command(names =
            {
                    "faction chat", "f chat", "team chat", "t chat",
                    "faction c", "f c", "team c", "t c"
            }, async = true)
    public static void teamChatCommand(Player player, @Param(name = "mode") PlayerTeam.TeamChatMode mode) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        PlayerTeam.TeamMember member = team.getMember(player.getUniqueId());
        PlayerTeam.TeamChatMode previousChatMode = member.getChatMode();

        if (mode == PlayerTeam.TeamChatMode.CAPTAIN) {
            if (!member.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_CAPTAIN)) {
                player.sendMessage(CC.RED + "You are not an officer to use this mode");
                if (previousChatMode == PlayerTeam.TeamChatMode.CAPTAIN) {
                    member.setChatMode(PlayerTeam.TeamChatMode.PUBLIC);
                }
                return;
            }
        }

        member.setChatMode(mode);
        team.setSave(true);
        player.sendMessage(CC.GREEN + "You are now using " + mode.getName().toLowerCase() + " chat");
    }
}
