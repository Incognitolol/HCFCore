package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.team.Team;

public class TeamInfoCommand {
    @Command(names =
            {
                    "team info", "t info", "f info", "faction info",
                    "team who", "t who", "f who", "faction who",
                    "team i", "t i", "f i", "faction i",
                    "team show", "t show", "f show", "faction show"
            }, async = true)
    public static void teamInfoCommand(CommandSender sender, @Param(name = "team", defaultValue = "self", wildcard = true) Team team) {
        team.sendTeamInfo(sender);
    }
}
