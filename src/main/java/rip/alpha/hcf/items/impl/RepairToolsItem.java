package rip.alpha.hcf.items.impl;

import net.mcscrims.libraries.util.CC;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.items.CItem;
import rip.alpha.hcf.items.CItemRarity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RepairToolsItem extends CItem {

    public RepairToolsItem(ItemStack itemStack, List<String> lore, CItemRarity rarity, boolean remove) {
        super(itemStack, lore, rarity, remove);
    }

    @Override
    public void onClick(Player player) {
        for (ItemStack itemStack : player.getOpenInventory().getBottomInventory().getContents()){
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;

            if (itemStack.getType().name().contains("_SWORD")
                    || itemStack.getType().name().contains("_SPADE")
                    || itemStack.getType().name().contains("_PICKAXE")
                    || itemStack.getType().name().contains("_AXE")
                    || itemStack.getType().name().contains("_HOE")) {
                itemStack.setDurability((short) 0);
            }
        }

        player.updateInventory();
        player.sendMessage(CC.GREEN + "You have repaired all the tools in your hotbar");
    }
}
