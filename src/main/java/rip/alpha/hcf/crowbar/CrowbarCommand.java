package rip.alpha.hcf.crowbar;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;

public class CrowbarCommand {

    @Command(names = {"crowbar give"}, permission = "op")
    public static void crowbarGiveCommand(CommandSender sender, @Param(name = "target", defaultValue = "self") Player player) {
        CrowbarItem crowbarItem = HCF.getInstance().getCrowbarHandler().createDefaultCrowbarItem();
        ItemStack itemStack = crowbarItem.toItemStack();
        player.getInventory().addItem(itemStack);
        sender.sendMessage(CC.GREEN + "You have given " + player.getName() + " a crowbar");
    }
}
