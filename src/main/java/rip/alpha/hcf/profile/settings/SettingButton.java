package rip.alpha.hcf.profile.settings;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SettingButton extends Button {

    private final Setting setting;

    @Override
    public String getName(Player player) {
        return CC.translate(setting.getDisplayName());
    }

    @Override
    public List<String> getDescription(Player player) {
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        List<String> lore = new ArrayList<>();
        boolean enabled = teamProfile.getSetting(this.setting);

        lore.add(" ");
        lore.add(CC.GRAY + setting.getDescription());
        lore.add(" ");
        lore.add(" " + (enabled ? "&6" + CC.ARROW_LEFT + " " : "") + CC.YELLOW + setting.getEnabledMessage());
        lore.add(" " + (!enabled ? "&6" + CC.ARROW_LEFT + " " : "") + CC.YELLOW + setting.getDisabledMessage());

        return CC.translateLines(lore);
    }

    @Override
    public Material getMaterial(Player player) {
        return setting.getMaterial();
    }

    @Override
    public byte getDamageValue(Player player) {
        return 0;
    }

    @Override
    public void clicked(Player player, int i, ClickType clickType, int i1) {
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        teamProfile.setSetting(this.setting, !teamProfile.getSetting(setting));
        teamProfile.setSave(true);
    }
}
