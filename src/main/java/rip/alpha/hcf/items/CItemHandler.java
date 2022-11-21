package rip.alpha.hcf.items;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import net.mcscrims.command.CommandFramework;
import net.mcscrims.libraries.Libraries;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.items.command.CItemGetItemComamnd;
import rip.alpha.hcf.items.impl.HealerWandItem;
import rip.alpha.hcf.items.impl.RepairArmourItem;
import rip.alpha.hcf.items.impl.RepairToolsItem;

import java.util.Arrays;
import java.util.Collections;

public class CItemHandler {

    @Getter private final Int2ObjectMap<CItem> items = new Int2ObjectOpenHashMap<>();

    public CItemHandler(HCF instance){
        this.items.put(0, new RepairArmourItem(new ItemBuilder(Material.PAPER).name(CC.RED + CC.BOLD + "Repair All").build(),
                Collections.singletonList(CC.GRAY + "Right click this item and it will repair all the armour you have equipped"),
                CItemRarity.COMMON, true));
        this.items.put(1, new HealerWandItem(new ItemBuilder(Material.STICK).name(CC.GREEN + CC.BOLD + "Healer Wand").build(),
                Arrays.asList(CC.RED + "You must have the bard class equipped to use this item!",
                        CC.GRAY + "Right-click the heal wand and all your teammates within 35 blocks will gain 2 hearts")
                , CItemRarity.EPIC, false));
        this.items.put(2, new RepairToolsItem(new ItemBuilder(Material.PAPER).name(CC.RED + CC.BOLD + "Repair Tools").build(),
                Collections.singletonList(CC.GRAY + "Right click this item and it will repair all the tools you have in your hotbar"),
                CItemRarity.COMMON, true));

        instance.getServer().getPluginManager().registerEvents(new CItemListener(), instance);
        CommandFramework commandFramework = Libraries.getInstance().getCommandFramework();
        commandFramework.registerClass(CItemGetItemComamnd.class);
    }

    public CItem getCrateItem(String displayName){
        for (CItem item : this.items.values()){
            if (item.getDisplayItem().getItemMeta().getDisplayName().equalsIgnoreCase(displayName)){
                return item;
            }
        }
        return null;
    }

    public boolean isCustomItem(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (!itemStack.hasItemMeta()) {
            return false;
        }
        if (!itemStack.getItemMeta().hasLore()) {
            return false;
        }

        for (String lore : itemStack.getItemMeta().getLore()) {
            if (lore.endsWith(CC.BLACK)) {
                return true;
            }
        }

        return false;
    }

}
