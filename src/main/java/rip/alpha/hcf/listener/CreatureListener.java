package rip.alpha.hcf.listener;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import rip.alpha.hcf.HCF;

import java.util.Random;

public class CreatureListener implements Listener {
    private final Random random = new Random();

    @EventHandler(priority = EventPriority.LOW)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.BAT
                || event.getEntityType() == EntityType.SQUID
                || event.getEntityType() == EntityType.WOLF) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof Monster) {
            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER
                    || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
                    || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
                return;
            }

            event.setCancelled(true);
        } else if (event.getEntity() instanceof Flying) {
            if (HCF.getInstance().getBorderHandler().inWarzone(event.getEntity().getLocation())) {
                event.setCancelled(true);
                return;
            }

            if (event.getEntity().getType() == EntityType.GHAST) {
                int nextInt = this.random.nextInt(100);
                if (nextInt <= 97) {
                    event.setCancelled(true);
                }
            }
        } else if (event.getEntity() instanceof Slime) {
            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER
                    || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
                    || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
                return;
            }

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntityType() != EntityType.GHAST) {
            return;
        }
        if (!(event.getTarget() instanceof Player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            int chance = random.nextInt(100);
            if (chance <= 25) {
                event.getDrops().addAll(event.getDrops());
            }
        }

        double multiplier = 1;

        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();

            if (player.getItemInHand() != null && player.getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
                switch (player.getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)) {
                    case 1:
                        multiplier = 1.2;
                        break;
                    case 2:
                        multiplier = 1.4;
                        break;
                    case 3:
                        multiplier = 2.0;
                        break;
                    default:
                        break;
                }
            }
        }

        event.setDroppedExp((int) Math.ceil(event.getDroppedExp() * multiplier));
    }
}
