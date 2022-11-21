package rip.alpha.hcf.items;

import lombok.Data;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

@Data
public abstract class CItem {
    private ItemStack displayItem;
    private CItemRarity rarity;
    private boolean remove;

    public CItem(ItemStack displayItem, List<String> lore, CItemRarity rarity, boolean remove) {
        ItemBuilder displayBuilder = new ItemBuilder(displayItem.clone()).lore(lore);
        displayBuilder.lore(rarity.getDisplayName());

        this.displayItem = displayBuilder.build();

        this.rarity = rarity;
        this.remove = remove;
    }

    public void giveItem(Player player) {
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), this.getDisplayItem());
            player.sendMessage(CC.RED + "Your inventory is full, your item has been dropped on the floor.");
            return;
        }

        player.getInventory().addItem(this.displayItem.clone());
    }

    public abstract void onClick(Player player);

}
