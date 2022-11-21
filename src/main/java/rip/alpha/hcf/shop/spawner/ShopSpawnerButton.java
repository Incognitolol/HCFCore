package rip.alpha.hcf.shop.spawner;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.BalanceUtil;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class ShopSpawnerButton extends Button {

    private final ShopSpawnerEntry entry;
    private final boolean view;

    @Override
    public String getName(Player player) {
        return this.entry.getDisplayName();
    }

    @Override
    public List<String> getDescription(Player player) {
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (teamProfile == null) {
            return Collections.emptyList();
        }
        List<String> lore = new ArrayList<>();
        lore.add(" ");

        String priceColor = CC.GREEN;
        if (teamProfile.getBalance() < entry.getPrice()) {
            priceColor = CC.RED;
        }
        lore.add("&6Click to purchase &7x1 " + entry.getDisplayName() + " &6for " + priceColor + "$" + BalanceUtil.formatBalance(entry.getPrice()));

        return CC.translateLines(lore);
    }

    @Override
    public Material getMaterial(Player player) {
        return this.entry.getItemStack().getType();
    }

    @Override
    public byte getDamageValue(Player player) {
        return 0;
    }

    @Override
    public void clicked(Player player, int i, ClickType clickType, int i1) {
        if (view) {
            player.sendMessage(CC.RED + "You cannot use this menu in view mode.");
            return;
        }

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (teamProfile == null) {
            return;
        }

        int price = entry.getPrice();
        if (teamProfile.getBalance() < price) {
            player.sendMessage(CC.RED + "You cannot afford that item.");
            return;
        }

        teamProfile.setBalance(teamProfile.getBalance() - price);
        player.getInventory().addItem(this.entry.getItemStack().clone());
        player.sendMessage(CC.translate("&6You have purchased &7x1 " + this.entry.getDisplayName() + " &6for &a$" + price));
    }
}
