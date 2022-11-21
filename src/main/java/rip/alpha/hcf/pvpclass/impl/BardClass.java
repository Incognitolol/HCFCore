package rip.alpha.hcf.pvpclass.impl;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import net.mcscrims.basic.Basic;
import net.mcscrims.basic.profile.BasicProfile;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TaskUtil;
import net.mcscrims.libraries.util.items.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
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
import rip.alpha.hcf.HCFConfiguration;
import rip.alpha.hcf.effect.PlayerEffect;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.pvpclass.PvPClass;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.impl.SystemTeam;
import rip.alpha.hcf.timer.impl.CombatTagTimer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class BardClass extends PvPClass {

    private final Object2DoubleMap<UUID> bardEnergyMap = new Object2DoubleOpenHashMap<>();
    private final Object2IntMap<Material> energyCost = new Object2IntOpenHashMap<>();

    private final int bardRadius;
    private final int bardSwordNerf;
    private final double bardMaxEnergy;

    public BardClass() {
        super("Bard");

        HCFConfiguration configuration = HCF.getInstance().getConfiguration();
        this.bardRadius = configuration.getBardRadius();
        this.bardSwordNerf = configuration.getBardSwordNerf();
        this.bardMaxEnergy = configuration.getBardMaxEnergy();

        this.addEffect(PotionEffectType.SPEED, 2);
        this.addEffect(PotionEffectType.REGENERATION, 0);
        this.addEffect(PotionEffectType.DAMAGE_RESISTANCE, 1);
        this.addEffect(PotionEffectType.WEAKNESS, 0);
        this.addEffect(PotionEffectType.FIRE_RESISTANCE, 0);

        this.addHoldableEffect(Material.SUGAR, PotionEffectType.SPEED, 1);
        this.addHoldableEffect(Material.FEATHER, PotionEffectType.JUMP, 1);
        this.addHoldableEffect(Material.GHAST_TEAR, PotionEffectType.REGENERATION, 0);
        this.addHoldableEffect(Material.MAGMA_CREAM, PotionEffectType.FIRE_RESISTANCE, 0);

        this.addClickableEffect(Material.SUGAR, 30, PotionEffectType.SPEED, 2);
        this.addClickableEffect(Material.BLAZE_POWDER, 45, PotionEffectType.INCREASE_DAMAGE, 0);
        this.addClickableEffect(Material.IRON_INGOT, 45, PotionEffectType.DAMAGE_RESISTANCE, 0);
        this.addClickableEffect(Material.FEATHER, 15, PotionEffectType.JUMP, 7);
        this.addClickableEffect(Material.SPIDER_EYE, 60, PotionEffectType.WITHER, 1);
    }

    @Override
    public boolean isApplicable(Player player, PlayerInventory inventory) {
        if (inventory.getHelmet() == null || inventory.getChestplate() == null || inventory.getLeggings() == null || inventory.getBoots() == null) {
            return false;
        }

        if (!(inventory.getHelmet().getType() == Material.GOLD_HELMET
                && inventory.getChestplate().getType() == Material.GOLD_CHESTPLATE
                && inventory.getLeggings().getType() == Material.GOLD_LEGGINGS
                && inventory.getBoots().getType() == Material.GOLD_BOOTS)) {
            return false;
        }

        return this.checkLimit(player);
    }

    @Override
    public void onEquip(Player player) {
        this.bardEnergyMap.put(player.getUniqueId(), 0D);
    }

    @Override
    public void onUnEquip(Player player) {
        this.bardEnergyMap.remove(player.getUniqueId());
    }

    @Override
    public void onTick(Player player, PlayerInventory inventory) {
        double currentEnergy = this.getEnergy(player);
        double increaseAmount = 0.1D;
        ItemStack[] armour = inventory.getArmorContents();
        for (ItemStack itemStack : armour){
            if (!itemStack.hasItemMeta()) continue;
            if (itemStack.getItemMeta().getLore().isEmpty()) continue;

            if (HCF.getInstance().getCrateHandler().isKothItem(itemStack)) {
                increaseAmount = increaseAmount + 0.01D;
            }
        }

        currentEnergy += increaseAmount;
        if (currentEnergy >= this.bardMaxEnergy) {
            currentEnergy = this.bardMaxEnergy;
        }
        this.bardEnergyMap.put(player.getUniqueId(), currentEnergy);
        super.onTick(player, inventory);
    }

    @Override
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

                if (this.getEnergy(player) < entry.getKey()) {
                    player.sendMessage(CC.RED + "You do not have enough energy for this bard effect");
                    player.sendMessage(CC.RED + "You need atleast " + entry.getKey() + " bard energy to use this effect");
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
                this.bardEnergyMap.put(player.getUniqueId(), 0D);
                ItemUtils.removeItem(player.getInventory(), itemStack.getType(), itemStack.getDurability(), 1);

                if (effect.getType().getId() != PotionEffectType.INCREASE_DAMAGE.getId()) {
                    this.getPlayerEffectHandler().applyPotionEffect(player, effect);
                }

                PlayerTeam team = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
                TaskUtil.runAsync(() -> {
                    int i = 0;

                    for (Entity entity : player.getNearbyEntities(this.bardRadius, this.bardRadius, this.bardRadius)) {
                        if (entity instanceof Player) {
                            Player target = (Player) entity;
                            if (target.getUniqueId().equals(player.getUniqueId())) {
                                continue;
                            }

                            if (effect.getType().getId() != PotionEffectType.WITHER.getId()) {
                                if (team == null || team.getMember(target.getUniqueId()) == null) {
                                    continue;
                                }

                                TeamProfile targetProfile = HCF.getInstance().getProfileHandler().getProfile(target.getUniqueId());
                                if (targetProfile != null) {
                                    Team lastTeam = targetProfile.getLastClaimTeam();
                                    if (lastTeam instanceof SystemTeam) {
                                        SystemTeam systemTeam = (SystemTeam) lastTeam;
                                        if (systemTeam.isSafezone()) {
                                            continue;
                                        }
                                    }
                                }
                            } else {
                                if (team != null && team.getMember(target.getUniqueId()) != null) {
                                    continue;
                                }

                                TeamProfile targetProfile = HCF.getInstance().getProfileHandler().getProfile(target.getUniqueId());
                                if (targetProfile != null) {
                                    Team lastTeam = targetProfile.getLastClaimTeam();
                                    if (lastTeam instanceof SystemTeam) {
                                        SystemTeam systemTeam = (SystemTeam) lastTeam;
                                        if (systemTeam.isSafezone()) {
                                            continue;
                                        }
                                    }
                                }
                            }

                            this.getPlayerEffectHandler().applyPotionEffect(target, effect);
                            i++;
                        }
                    }

                    if (i > 0) {
                        BasicProfile basicProfile = Basic.getInstance().getBasicAPI().getProfile(player.getUniqueId());
                        basicProfile.addXp(i);
                        player.sendMessage(CC.GREEN + "You have applied this effect to " + i + " other player(s)");
                    } else {
                        player.sendMessage(CC.RED + "There was nobody nearby to apply the effect to.");
                    }
                }, HCF.getInstance());
            }
        }
    }

    @Override
    public void handleHoldable(Player player, PlayerEffect effect) {
        super.handleHoldable(player, effect);
        PlayerTeam team = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
        if (team != null) {
            for (Entity entity : player.getNearbyEntities(this.bardRadius, this.bardRadius, this.bardRadius)) {
                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    if (target.getUniqueId().equals(player.getUniqueId())) {
                        continue;
                    }
                    if (team.getMember(target.getUniqueId()) == null) {
                        continue;
                    }

                    PotionEffect potionEffect = HCF.getInstance().getPlayerEffectHandler().getActivePotionEffect(target, effect.getType());
                    if (potionEffect != null) {
                        if (potionEffect.getAmplifier() == effect.getAmplifier()
                                && potionEffect.getDuration() > (20 * 4)) {
                            continue;
                        }
                    }

                    this.getPlayerEffectHandler().applyPotionEffect(target, effect);
                }
            }
        }
    }

    @Override
    public void handleMove(Player player, Location from, Location to) {

    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {

    }

    @Override
    public void handleDamage(EntityDamageEvent event) {

    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if (damager == null) {
                return;
            }
            if (!this.isEquip(damager)) {
                return;
            }
            ItemStack item = damager.getItemInHand();
            if (!item.getType().name().contains("_SWORD")) {
                return;
            }
            event.setDamage(event.getDamage() * (1D - (this.bardSwordNerf / 100D)));
        }
    }

    @Override
    public void handle(Event event) {

    }

    public double getEnergy(Player player) {
        return this.getEnergy(player.getUniqueId());
    }

    public double getEnergy(UUID uuid) {
        return this.bardEnergyMap.getOrDefault(uuid, 0D);
    }

}
