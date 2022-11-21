package rip.alpha.hcf.team.command.impl.system;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.team.impl.SystemTeam;

public class STeamSafezoneCommand {
    @Command(names = {"systemteam safezone", "systeam safezone", "steam safezone"}, async = true, permission = "op")
    public static void systemSafezoneCommand(Player player, @Param(name = "team") SystemTeam team, @Param(name = "enabled") boolean enabled) {
        team.setSafezone(enabled);
        team.setSave(true);
        String message = enabled ? (CC.GREEN + "You have made that team a safezone") : (CC.RED + "You have made that team a warzone");
        player.sendMessage(message);
    }
}
