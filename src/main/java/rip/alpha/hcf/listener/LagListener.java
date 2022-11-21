package rip.alpha.hcf.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemsEvent;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.settings.Setting;

import java.util.List;

public class LagListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        player.giveExp(event.getExpToDrop());
        event.setExpToDrop(0);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItems(BlockDropItemsEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        List<Item> itemList = event.getToDrop();
        if (itemList.isEmpty()) {
            return;
        }
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        boolean cancel = false;
        for (Item item : itemList) {
            if (item == null) {
                continue;
            }
            ItemStack itemStack = item.getItemStack();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }

            if (itemStack.getType() == Material.COBBLESTONE || itemStack.getType() == Material.STONE) {
                if (!teamProfile.getSetting(Setting.COBBLE)) {
                    continue;
                }
            } else if (PlayerListener.MOB_DROPS.contains(itemStack.getType())) {
                if (!teamProfile.getSetting(Setting.MOB_DROPS)) {
                    continue;
                }
            }

            player.getInventory().addItem(item.getItemStack());
            cancel = true;
        }

        if (cancel) {
            event.setCancelled(true);
        }
    }

    public boolean isEnabled() {
        return Bukkit.getOnlinePlayers().size() > 100;
    }
}
