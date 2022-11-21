package rip.alpha.hcf.shop.buy;

import lombok.Getter;
import net.mcscrims.libraries.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
public class ShopBuyEntry {

    private final String displayName;
    private final Material material;
    private final double basePrice;
    private final int amount;
    private final int price;
    private final ItemStack itemStack;

    public ShopBuyEntry(String displayName, Material material, double basePrice, int amount) {
        this.displayName = displayName;
        this.material = material;
        this.basePrice = basePrice;
        this.amount = amount;
        this.price = (int) (amount * basePrice);
        this.itemStack = new ItemBuilder(this.material).amount(this.amount).build();
    }

    public ShopBuyEntry(String displayName, ItemStack itemStack, double basePrice) {
        this.displayName = displayName;
        this.material = itemStack.getType();
        this.basePrice = basePrice;
        this.amount = itemStack.getAmount();
        this.price = (int) (this.amount * basePrice);
        this.itemStack = new ItemBuilder(itemStack).amount(this.amount).build();
    }
}
