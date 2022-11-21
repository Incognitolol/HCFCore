package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamInvitesCommand {
    @Command(names = {"team invites", "t invites", "f invites", "faction invites"}, async = true)
    public static void teamInvitesCommand(Player player) {
        TeamHandler teamHandler =HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        PlayerTeam.TeamMember member = team.getMember(player.getUniqueId());
        if (!member.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_CAPTAIN)) {
            player.sendMessage(CC.RED + "You are not high enough in the team hierarchy to withdraw from the teams balance");
            return;
        }

        Set<PlayerTeam.TeamInviteEntry> inviteEntries = new HashSet<>(team.getInviteEntries());
        inviteEntries.removeIf(PlayerTeam.TeamInviteEntry::isExpired);

        if (inviteEntries.size() <= 0) {
            player.sendMessage(CC.RED + "Your team currently doesn't have any outstanding invites");
            return;
        }

        int i = 0;
        List<String> message = new ArrayList<>();
        message.add("&7&m---------------------------------------");
        message.add("&6Team Invites &7(" + inviteEntries.size() + ")");
        for (PlayerTeam.TeamInviteEntry entry : inviteEntries) {
            message.add("&7" + ++i + ". &r" + UUIDFetcher.getName(entry.getUuid()));
        }
        message.add("&7&m---------------------------------------");
        CC.translateLines(message).forEach(player::sendMessage);
    }
}
