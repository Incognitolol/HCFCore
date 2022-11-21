package rip.alpha.hcf.team.command.impl.system;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.game.impl.KoTHGame;
import rip.alpha.hcf.team.impl.SystemTeam;

public class STeamLinkKoTHCommand {
    @Command(names = {"systemteam linkKoth", "systeam linkKoth", "steam linkKoth"}, async = true, permission = "op")
    public static void systemTeamLinkKoth(Player player, @Param(name = "team") SystemTeam team, @Param(name = "game") KoTHGame koTHGame) {
        team.setLinkedGameId(koTHGame.getId());
        team.setSave(true);
        player.sendMessage(CC.GREEN + "You have linked that system team to that game!");
    }
}
