package rip.alpha.hcf.items.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;

public class CItemGetItemComamnd {

    @Command(names = "citem getitem", permission = "hcf.citem.getitem",async = true)
    public static void getItemCommand(Player player, @Param(name = "index")int index) {
        HCF.getInstance().getItemHandler().getItems().get(index).giveItem(player);
    }
}
