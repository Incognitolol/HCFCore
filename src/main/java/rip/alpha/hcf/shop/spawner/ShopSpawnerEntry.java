package rip.alpha.hcf.shop.spawner;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

@Getter
@RequiredArgsConstructor
public class ShopSpawnerEntry {

    private final int index;
    private final String displayName;
    private final ItemStack itemStack;
    private final int price;

}
