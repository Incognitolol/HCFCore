package rip.alpha.hcf.pvpclass.kits.impl;

import net.mcscrims.libraries.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.pvpclass.kits.Kit;

public class DiamondKit extends Kit {

    private final ItemStack[] contents = new ItemStack[9 * 4];
    private final ItemStack[] armorContents = new ItemStack[4];

    public DiamondKit() {
        super("Diamond");

        ItemStack potion = new ItemBuilder(Material.POTION)
                .durability(16421)
                .build();

        ItemStack speed = new ItemBuilder(Material.POTION)
                .durability(8226)
                .build();

        ItemStack fres = new ItemBuilder(Material.POTION)
                .durability(8259)
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
        ;

        ItemStack helmet = new ItemBuilder(Material.DIAMOND_HELMET)
                .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel())
                .enchantment(Enchantment.DURABILITY, 3)
                .build();

        ItemStack chestplate = new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel())
                .enchantment(Enchantment.DURABILITY, 3)
                .build();

        ItemStack leggings = new ItemBuilder(Material.DIAMOND_LEGGINGS)
                .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel())
                .enchantment(Enchantment.DURABILITY, 3)
                .build();

        ItemStack boots = new ItemBuilder(Material.DIAMOND_BOOTS)
                .enchantment(Enchantment.PROTECTION_FALL, 4)
                .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel())
                .enchantment(Enchantment.DURABILITY, 3)
                .build();

        this.contents[0] = sword;
        this.contents[1] = pearls;
        this.contents[6] = fres;
        this.contents[7] = speed;
        this.contents[8] = food;

        this.contents[17] = speed;
        this.contents[26] = speed;
        this.contents[35] = speed;

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
