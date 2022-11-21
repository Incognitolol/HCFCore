package rip.alpha.hcf.deathban.menu.button;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.menu.Button;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.deathban.Death;
import rip.alpha.hcf.game.schedule.GameScheduleEntry;
import rip.alpha.hcf.profile.TeamProfile;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class DeathButton extends Button {

    private final Death death;

    @Override
    public String getName(Player player) {
        return CC.GRAY + CC.ITALIC + GameScheduleEntry.DATE_PARSE_FORMAT.format(death.getDate());
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> lore = new ArrayList<>();
        String killer = death.getKiller() == null ? "None" : UUIDFetcher.getName(death.getKiller());
        lore.add(CC.YELLOW + "Killer: " + CC.WHITE + killer);
        lore.add(CC.YELLOW + "Killed: " + CC.WHITE + UUIDFetcher.getName(death.getDead()));
        lore.add(CC.YELLOW + "Location: " + CC.WHITE + death.getLocation().getX() + "," + death.getLocation().getZ());
        lore.add(" ");
        lore.add(CC.RED + "Left click to teleport to their death location");
        lore.add(CC.GREEN + "Right click to restore death inventory to the player");
        return lore;
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.SKULL_ITEM;
    }

    @Override
    public byte getDamageValue(Player player) {
        return 0;
    }

    @Override
    public void clicked(Player player, int i, ClickType clickType, int i1) {
        if (clickType == ClickType.RIGHT) {

            if (this.death.getDeathInventory() == null) {
                player.sendMessage(CC.RED + "Couldn't find an inventory for that player.");
                return;
            }

            TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(this.death.getDead());

            if (teamProfile == null) {
                player.sendMessage(CC.RED + "That player must be online");
                return;
            }

            teamProfile.loadDeathInventory(death);
            player.closeInventory();
            player.sendMessage(CC.GREEN + "You have restored their inventory");
        } else if (clickType == ClickType.LEFT) {
            player.teleport(this.death.getLocation().toBukkit());
            player.sendMessage(CC.GREEN + "You have been teleported to their last location");
        }
    }
}
