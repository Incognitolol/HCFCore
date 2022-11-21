package rip.alpha.hcf.command;

import net.mcscrims.command.annotation.Command;
import org.bukkit.entity.Player;
import rip.alpha.hcf.shop.view.ViewShopsMenu;

public class ShopsCommand {

    @Command(names = {"shop", "viewshops"})
    public static void shopsCommand(Player player) {
        new ViewShopsMenu().openMenu(player);
    }
}
