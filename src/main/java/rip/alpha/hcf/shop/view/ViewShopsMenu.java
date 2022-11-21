package rip.alpha.hcf.shop.view;

import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.menu.Button;
import net.mcscrims.libraries.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ViewShopsMenu extends Menu {

    private static final Button PLACEHOLDER = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 7, " ");

    @Override
    public String getTitle(Player player) {
        return CC.GOLD + "View Shops";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();

        buttonMap.put(11, new ViewShopsButton(CC.translate("&5Spawner Shop"), Material.MOB_SPAWNER));
        buttonMap.put(13, new ViewShopsButton(CC.translate("&aBuy Shop"), Material.SUGAR_CANE));
        buttonMap.put(15, new ViewShopsButton(CC.translate("&cSell Shop"), Material.DIAMOND));

        for (int i = 0; i < 9 * 3; i++) {
            if (buttonMap.containsKey(i)) {
                continue;
            }
            buttonMap.put(i, PLACEHOLDER);
        }

        return buttonMap;
    }
}
