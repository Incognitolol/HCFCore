package rip.alpha.hcf.pvpclass;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TimeUtil;
import net.mcscrims.libraries.util.items.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.effect.PlayerEffect;
import rip.alpha.hcf.effect.PlayerEffectHandler;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.pvpclass.impl.ArcherClass;
import rip.alpha.hcf.pvpclass.impl.BardClass;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.impl.SystemTeam;
import rip.alpha.hcf.timer.impl.CombatTagTimer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@RequiredArgsConstructor
public abstract class PvPClass {

    private final String name;

    private final Int2ObjectMap<PlayerEffect> playerEffects = new Int2ObjectOpenHashMap<>();
    private final Table<Material, Integer, PlayerEffect> clickableEffects = HashBasedTable.create();
    private final Map<Material, PlayerEffect> holdableEffects = new HashMap<>();
    private final Object2LongMap<UUID> clickableEffectCooldowns = new Object2LongOpenHashMap<>();

    private final PlayerEffectHandler playerEffectHandler = HCF.getInstance().getPlayerEffectHandler();

    public void addEffect(PotionEffectType type, int amplifier) {
        this.playerEffects.put(type.getId(), new PlayerEffect(type, PlayerEffect.PERM_EFFECT_TICKS, amplifier));
    }

    public void addEffect(PotionEffectType type, int duration, int amplifier) {
        this.playerEffects.put(type.getId(), new PlayerEffect(type, duration * 20, amplifier));
    }

    public void addClickableEffect(Material material, int i, PotionEffectType type, int amplifier) {
        this.clickableEffects.put(material, i, new PlayerEffect(type, 5 * 20, amplifier));
    }

    public void addHoldableEffect(Material material, PotionEffectType type, int amplifier) {
        this.holdableEffects.put(material, new PlayerEffect(type, 5 * 20, amplifier));
    }

    public void applyEffects(Player player) {
        for (PlayerEffect effect : playerEffects.values()) {
            HCF.getInstance().getPlayerEffectHandler().applyPotionEffect(player, effect);
        }
    }

    public void removeEffects(Player player) {
        if (player.willBeOnline()) {
            for (PlayerEffect effect : this.playerEffects.values()) {
                if (effect == null) {
                    continue;
                }
                player.removePotionEffect(effect.getType()); //remove
                HCF.getInstance().getPlayerEffectHandler().restorePotionEffect(player, effect.getType()); //then restore
            }
        }
    }

    public boolean canUseEffect(Player player) {
        return this.canUseEffect(player.getUniqueId());
    }

    public boolean canUseEffect(UUID uuid) {
        return this.getRemainingCooldown(uuid) <= 0;
    }

    public void addCooldown(UUID uuid, int sec) {
        this.clickableEffectCooldowns.put(uuid, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(sec));
    }

    public long getRemainingCooldown(UUID uuid) {
        if (!this.clickableEffectCooldowns.containsKey(uuid)) {
            return 0;
        }
        long time = this.clickableEffectCooldowns.get(uuid);
        return time - System.currentTimeMillis();
    }

    public void handleRemove(UUID uuid) {
        this.clickableEffectCooldowns.remove(uuid);
    }

