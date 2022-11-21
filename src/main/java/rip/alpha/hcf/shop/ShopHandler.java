package rip.alpha.hcf.shop;

import lombok.Getter;
import net.mcscrims.libraries.spawner.SpawnerEntry;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.shop.buy.ShopBuyEntry;
import rip.alpha.hcf.shop.buy.ShopBuyMenu;
import rip.alpha.hcf.shop.sell.ShopSellEntry;
import rip.alpha.hcf.shop.sell.ShopSellMenu;
import rip.alpha.hcf.shop.spawner.ShopSpawnerEntry;
import rip.alpha.hcf.shop.spawner.ShopSpawnerMenu;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ShopHandler {

    private final List<ShopSellEntry> shopSellEntries;
    private final List<ShopBuyEntry> shopBuyEntries;
    private final List<ShopSpawnerEntry> shopSpawnerEntries;

    public ShopHandler(HCF instance) {
        this.shopSellEntries = new ArrayList<>();
        this.shopBuyEntries = new ArrayList<>();
        this.shopSpawnerEntries = new ArrayList<>();

        this.registerSellEntry("&bDiamonds", Material.DIAMOND, Material.DIAMOND_BLOCK, 17.36D);
        this.registerSellEntry("&aEmeralds", Material.EMERALD, Material.EMERALD_BLOCK, 20.83D);
        this.registerSellEntry("&cRedstone", Material.REDSTONE, Material.REDSTONE_BLOCK, 3.4D);
        this.registerSellEntry("&6Gold", Material.GOLD_INGOT, Material.GOLD_BLOCK, 6.91D);
        this.registerSellEntry("&9Lapis", Material.INK_SACK, Material.LAPIS_BLOCK, 6.91D);
        this.registerSellEntry("&7Iron", Material.IRON_INGOT, Material.IRON_BLOCK, 5.2D);
        this.registerSellEntry("&8Coal", Material.COAL, Material.COAL_BLOCK, 1.73D);
        this.registerSellEntry("&aSuger Cane", Material.SUGAR_CANE, null, 25D);
        this.registerSellEntry("&cNether Wart", Material.NETHER_STALK, null, 30D);
        this.registerSellEntry("&2Melon", Material.MELON, null, 30D);
        this.registerSellEntry("&6Carrots", Material.CARROT_ITEM, null, 10D);
        this.registerSellEntry("&ePotato", Material.POTATO_ITEM, null, 10D);
        this.registerSellEntry("&aSlime Balls", Material.SLIME_BALL, null, 30D);
        this.registerSellEntry("&8Gunpowder", Material.SULPHUR, null, 50D);

        this.registerBuyEntry("&aSugar Cane", Material.SUGAR_CANE, 25D, 16);
        this.registerBuyEntry("&cNether Wart", Material.NETHER_STALK, 30D, 16);
        this.registerBuyEntry("&2Melon Seeds", Material.MELON_SEEDS, 45D, 16);
        this.registerBuyEntry("&6Carrots", Material.CARROT_ITEM, 10D, 16);
        this.registerBuyEntry("&ePotato", Material.POTATO_ITEM, 10D, 16);
        this.registerBuyEntry("&6Glowstone", Material.GLOWSTONE, 10D, 16);
        this.registerBuyEntry("&5Soul Sand", Material.SOUL_SAND, 10D, 16);
        this.registerBuyEntry("&fGhast Tear", Material.GHAST_TEAR, 10D, 16);
        this.registerBuyEntry("&aSlime Balls", Material.SLIME_BALL, 30D, 16);
        this.registerBuyEntry("&7Cow Eggs", new ItemBuilder(Material.MONSTER_EGG).durability(92).amount(2).build(), 500D);
        this.registerBuyEntry("&5End Portal Frame", Material.ENDER_PORTAL_FRAME, 5000D, 1);

        this.registerSpawnerEntry(10, "&fSkeleton Spawner", EntityType.SKELETON, 30000);
        this.registerSpawnerEntry(11, "&3Zombie Spawner", EntityType.ZOMBIE, 30000);
        this.registerSpawnerEntry(12, "&cSpider Spawner", EntityType.SPIDER, 30000);
        this.registerSpawnerEntry(13, "&5Cave Spider Spawner", EntityType.CAVE_SPIDER, 30000);
        this.registerSpawnerEntry(14, "&eCow Spawner", EntityType.COW, 30000);
        this.registerSpawnerEntry(15, "&6Zombie Pigman Spawner", EntityType.PIG_ZOMBIE, 40000);
        this.registerSpawnerEntry(16, "&aSlime Spawner", EntityType.SLIME, 40000);
        this.registerSpawnerEntry(21, "&eChicken Spawner", EntityType.CHICKEN, 30000);
        this.registerSpawnerEntry(22, "&6Crowbar", HCF.getInstance().getCrowbarHandler().createDefaultCrowbarItem().toItemStack(), 15000);
        this.registerSpawnerEntry(23, "&2Creeper Spawner", EntityType.CREEPER, 80000);

        PluginManager pluginManager = instance.getServer().getPluginManager();
        pluginManager.registerEvents(new ShopListener(this), instance);
    }

    public void openSellMenu(Player player) {
        ShopSellMenu shopSellMenu = new ShopSellMenu(this, false);
        shopSellMenu.openMenu(player);
    }

    public void openBuyMenu(Player player) {
        ShopBuyMenu shopBuyMenu = new ShopBuyMenu(this, false);
        shopBuyMenu.openMenu(player);
    }

    public void openSpawnerMenu(Player player) {
        ShopSpawnerMenu shopSpawnerMenu = new ShopSpawnerMenu(this, false);
        shopSpawnerMenu.openMenu(player);
    }

    private void registerSellEntry(String displayName, Material material, Material blockMaterial, double basePrice) {
        ShopSellEntry shopSellEntry = new ShopSellEntry(CC.translate(displayName), material, blockMaterial, basePrice);
        this.shopSellEntries.add(shopSellEntry);
    }

    private void registerBuyEntry(String displayName, Material material, double basePrice, int amount) {
        ShopBuyEntry shopBuyEntry = new ShopBuyEntry(CC.translate(displayName), material, basePrice, amount);
        this.shopBuyEntries.add(shopBuyEntry);
    }

    private void registerBuyEntry(String displayName, ItemStack itemStack, double basePrice) {
        ShopBuyEntry shopBuyEntry = new ShopBuyEntry(CC.translate(displayName), itemStack, basePrice);
        this.shopBuyEntries.add(shopBuyEntry);
    }

    private void registerSpawnerEntry(int index, String displayName, EntityType entityType, int price) {
        SpawnerEntry spawnerEntry = new SpawnerEntry(entityType);
        ItemStack itemStack = spawnerEntry.toItemStack();
        this.registerSpawnerEntry(index, displayName, itemStack, price);
    }

    private void registerSpawnerEntry(int index, String displayName, ItemStack itemStack, int price) {
        ShopSpawnerEntry shopSpawnerEntry = new ShopSpawnerEntry(index, CC.translate(displayName), itemStack, price);
        this.shopSpawnerEntries.add(shopSpawnerEntry);
    }
}
