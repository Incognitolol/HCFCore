package rip.alpha.hcf.enchantment;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;

@RequiredArgsConstructor
public class EnchantmentLimiterListener implements Listener {

    private final EnchantmentHandler enchantmentHandler;

    @EventHandler(ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        Object2IntMap<Enchantment> map = new Object2IntOpenHashMap(event.getEnchantsToAdd());

        map.entrySet().iterator().forEachRemaining(entry -> {
            Enchantment enchantment = entry.getKey();
            int value = entry.getValue();
            EnchantmentEntry enchantmentEntry = this.enchantmentHandler.getEnchantmentEntry(enchantment);
            if (enchantmentEntry != null) {
                int max = enchantmentEntry.getMaxLevel();
                int val = Math.min(max, value);
                if (val <= 0) {
                    map.remove(enchantment);
                } else {
                    map.put(enchantment, val);
                }
            } else {
                map.put(enchantment, value);
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        ItemStack itemStack = event.getEntity().getItemStack();

        if (itemStack == null) {
            return;
        }
        if (HCF.getInstance().getCrateHandler().isKothItem(itemStack)) {
            return;
        }

        Object2IntMap<Enchantment> enchantments = new Object2IntOpenHashMap<>(itemStack.getEnchantments());

        enchantments.entrySet().iterator().forEachRemaining(entry -> {
            Enchantment enchantment = entry.getKey();
            int value = entry.getValue();
            EnchantmentEntry enchantmentEntry = this.enchantmentHandler.getEnchantmentEntry(enchantment);
            if (enchantmentEntry != null && enchantmentEntry.overLimit(value)) {
                int max = enchantmentEntry.getMaxLevel();
                int val = Math.min(max, value);
                if (val != value) {
                    if (val <= 0) {
                        itemStack.removeEnchantment(enchantment);
                    } else {
                        itemStack.addUnsafeEnchantment(enchantment, val);
                    }
                }
            }
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (inventory == null) {
            return;
        }
        if (!(inventory instanceof AnvilInventory)) {
            return;
        }

        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null) {
            return;
        }

        if (HCF.getInstance().getCrateHandler().isKothItem(itemStack)) {
            event.setCancelled(true);
            return;
        }

        Object2IntMap<Enchantment> enchantments = new Object2IntOpenHashMap<>(itemStack.getEnchantments());
        enchantments.entrySet().iterator().forEachRemaining(entry -> {
            Enchantment enchantment = entry.getKey();
            int value = entry.getValue();
            EnchantmentEntry enchantmentEntry = this.enchantmentHandler.getEnchantmentEntry(enchantment);
            if (enchantmentEntry != null && enchantmentEntry.overLimit(value)) {
                int max = enchantmentEntry.getMaxLevel();
                int val = Math.min(max, value);
                if (val != value) {
                    if (val <= 0) {
                        itemStack.removeEnchantment(enchantment);
                    } else {
                        itemStack.addUnsafeEnchantment(enchantment, val);
                    }
                }
            }
        });
    }
}
