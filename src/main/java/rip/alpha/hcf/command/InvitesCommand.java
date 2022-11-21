package rip.alpha.hcf.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class InvitesCommand {

    @Command(names = {"invites"}, async = true)
    public static void invitesCommand(Player player) {
        Collection<PlayerTeam> playerTeams = HCF.getInstance().getTeamHandler().getPlayerTeams();
        playerTeams = playerTeams.stream().filter(playerTeam -> playerTeam.hasInvite(player.getUniqueId())).collect(Collectors.toSet());

        if (playerTeams.size() <= 0) {
            player.sendMessage(CC.RED + "You do not currently have an outstanding invites to any teams.");
            return;
        }

        int i = 0;
        List<String> message = new ArrayList<>();
        message.add("&7&m---------------------------------------");
        message.add("&9Team Invites &7(" + playerTeams.size() + ")");
        for (PlayerTeam team : playerTeams) {
            message.add("&7" + ++i + ". &r" + team.getDisplayName(player));
        }
        message.add("&7&m---------------------------------------");
        CC.translateLines(message).forEach(player::sendMessage);
    }

}
