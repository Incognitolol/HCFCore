package rip.alpha.hcf.shop.spawner;

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
public class ShopSpawnerMenu extends Menu {

    private static final Button PLACEHOLDER = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 7, " ");

    private final ShopHandler shopHandler;
    private final boolean view;

    @Override
    public String getTitle(Player player) {
        return CC.GOLD + "Spawner Shop";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();

        for (int i = 0; i < this.shopHandler.getShopSpawnerEntries().size(); i++) {
            ShopSpawnerEntry shopSpawnerEntry = this.shopHandler.getShopSpawnerEntries().get(i);
            ShopSpawnerButton button = new ShopSpawnerButton(shopSpawnerEntry, this.view);
            buttonMap.put(shopSpawnerEntry.getIndex(), button);
        }

        for (int i = 0; i < 9 * 4; i++) {
            if (buttonMap.containsKey(i)) {
                continue;
            }
            buttonMap.put(i, PLACEHOLDER);
        }

        return buttonMap;
    }
}
