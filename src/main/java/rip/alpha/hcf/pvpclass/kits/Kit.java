package rip.alpha.hcf.pvpclass.kits;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public abstract class Kit {

    private final String name;

    public Kit(String name) {
        this.name = name;
    }

    public void giveKit(Player player) {
        player.getInventory().setContents(this.getContents());
        player.getInventory().setArmorContents(this.getArmorContents());
        player.updateInventory();
    }

    public abstract ItemStack[] getContents();

    public abstract ItemStack[] getArmorContents();

}
