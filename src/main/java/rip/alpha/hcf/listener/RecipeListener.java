package rip.alpha.hcf.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import rip.alpha.hcf.HCF;

import java.util.EnumSet;
import java.util.Iterator;

public class RecipeListener implements Listener {

    public static boolean hotfixEnabled = true;
    private static final EnumSet<PotionType> TYPE_BLACKLIST = EnumSet.of(
            PotionType.STRENGTH,
            PotionType.POISON,
            PotionType.SLOWNESS,
            PotionType.REGEN,
            PotionType.WEAKNESS,
            PotionType.INSTANT_DAMAGE
    );

    public RecipeListener() {
        Iterator<Recipe> recipeIterator = HCF.getInstance().getServer().recipeIterator();

        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();

            if (!HCF.getInstance().getConfiguration().isCraftGapples() && recipe.getResult().getDurability() == (short) 1
                    && recipe.getResult().getType() == org.bukkit.Material.GOLDEN_APPLE) {
                recipeIterator.remove();
            }

            if (!HCF.getInstance().getConfiguration().isCraftCrapples() && recipe.getResult().getDurability() == (short) 0
                    && recipe.getResult().getType() == org.bukkit.Material.GOLDEN_APPLE) {
                recipeIterator.remove();
            }

            if (recipe.getResult().getType() == Material.SPECKLED_MELON) {
                recipeIterator.remove();
            }
        }

        this.registerMelonRecipe();
    }

    private void registerMelonRecipe() {
        ItemStack melon = new ItemStack(Material.SPECKLED_MELON);

        ShapelessRecipe recipe = new ShapelessRecipe(melon);
        recipe.addIngredient(Material.MELON);
        recipe.addIngredient(Material.GOLD_NUGGET);

        HCF.getInstance().getServer().addRecipe(recipe);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (event.getRecipe().getResult().getType() != Material.SPECKLED_MELON) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        player.updateInventory();
    }

    // TODO Remove this is a hotfix
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!hotfixEnabled) {
            return;
        }

        ItemStack itemStack = event.getItem();

        if (itemStack == null){
            return;
        }

        if (itemStack.getType() != Material.POTION) {
            return;
        }

        if (itemStack.getDurability() == 0){
            return;
        }

        try {
            Potion potion = Potion.fromItemStack(event.getItem());

            if (TYPE_BLACKLIST.contains(potion.getType())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cThis type of potion is not allowed.");
            }
        } catch (IllegalArgumentException e){
            //ignore extended issue
        }
    }
}
