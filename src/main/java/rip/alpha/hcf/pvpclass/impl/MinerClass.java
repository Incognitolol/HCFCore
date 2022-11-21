package rip.alpha.hcf.pvpclass.impl;

import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TaskUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.effect.PlayerEffect;
import rip.alpha.hcf.pvpclass.PvPClass;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MinerClass extends PvPClass {

    private final Set<UUID> hasInvisibility = new HashSet<>();
    private final int invisibilityYLLevel = 20;
    private final PlayerEffect invisibilityEffect = new PlayerEffect(PotionEffectType.INVISIBILITY, 1);

    public MinerClass() {
        super("Miner");
        this.addEffect(PotionEffectType.FIRE_RESISTANCE, 0);
        this.addEffect(PotionEffectType.FAST_DIGGING, 1);
        this.addEffect(PotionEffectType.NIGHT_VISION, 1);
    }

    @Override
    public boolean isApplicable(Player player, PlayerInventory inventory) {
        if (inventory.getHelmet() == null || inventory.getChestplate() == null || inventory.getLeggings() == null || inventory.getBoots() == null) {
            return false;
        }

        return inventory.getHelmet().getType() == Material.IRON_HELMET
                && inventory.getChestplate().getType() == Material.IRON_CHESTPLATE
                && inventory.getLeggings().getType() == Material.IRON_LEGGINGS
                && inventory.getBoots().getType() == Material.IRON_BOOTS;
    }

    @Override
    public void onEquip(Player player) {
    }

    @Override
    public void onUnEquip(Player player) {
        boolean removed = this.hasInvisibility.remove(player.getUniqueId());
        if (removed) {
            TaskUtil.runSync(() -> {
                player.removePotionEffect(PotionEffectType.INVISIBILITY); //remove
                HCF.getInstance().getPlayerEffectHandler().restorePotionEffect(player, PotionEffectType.INVISIBILITY); //then restore
            }, HCF.getInstance());
        }
    }

    @Override
    public void onTick(Player player, PlayerInventory inventory) {

    }

    @Override
    public void handleMove(Player player, Location from, Location to) {
        if (this.hasInvisibility.contains(player.getUniqueId())) {
            if (to.getBlockY() > this.invisibilityYLLevel) {
                TaskUtil.runSync(() -> {
                    player.removePotionEffect(PotionEffectType.INVISIBILITY); //remove
                    HCF.getInstance().getPlayerEffectHandler().restorePotionEffect(player, PotionEffectType.INVISIBILITY); //then restore
                    this.hasInvisibility.remove(player.getUniqueId());
                    player.sendMessage(CC.RED + "Your miner invisibility has been disabled");
                }, HCF.getInstance());
            }
        } else {
            if (to.getBlockY() <= this.invisibilityYLLevel) {
                HCF.getInstance().getPlayerEffectHandler().applyPotionEffect(player, this.invisibilityEffect, true);
                this.hasInvisibility.add(player.getUniqueId());
                player.sendMessage(CC.GREEN + "Your miner invisibility has been enabled");
            }
        }
    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {

    }

    @Override
    public void handleDamage(EntityDamageEvent event) {

    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {

    }

    @Override
    public void handle(Event event) {

    }
}
