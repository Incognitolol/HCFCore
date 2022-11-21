package rip.alpha.hcf.game.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;

public class PalaceCommand {
    @Command(names = "palace create", async = true, permission = "hcf.palace.create")
    public static void palaceCreate(Player player) {
        KoTHCommands.createKOTH(player, "Palace", CC.D_PURPLE);
    }
}
