package rip.alpha.hcf.shop.buy;

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
public class ShopBuyButton extends Button {

    private final ShopBuyEntry entry;
    private final boolean view;

    @Override
    public String getName(Player player) {
        return entry.getDisplayName();
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

        String formattedPrice = BalanceUtil.formatBalance(entry.getPrice());
        lore.add("&6Click to purchase &7x" + entry.getAmount() + " "
                + entry.getDisplayName() + " &6for " + priceColor + "$" + formattedPrice);

        return CC.translateLines(lore);
    }

    @Override
    public Material getMaterial(Player player) {
        return entry.getMaterial();
    }

    @Override
    public byte getDamageValue(Player player) {
        return (byte) entry.getItemStack().getDurability();
    }

    @Override
    public int getAmount(Player player) {
        return this.entry.getAmount();
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

        String formattedPrice = BalanceUtil.formatBalance(price);
        player.sendMessage(CC.translate("&6You have purchased &7x" + this.entry.getAmount() + " " + this.entry.getDisplayName() + " &6for &a$" + formattedPrice));
    }
}
