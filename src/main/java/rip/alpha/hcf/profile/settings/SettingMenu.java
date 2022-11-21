package rip.alpha.hcf.profile.settings;

import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.menu.Button;
import net.mcscrims.libraries.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingMenu extends Menu {

    private static final Button PLACEHOLDER = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 15, " ");

    {
        setNoncancellingInventory(true);
        setUpdateAfterClick(true);
    }

    @Override
    public String getTitle(Player player) {
        return CC.GOLD + "Settings Menu";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();
        List<Setting> settings = Setting.getSortedSettings();

        int row = 0;
        for (int i = 0; i < settings.size(); i++) {
            Setting setting = settings.get(i);
            SettingButton button = new SettingButton(setting);
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
