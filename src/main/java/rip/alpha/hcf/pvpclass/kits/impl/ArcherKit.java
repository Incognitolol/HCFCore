package rip.alpha.hcf.pvpclass.kits.impl;

import net.mcscrims.libraries.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.pvpclass.kits.Kit;

public class ArcherKit extends Kit {

    private final ItemStack[] contents = new ItemStack[9 * 4];
    private final ItemStack[] armorContents = new ItemStack[4];

    public ArcherKit() {
        super("Archer");

        ItemStack potion = new ItemBuilder(Material.POTION)
                .durability(16421)
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

        ItemStack helmet = new ItemBuilder(Material.LEATHER_HELMET)
                .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel())
                .enchantment(Enchantment.DURABILITY, 3)
                .build();

        ItemStack chestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE)
                .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel())
                .enchantment(Enchantment.DURABILITY, 3)
                .build();

        ItemStack leggings = new ItemBuilder(Material.LEATHER_LEGGINGS)
                .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel())
                .enchantment(Enchantment.DURABILITY, 3)
                .build();

        ItemStack boots = new ItemBuilder(Material.LEATHER_BOOTS)
                .enchantment(Enchantment.PROTECTION_FALL, 4)
                .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel())
                .enchantment(Enchantment.DURABILITY, 3)
                .build();

        ItemStack bow = new ItemBuilder(Material.BOW)
                .enchantment(Enchantment.ARROW_DAMAGE, Enchantment.ARROW_DAMAGE.getMaxLevel())
                .enchantment(Enchantment.ARROW_FIRE, Enchantment.ARROW_FIRE.getMaxLevel())
                .enchantment(Enchantment.DURABILITY, Enchantment.DURABILITY.getMaxLevel())
                .enchantment(Enchantment.ARROW_INFINITE, 1).build();

        ItemStack sugar = new ItemStack(Material.SUGAR, 64);
        ItemStack feather = new ItemStack(Material.FEATHER, 64);
        ItemStack iron = new ItemStack(Material.IRON_INGOT, 64);

        this.contents[0] = sword;
        this.contents[1] = pearls;
        this.contents[2] = bow;
        this.contents[4] = fres;
        this.contents[5] = sugar;
        this.contents[6] = feather;
        this.contents[7] = iron;
        this.contents[8] = food;
        this.contents[9] = new ItemStack(Material.ARROW);

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
