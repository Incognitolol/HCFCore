package rip.alpha.hcf.team.command.impl.system;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.team.impl.SystemTeam;

public class STeamEnderpearlCommand {
    @Command(names = {"systemteam enderpearl", "systeam enderpearl", "steam enderpearl"}, async = true, permission = "op")
    public static void systemEnderpearlCommand(Player player, @Param(name = "team") SystemTeam team, @Param(name = "enabled") boolean enabled) {
        team.setEnderpearl(enabled);
        team.setSave(true);
        String message = enabled ? (CC.GREEN + "You have made that team an enderpearl zone") : (CC.RED + "You have made that team a non-pearl zone");
        player.sendMessage(message);
    }
}
