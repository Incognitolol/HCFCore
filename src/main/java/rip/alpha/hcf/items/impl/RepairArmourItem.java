package rip.alpha.hcf.items.impl;

import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.items.CItem;
import rip.alpha.hcf.items.CItemRarity;

import java.util.List;

public class RepairArmourItem extends CItem {

    public RepairArmourItem(ItemStack itemStack, List<String> lore, CItemRarity rarity, boolean remove) {
        super(itemStack, lore, rarity, remove);
    }

    @Override
    public void onClick(Player player) {
        for (ItemStack itemStack : player.getInventory().getArmorContents()){
            itemStack.setDurability((short) 0);
        }

        player.updateInventory();
        player.sendMessage(CC.GREEN + "You have repaired all the armor you currently have equipped");
    }
}
