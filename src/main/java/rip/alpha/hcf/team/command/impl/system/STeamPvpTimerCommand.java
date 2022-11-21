package rip.alpha.hcf.team.command.impl.system;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.team.impl.SystemTeam;

public class STeamPvpTimerCommand {
    @Command(names = {"systemteam pvptimer", "systeam pvptimer", "steam pvptimer"}, async = true, permission = "op")
    public static void systemPvpTimerCommand(Player player, @Param(name = "team") SystemTeam team, @Param(name = "enabled") boolean enabled) {
        team.setDontAllowPvpTimer(enabled);
        team.setSave(true);
        String message = enabled ? (CC.GREEN + "You have made that team a pvptimer zone") : (CC.RED + "You have made that team a non-pvptimer zone");
        player.sendMessage(message);
    }
}
