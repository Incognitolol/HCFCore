package rip.alpha.hcf.pvpclass;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.EquipmentSetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.spigotmc.SpigotConfig;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.effect.PlayerEffect;
import rip.alpha.hcf.profile.TeamProfile;

@RequiredArgsConstructor
public class PvPClassListener implements Listener {

    private final PvPClassHandler pvPClassHandler;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getDuration() >= PlayerEffect.CHECK_EFFECT_TICKS) {
                player.removePotionEffect(effect.getType()); //remove
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(event.getPlayer());
        if (profile.getEquipPvPClass() != null) {
            PvPClass pvPClass = HCF.getInstance().getPvPClassHandler().getPvPClass(profile.getEquipPvPClass());
            this.pvPClassHandler.unequipClass(event.getPlayer(), profile, pvPClass);
        }
    }

    @EventHandler
    public void onEquipmentSet(EquipmentSetEvent event) {
        HumanEntity entity = event.getHumanEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
            if (profile == null) {
                return;
            }
            profile.setEquipTime(-1L);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        for (PvPClass pvPClass : this.pvPClassHandler.getPvPClasses()) {
            pvPClass.handleClickableEffect(player, event);
            pvPClass.handleInteract(event);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        for (PvPClass pvPClass : this.pvPClassHandler.getPvPClasses()) {
            pvPClass.handleDamage(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        for (PvPClass pvPClass : this.pvPClassHandler.getPvPClasses()) {
            pvPClass.handleEntityDamageByEntity(event);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        for (PvPClass pvPClass : this.pvPClassHandler.getPvPClasses()) {
            pvPClass.handle(event);
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        for (PvPClass pvPClass : this.pvPClassHandler.getPvPClasses()) {
            pvPClass.handle(event);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        for (PvPClass pvPClass : this.pvPClassHandler.getPvPClasses()) {
            pvPClass.handle(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (SpigotConfig.removeOnlyBadEffectsWithMilk) {
            return;
        }
        Player player = event.getPlayer();
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (event.getItem() != null && event.getItem().getType() == Material.MILK_BUCKET) {
            Bukkit.getScheduler().runTaskLater(HCF.getInstance(), () -> {
                PvPClass pvPClass = HCF.getInstance().getPvPClassHandler().getPvPClass(teamProfile.getEquipPvPClass());
                if (pvPClass == null) {
                    return;
                }
                pvPClass.applyEffects(player);
            }, 2L);
        }
    }
}
