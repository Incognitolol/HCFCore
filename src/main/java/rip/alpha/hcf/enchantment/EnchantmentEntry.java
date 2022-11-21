package rip.alpha.hcf.enchantment;

import lombok.Getter;
import org.bukkit.enchantments.Enchantment;

@Getter
public class EnchantmentEntry {

    private final int enchantmentId;
    private final int maxLevel;

    public EnchantmentEntry(Enchantment enchantment, int maxLevel) {
        this.enchantmentId = enchantment.getId();
        this.maxLevel = maxLevel;
        enchantment.setMaxLevel(maxLevel);
        if (maxLevel == 0) {
            enchantment.setRandomWeight(0);
        }
    }

    public boolean overLimit(int level) {
        return level > maxLevel;
    }
}
