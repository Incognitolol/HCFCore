package rip.alpha.hcf.items;

import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;

public class CItemListener implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (itemStack == null) {
            return;
        }

        if (!itemStack.hasItemMeta()) {
            return;
        }

        if (itemStack.getItemMeta().getLore() == null || itemStack.getItemMeta().getLore().isEmpty()) {
            return;
        }

        if (event.getAction().name().contains("RIGHT_")) {
            if (HCF.getInstance().getItemHandler().isCustomItem(itemStack)) {
                CItem item = HCF.getInstance().getItemHandler().getCrateItem(itemStack.getItemMeta().getDisplayName());
                if (item == null) {
                    return;
                }

                if (item.isRemove()) {
                    player.getInventory().remove(itemStack);
                }

                item.onClick(player);
            }
        }
    }
}
