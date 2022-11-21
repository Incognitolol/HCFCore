package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.player.TeamFocusRemoveEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamUnfocusCommand {
    @Command(names = {"team unfocus", "t unfocus", "f unfocus", "faction unfocus"}, async = true)
    public static void teamUnfocusCommand(Player player) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        PlayerTeam.TeamMember member = team.getMember(player.getUniqueId());
        if (!member.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_CAPTAIN)) {
            player.sendMessage(CC.RED + "You are not high enough in the team hierarchy to focus for the team");
            return;
        }

        if (team.getFocusedTeamId() == null) {
            player.sendMessage(CC.RED + "You are not focusing a team.");
            return;
        }

        new TeamFocusRemoveEvent(team).call(HCF.getInstance());
        team.setFocusedTeamId(null);
        team.broadcast("", "&eTeam " + "&a" + player.getName() + "&e has unfocused the focused team", PlayerTeam.TeamMember.TEAM_MEMBER);
    }
}
