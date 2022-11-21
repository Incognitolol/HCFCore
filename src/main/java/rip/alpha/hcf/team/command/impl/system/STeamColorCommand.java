package rip.alpha.hcf.team.command.impl.system;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.team.impl.SystemTeam;

public class STeamColorCommand {
    @Command(names = {"systemteam color", "systeam color", "steam color"}, async = true, permission = "op")
    public static void systemTeamColor(Player player, @Param(name = "team") SystemTeam team, @Param(name = "color") String color) {
        team.setColor(CC.translate(color));
        team.setSave(true);
        player.sendMessage(CC.GREEN + "You have changed that color of that system team.");
    }
}