    public void onTick(Player player, PlayerInventory inventory) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (profile == null) {
            return;
        }
        Team team = profile.getLastClaimTeam();
        if (team instanceof SystemTeam && ((SystemTeam) team).isSafezone()) {
            return;
        }
        ItemStack itemStack = inventory.getItemInHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }
        PlayerEffect holdEffect = this.getHoldableEffects().get(itemStack.getType());
        if (holdEffect == null) {
            return;
        }
        this.handleHoldable(player, holdEffect);
    }

    public void handleClickableEffect(Player player, PlayerInteractEvent event) {
        if (!this.isEquip(player)) {
            return;
        }

        if (event.getAction().name().contains("RIGHT_")) {
            if (event.getItem() != null) {
                ItemStack itemStack = event.getItem();
                if (itemStack == null) {
                    return;
                }
                Map.Entry<Integer, PlayerEffect> entry = this.getClickableEffect(itemStack.getType());
                if (entry == null) {
                    return;
                }
                PlayerEffect effect = entry.getValue();
                if (effect == null) {
                    return;
                }

                if (!this.canUseEffect(player)) {
                    player.sendMessage(CC.RED + "You are on cooldown for " +
                            TimeUtil.formatTime(this.getRemainingCooldown(player.getUniqueId())));
                    return;
                }

                TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
                if (teamProfile == null) {
                    return;
                }
                Team lastTeamClaim = teamProfile.getLastClaimTeam();
                if (lastTeamClaim instanceof SystemTeam) {
                    SystemTeam systemTeam = (SystemTeam) lastTeamClaim;
                    if (systemTeam.isSafezone()) {
                        player.sendMessage(CC.RED + "You cannot use effects in spawn.");
                        return;
                    }
                }

                teamProfile.addTimer(new CombatTagTimer());
                itemStack = ItemUtils.minusItem(itemStack);
                event.getPlayer().getInventory().setItemInHand(itemStack);
                HCF.getInstance().getPlayerEffectHandler().applyPotionEffect(player, effect);
                this.addCooldown(player.getUniqueId(), entry.getKey());
            }
        }
    }

    public Map.Entry<Integer, PlayerEffect> getClickableEffect(Material material) {
        Map<Integer, PlayerEffect> map = this.clickableEffects.row(material);
        if (map.size() <= 0) {
            return null;
        }
        return map.entrySet().stream().findFirst().get();
    }

    public boolean isEquip(Player player) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (profile == null) {
            return false;
        }
        return profile.isActiveClass(this.getClass());
    }

    public boolean checkLimit(Player player) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (profile == null) {
            return false;
        }

        PlayerTeam team = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
        if (team == null) {
            if (profile.getCouldntEquipClassMessage() == -1
                    || (profile.getCouldntEquipClassMessage() - System.currentTimeMillis() <= 0)) {
                player.sendMessage(CC.RED + "You need to be in a team to equip this kit");
                profile.setCouldntEquipClassMessage(System.currentTimeMillis() + 15000);
            }
            return false;
        }

        Class<? extends PvPClass> clazz = this.getClass();
        if (clazz == BardClass.class){
            int amountInClass = team.getAmountInClass(player.getUniqueId(), clazz);
            int limit = HCF.getInstance().getConfiguration().getBardClassLimit();
            if (amountInClass >= limit) {
                boolean sendMessage = profile.getCouldntEquipClassMessage() == -1
                        || (profile.getCouldntEquipClassMessage() - System.currentTimeMillis() <= 0);
                if (sendMessage) {
                    player.sendMessage(CC.RED + "Your team has too many bards equipped!");
                    profile.setCouldntEquipClassMessage(System.currentTimeMillis() + 15000);
                }
                return false;
            }
        } else if (clazz == ArcherClass.class){
            int amountInClass = team.getAmountInClass(player.getUniqueId(), clazz);
            int limit = HCF.getInstance().getConfiguration().getArcherClassLimit();
            if (amountInClass >= limit) {
                boolean sendMessage = profile.getCouldntEquipClassMessage() == -1
                        || (profile.getCouldntEquipClassMessage() - System.currentTimeMillis() <= 0);
                if (sendMessage) {
                    player.sendMessage(CC.RED + "Your team has too many archers equipped!");
                    profile.setCouldntEquipClassMessage(System.currentTimeMillis() + 15000);
                }
                return false;
            }
        }

        return true;
    }

    public void handleHoldable(Player player, PlayerEffect effect) {
        PotionEffect potionEffect = HCF.getInstance().getPlayerEffectHandler().getActivePotionEffect(player, effect.getType());
        if (potionEffect != null) {
            if (potionEffect.getAmplifier() == effect.getAmplifier()
                    && potionEffect.getDuration() > (20 * 4)) {
                return;
            }
        }
        this.playerEffectHandler.applyPotionEffect(player, effect);
    }

    public abstract boolean isApplicable(Player player, PlayerInventory inventory);

    public abstract void onEquip(Player player);

    public abstract void onUnEquip(Player player);

    public abstract void handleMove(Player player, Location from, Location to);

    public abstract void handleInteract(PlayerInteractEvent event);

    public abstract void handleDamage(EntityDamageEvent event);

    public abstract void handleEntityDamageByEntity(EntityDamageByEntityEvent event);

    public abstract void handle(Event event);

}
