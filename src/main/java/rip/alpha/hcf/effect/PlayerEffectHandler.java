package rip.alpha.hcf.effect;

import lombok.Getter;
import net.minecraft.server.v1_7_R4.MobEffect;
import net.minecraft.server.v1_7_R4.MobEffectList;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.util.CraftPotionUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.pvpclass.PvPClass;
import rip.foxtrot.spigot.fSpigot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerEffectHandler {

    @Getter
    private final Map<UUID, Set<PlayerEffectRestoreEntry>> restoreEntries;

    public PlayerEffectHandler(HCF instance) {
        this.restoreEntries = new HashMap<>();

        fSpigot.INSTANCE.addPotionEffectHandler(new EffectPotionHandler(this));

        PluginManager pluginManager = instance.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerEffectListener(this), instance);
    }

    public Set<PlayerEffectRestoreEntry> getRestoreEntries(UUID uuid) {
        return this.restoreEntries.getOrDefault(uuid, Collections.emptySet());
    }

    public PlayerEffectRestoreEntry getRestoreEntry(UUID uuid, PotionEffectType type) {
        if (this.getRestoreEntries(uuid).isEmpty()) {
            return null;
        }

        for (PlayerEffectRestoreEntry entry : this.getRestoreEntries(uuid)) {
            if (entry.getType().equals(type)) {
                return entry;
            }
        }
        return null;
    }

    public void removeEntry(UUID uuid, PlayerEffectRestoreEntry entry) {
        this.getRestoreEntries(uuid).remove(entry);
    }

    public void addRestoreEntry(UUID uuid, PlayerEffectRestoreEntry entry) {
        PlayerEffectRestoreEntry restoreEntry = this.getRestoreEntry(uuid, entry.getType());

        boolean b = true;

        if (restoreEntry != null) {
            b = false;

            if (restoreEntry.getAmplifier() > entry.getAmplifier()) {
                this.removeEntry(uuid, restoreEntry);
                b = true;
            }
        }

        if (b) {
            this.getRestoreEntries(uuid).add(entry);
        }
    }

    public void restorePotionEffect(Player player, PotionEffectType type) {
        PlayerEffectRestoreEntry entry = this.getRestoreEntry(player.getUniqueId(), type);

        if (entry == null) {
            TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
            if (profile != null) {
                Class<? extends PvPClass> clazz = profile.getEquipPvPClass();
                if (clazz != null) {
                    PvPClass pvPClass = HCF.getInstance().getPvPClassHandler().getPvPClass(clazz);
                    if (pvPClass != null) {
                        PlayerEffect effect = pvPClass.getPlayerEffects().get(type.getId());
                        if (effect != null) {
                            entry = new PlayerEffectRestoreEntry(effect);
                        }
                    }
                }
            }
        }

        if (entry != null) {
            this.restorePotionEffect(player, entry);
        }
    }

    public void restorePotionEffect(Player player, PlayerEffectRestoreEntry entry) {
        entry.apply(player);
        this.getRestoreEntries(player.getUniqueId()).remove(entry);
    }

    public void applyPotionEffect(Player player, PlayerEffect effect) {
        this.applyPotionEffect(player, effect, true);
    }

    public void applyPotionEffect(Player player, PlayerEffect effect, boolean restore) {
        PotionEffect potionEffect = this.getActivePotionEffect(player, effect.getType());

        if (potionEffect != null) { //check if the player has the potion effect
            if (potionEffect.getAmplifier() > effect.getAmplifier()) {
                return;
            }

            if (potionEffect.getDuration() > (30 * 20)) {
                if (potionEffect.getAmplifier() >= effect.getAmplifier()) {
                    return;
                }
            }

            if (potionEffect.getAmplifier() == effect.getAmplifier() && potionEffect.getDuration() > (20 * 30)) {
                return;
            }

            if (restore) {
                if (!(potionEffect.getDuration() >= PlayerEffect.CHECK_EFFECT_TICKS)) {
                    int duration = potionEffect.getDuration() - effect.getDuration(); //minus the duration of the current effect
                    if (effect.getDuration() >= PlayerEffect.CHECK_EFFECT_TICKS) {
                        duration = potionEffect.getDuration();
                    }
                    if (duration > 10) {
                        PlayerEffectRestoreEntry entry = new PlayerEffectRestoreEntry(potionEffect, duration);
                        this.addRestoreEntry(player.getUniqueId(), entry);
                    }
                }
            }
        }

        effect.applyEffect(player);
    }

    public PotionEffect getActivePotionEffect(Player player, PotionEffectType type) {
        MobEffect effect = ((CraftPlayer) player).getHandle().getEffect(MobEffectList.byId[type.getId()]);
        return effect != null ? CraftPotionUtils.toBukkit(effect) : null;
    }

}
