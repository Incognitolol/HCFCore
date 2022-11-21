package rip.alpha.hcf.deathban.menu;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.menu.Button;
import net.mcscrims.libraries.util.menu.Menu;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.deathban.Death;
import rip.alpha.hcf.deathban.menu.button.DeathButton;
import rip.alpha.hcf.profile.TeamProfile;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class DeathMenu extends Menu {
    private final UUID target;

    @Override
    public String getTitle(Player player) {
        return CC.GOLD + UUIDFetcher.getName(this.target) + "'s Deaths";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();

        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfileOrLoad(this.target);

        int i = 0;
        for (Death death : profile.getRecentDeaths()) {
            buttons.put(i, new DeathButton(death));
            i++;
        }

        return buttons;
    }
}
