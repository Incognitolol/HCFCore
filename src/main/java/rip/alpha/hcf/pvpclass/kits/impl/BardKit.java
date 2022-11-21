package rip.alpha.hcf.pvpclass.kits.impl;

import net.mcscrims.libraries.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.pvpclass.kits.Kit;

public class BardKit extends Kit {

    private final ItemStack[] contents = new ItemStack[9 * 4];
    private final ItemStack[] armorContents = new ItemStack[4];

    public BardKit() {
        super("Bard");

        ItemStack potion = new ItemBuilder(Material.POTION)
                .durability(16421)
                .build();

        ItemStack sword = new ItemBuilder(Material.DIAMOND_SWORD)
                .enchantment(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ALL.getMaxLevel())
                .build();

        ItemStack pearls = new ItemBuilder(Material.ENDER_PEARL)
                .amount(16)
                .build();

        ItemStack food = new ItemBuilder(Material.COOKED_BEEF)
                .amount(64)
                .build();

        ItemStack fres = new ItemBuilder(Material.POTION)
                .durability(8259)
                .build();

        ItemStack helmet = new ItemBuilder(Material.GOLD_HELMET)
                .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel())
                .enchantment(Enchantment.DURABILITY, 3)
                .build();

        ItemStack chestplate = new ItemBuilder(Material.GOLD_CHESTPLATE)
                .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel())
                .enchantment(Enchantment.DURABILITY, 3)
                .build();

        ItemStack leggings = new ItemBuilder(Material.GOLD_LEGGINGS)
                .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel())
                .enchantment(Enchantment.DURABILITY, 3)
                .build();

        ItemStack boots = new ItemBuilder(Material.GOLD_BOOTS)
                .enchantment(Enchantment.PROTECTION_FALL, Enchantment.PROTECTION_FALL.getMaxLevel())
                .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel())
                .enchantment(Enchantment.DURABILITY, 3)
                .build();

        ItemStack powder = new ItemStack(Material.BLAZE_POWDER, 64);
        ItemStack sugar = new ItemStack(Material.SUGAR, 64);
        ItemStack iron = new ItemStack(Material.IRON_INGOT, 64);
        ItemStack ghast = new ItemStack(Material.GHAST_TEAR, 64);
        ItemStack feather = new ItemStack(Material.FEATHER, 64);
        ItemStack spiderEye = new ItemStack(Material.SPIDER_EYE, 64);
        ItemStack magmaCream = new ItemStack(Material.MAGMA_CREAM, 64);

        this.contents[0] = sword;
        this.contents[1] = pearls;
        this.contents[2] = fres;
        this.contents[4] = magmaCream;
        this.contents[5] = iron;
        this.contents[6] = sugar;
        this.contents[7] = powder;
        this.contents[8] = food;

        this.contents[17] = spiderEye;
        this.contents[26] = feather;
        this.contents[35] = ghast;

        this.armorContents[0] = boots;
        this.armorContents[1] = leggings;
        this.armorContents[2] = chestplate;
        this.armorContents[3] = helmet;

        for (int i = 0; i < this.contents.length; i++) {
            ItemStack itemStack = this.contents[i];
            if (itemStack != null) {
                continue;
            }
            this.contents[i] = potion;
        }
    }

    @Override
    public ItemStack[] getContents() {
        return this.contents;
    }

    @Override
    public ItemStack[] getArmorContents() {
        return this.armorContents;
    }
}
