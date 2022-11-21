package rip.alpha.hcf.shop.sell;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.menu.Button;
import net.mcscrims.libraries.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import rip.alpha.hcf.shop.ShopHandler;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ShopSellMenu extends Menu {

    private static final Button PLACEHOLDER = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 7, " ");

    private final ShopHandler shopHandler;
    private final boolean view;

    @Override
    public String getTitle(Player player) {
        return CC.GOLD + "Sell Shop";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();

        int row = 0;
        for (int i = 0; i < this.shopHandler.getShopSellEntries().size(); i++) {
            ShopSellEntry shopSellEntry = this.shopHandler.getShopSellEntries().get(i);
            ShopSellButton button = new ShopSellButton(shopSellEntry, view);
            int slot = i % 7;
            if (slot == 0) {
                row++;
            }
            buttonMap.put((row * 9) + (slot + 1), button);
        }


        for (int i = 0; i < (9 * (2 + row)); i++) {
            if (buttonMap.containsKey(i)) {
                continue;
            }
            buttonMap.put(i, PLACEHOLDER);
        }

        return buttonMap;
    }
}
