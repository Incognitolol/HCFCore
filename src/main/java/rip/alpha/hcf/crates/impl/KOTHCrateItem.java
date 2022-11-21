package rip.alpha.hcf.crates.impl;

import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.crates.CrateItem;

public class KOTHCrateItem extends CrateItem {

    public KOTHCrateItem(ItemStack itemStack, int weight, int id, int typeId) {
        super(itemStack, weight, id, typeId);
    }

    @Override
    public void giveItem(Player player) {
        ItemStack itemStack = new ItemBuilder(this.getDisplayItem().clone()).lore(CC.GRAY + "Koth Item.").build();

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
            player.sendMessage(CC.RED + "Your inventory is full, your item has been dropped on the floor.");
            return;
        }

        player.getInventory().addItem(itemStack);
    }
}
