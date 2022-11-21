package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.SimpleText;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamTopCommand {
    @Command(names = {"team top", "t top", "f top", "faction top"}, async = true)
    public static void teamTopCommand(CommandSender sender) {
        List<SimpleText> message = new ArrayList<>();
        message.add(new SimpleText("&7&m---------------------------------------"));
        message.add(new SimpleText("&6Team Top &7(Points)"));

        for (int i = 0; i < 10; i++) {
            UUID teamId = HCF.getInstance().getLeaderboardHandler().getTeam(i);

            if (teamId == null) {
                break;
            }

            PlayerTeam team = HCF.getInstance().getTeamHandler().getPlayerTeamById(teamId);

            if (team == null) {
                continue;
            }

            String text = CC.translate("&7" + (i + 1) + ". " + team.getDisplayName(sender) + " &7(" + team.getPoints() + ")");
            SimpleText simpleText = new SimpleText(text);
            simpleText.hover(CC.GOLD + "Click to view team information");
            simpleText.click("/f who " + team.getName());
            message.add(simpleText);
        }


        message.add(new SimpleText("&7&m---------------------------------------"));
        message.forEach(simpleText -> simpleText.send(sender));
    }
}
