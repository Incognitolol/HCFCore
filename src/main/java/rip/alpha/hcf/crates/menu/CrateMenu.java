package rip.alpha.hcf.crates.menu;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.RoundingUtil;
import net.mcscrims.libraries.util.menu.Button;
import net.mcscrims.libraries.util.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import rip.alpha.hcf.crates.Crate;
import rip.alpha.hcf.crates.CrateItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class CrateMenu extends Menu {

    {
        setNoncancellingInventory(false);
    }

    private final Crate crate;

    @Override
    public String getTitle(Player player) {
        return CC.BLUE + this.crate.getName() + " Crate";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();

        if (!this.crate.getItems().isEmpty()) {

            int totalWeight = this.crate.getTotalWeight();

            for (CrateItem crateItem : this.crate.getItems().values()) {
                ItemStack displayItem = crateItem.getDisplayItem().clone();
                displayItem.setAmount(Math.max(1, displayItem.getAmount()));

                ItemMeta meta = displayItem.getItemMeta();

                List<String> lore;
                if (meta.getLore() == null) {
                    lore = new ArrayList<>();
                } else {
                    lore = meta.getLore();
                }
                lore.add(" ");

                int weight = crateItem.getWeight();
                double chance = ((double) weight / (double) totalWeight) * 100.0;

                String chanceMessage = RoundingUtil.round(chance, 2) + "%" + CC.GRAY + CC.ITALIC + " (" + weight + "/" + totalWeight + ")";
                lore.add(CC.GOLD + "Chance: " + CC.YELLOW + chanceMessage);
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
                buttonMap.put(crateItem.getId(), Button.fromItem(displayItem));
            }
        }

        return buttonMap;
    }
}
