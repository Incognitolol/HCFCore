package rip.alpha.hcf.shop.sell;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@Getter
@RequiredArgsConstructor
public class ShopSellEntry {

    private final String displayName;
    private final Material material, blockMaterial;
    private final double basePrice;

    public int calculateAmount(int baseAmount, int blockAmount) {
        baseAmount += blockAmount * 9;
        return Math.toIntExact((long) (baseAmount * basePrice));
    }

}
