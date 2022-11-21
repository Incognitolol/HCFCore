package rip.alpha.hcf.listener;

import net.mcscrims.libraries.util.CC;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rip.alpha.hcf.HCF;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class FixListener implements Listener {

    private final Set<Material> materialSet = new HashSet<>();
    private final Random random = new Random();

    public FixListener() {
        for (Material material : Material.values()) {
            if (material.name().contains("_HELMET")
                    || material.name().contains("_CHESTPLATE")
                    || material.name().contains("_LEGGINGS")
                    || material.name().contains("_BOOTS")) {
                materialSet.add(material);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                for (PotionEffect eff : player.getActivePotionEffects()) {
                    double div = (eff.getAmplifier() + 1) * 1.3 + 1.0;

                    int dmg;
                    if (event.getDamage() / div <= 1.0) {
                        dmg = (eff.getAmplifier() + 1) * 3 + 1;
                    } else {
                        double strongdmg = 1.5;
                        dmg = (int) (event.getDamage() / div + (int) ((strongdmg + 1) * strongdmg));
                    }

                    event.setDamage(dmg);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onDurability(PlayerItemDamageEvent event) {
        ItemStack itemStack = event.getItem();
        if (itemStack != null) {
            if (this.materialSet.contains(itemStack.getType())) {
                int durability = random.nextInt(100);
                if (durability <= 75) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) { //the block glitch thing
        if (HCF.getInstance().getTeamHandler().getTeamByLocation(event.getBlock().getLocation()) != null && event.isCancelled()) {
            Player player = event.getPlayer();
            if (!player.isOnGround()) {
                player.teleport(player.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (!(block.getType() == Material.BED || block.getType() == Material.BED_BLOCK)) {
            return;
        }
        if (block.getWorld().getName().equalsIgnoreCase("world")) {
            return;
        }
        player.sendMessage(CC.RED + "You cannot interact with beds in the nether or end");
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DEFAULT);
    }
}
