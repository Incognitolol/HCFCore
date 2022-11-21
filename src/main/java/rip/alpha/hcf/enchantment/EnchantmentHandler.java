package rip.alpha.hcf.enchantment;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import rip.alpha.hcf.HCF;

@Getter
public class EnchantmentHandler {

    private final int maxProtection;
    private final int maxSharpness;
    private final int maxPower;

    private final Int2ObjectMap<EnchantmentEntry> enchantmentLimitMap;

    public EnchantmentHandler(HCF instance) {
        this.enchantmentLimitMap = new Int2ObjectOpenHashMap<>();

        this.maxProtection = instance.getConfiguration().getMapKitProtection();
        this.maxSharpness = instance.getConfiguration().getMapKitSharpness();
        this.maxPower = instance.getConfiguration().getMapKitPower();

        this.registerEnchantmentLimit(Enchantment.PROTECTION_ENVIRONMENTAL, this.maxProtection);
        this.registerEnchantmentLimit(Enchantment.DAMAGE_ALL, this.maxSharpness);
        this.registerEnchantmentLimit(Enchantment.ARROW_DAMAGE, this.maxPower);
        this.registerEnchantmentLimit(Enchantment.ARROW_KNOCKBACK, 0);
        this.registerEnchantmentLimit(Enchantment.ARROW_FIRE, 1);
        this.registerEnchantmentLimit(Enchantment.FIRE_ASPECT, 0);
        this.registerEnchantmentLimit(Enchantment.KNOCKBACK, 0);
        this.registerEnchantmentLimit(Enchantment.DAMAGE_ARTHROPODS, 0);
        this.registerEnchantmentLimit(Enchantment.DAMAGE_UNDEAD, 0);
        this.registerEnchantmentLimit(Enchantment.PROTECTION_PROJECTILE, 0);
        this.registerEnchantmentLimit(Enchantment.PROTECTION_EXPLOSIONS, 0);
        this.registerEnchantmentLimit(Enchantment.PROTECTION_FIRE, 0);
        this.registerEnchantmentLimit(Enchantment.THORNS, 0);
        this.registerEnchantmentLimit(Enchantment.WATER_WORKER, 0);

        instance.getServer().getPluginManager().registerEvents(new EnchantmentLimiterListener(this), instance);
    }

    public void registerEnchantmentLimit(Enchantment enchantment, int maxLevel) {
        EnchantmentEntry enchantmentEntry = new EnchantmentEntry(enchantment, maxLevel);
        this.enchantmentLimitMap.put(enchantment.getId(), enchantmentEntry);
    }

    public EnchantmentEntry getEnchantmentEntry(int id) {
        return this.enchantmentLimitMap.get(id);
    }

    public EnchantmentEntry getEnchantmentEntry(Enchantment enchantment) {
        return this.getEnchantmentEntry(enchantment.getId());
    }
}
