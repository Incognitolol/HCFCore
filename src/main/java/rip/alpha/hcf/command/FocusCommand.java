package rip.alpha.hcf.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.UUID;

public class FocusCommand {

    private static final TeamHandler TEAM_HANDLER = HCF.getInstance().getTeamHandler();

    @Command(names = {"focus"}, async = true)
    public static void onFocusCommand(Player player, @Param(name = "target") Player target) {
        PlayerTeam team = TEAM_HANDLER.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        if (team.getMember(target.getUniqueId()) != null) {
            player.sendMessage(CC.RED + "You may not focus people in your team.");
            return;
        }

        team.getFocusedPlayers().add(target.getUniqueId());
        team.broadcast("", "&d" + target.getName() + "&e has been focused by &d" + player.getName(), PlayerTeam.TeamMember.TEAM_MEMBER);
    }

    @Command(names = {"unfocus"}, async = true)
    public static void onUnfocusCommand(Player player, @Param(name = "target") UUID targetUUID) {
        PlayerTeam team = TEAM_HANDLER.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        PlayerTeam.TeamMember member = team.getMember(player.getUniqueId());
        if (!member.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_CAPTAIN)) {
            player.sendMessage(CC.RED + "You are not high enough in the team hierarchy to focus for the team");
            return;
        }

        if (!team.isFocused(targetUUID)) {
            player.sendMessage(CC.RED + "That player is not focused");
            return;
        }

        team.getFocusedPlayers().remove(targetUUID);
        team.broadcast("", "&d" + UUIDFetcher.getName(targetUUID) + "&e has been un-focused by &d" + player.getName(), PlayerTeam.TeamMember.TEAM_MEMBER);
    }
}
