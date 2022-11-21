package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.player.TeamFocusEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamFocusCommand {
    @Command(names = {"team focus", "t focus", "f focus", "faction focus"}, async = true)
    public static void teamFocusCommand(Player player, @Param(name = "targetTeam") PlayerTeam targetTeam) {
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

        if (targetTeam.getId().equals(team.getId())) {
            player.sendMessage(CC.RED + "You may not focus your own team.");
            return;
        }

        new TeamFocusEvent(team, targetTeam).call(HCF.getInstance());

        team.setFocusedTeamId(targetTeam.getId());
        team.broadcast("", "&eTeam " + "&d" + targetTeam.getName() + "&e has been focused by &d" + player.getName(), PlayerTeam.TeamMember.TEAM_MEMBER);
    }
}
