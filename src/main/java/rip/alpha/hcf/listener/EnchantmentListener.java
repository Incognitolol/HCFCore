package rip.alpha.hcf.listener;

import net.mcscrims.libraries.util.CC;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class EnchantmentListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() == null) {
            return;
        }
        if (event.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getItem() != null) {
                if (event.getItem().getType() == Material.ENCHANTED_BOOK) {
                    event.getItem().setType(Material.BOOK);
                    event.getPlayer().sendMessage(CC.GREEN + "You reverted this book to its original form!");
                    event.setCancelled(true);
                }
            }
        }
    }

//    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
//    public void onInventoryClick(InventoryClickEvent event) {
//        HumanEntity entity = event.getWhoClicked();
//        if (!(entity instanceof Player)) return;
//        Inventory inventory = event.getInventory();
//        if (!(inventory instanceof AnvilInventory)) return;
//        InventoryView view = event.getView();
//        int rawSlot = event.getRawSlot();
//        if (rawSlot != view.convertSlot(rawSlot)) return;
//        if (rawSlot != 2) return;
//        ItemStack item = event.getCurrentItem();
//        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
//            ItemMeta itemMeta = item.getItemMeta();
//            if (itemMeta.hasDisplayName()) {
//                String displayName = itemMeta.getDisplayName();
//                itemMeta.setDisplayName(displayName.replaceAll("[^a-zA-Z0-9]", ""));
//                item.setItemMeta(itemMeta);
//            }
//        }
//    }
}
