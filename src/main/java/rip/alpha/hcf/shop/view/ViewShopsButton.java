package rip.alpha.hcf.shop.view;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.ItemBuilder;
import net.mcscrims.libraries.util.menu.Button;
import net.minecraft.server.v1_7_R4.EnchantmentGlow;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.shop.buy.ShopBuyMenu;
import rip.alpha.hcf.shop.sell.ShopSellMenu;
import rip.alpha.hcf.shop.spawner.ShopSpawnerMenu;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class ViewShopsButton extends Button {

    public final static EnchantmentGlow ENCHANTMENT_GLOW = new EnchantmentGlow(100);

    private final String name;
    private final Material material;

    @Override
    public String getName(Player player) {
        return name;
    }

    @Override
    public List<String> getDescription(Player player) {
        return Collections.emptyList();
    }

    @Override
    public Material getMaterial(Player player) {
        return this.material;
    }

    @Override
    public byte getDamageValue(Player player) {
        return 0;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        ItemStack itemStack = super.getButtonItem(player);

        if (this.material == Material.MOB_SPAWNER || this.material == Material.SUGAR_CANE || this.material == Material.DIAMOND) {
            ItemBuilder builder = new ItemBuilder(itemStack);
            builder.enchantment(ENCHANTMENT_GLOW);
            return builder.build();
        }

        return itemStack;
    }

    @Override
    public void clicked(Player player, int i, ClickType clickType, int i1) {
        if (this.material == Material.MOB_SPAWNER) {
            new ShopSpawnerMenu(HCF.getInstance().getShopHandler(), true).openMenu(player);
        } else if (this.material == Material.SUGAR_CANE) {
            new ShopBuyMenu(HCF.getInstance().getShopHandler(), true).openMenu(player);
        } else if (this.material == Material.DIAMOND) {
            new ShopSellMenu(HCF.getInstance().getShopHandler(), true).openMenu(player);
        }
    }
}
