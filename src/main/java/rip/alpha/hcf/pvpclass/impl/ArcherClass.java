package rip.alpha.hcf.pvpclass.impl;

import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.DamagerUtils;
import net.mcscrims.libraries.util.gson.GsonUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.HCFConfiguration;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.pvpclass.PvPClass;
import rip.alpha.hcf.timer.impl.ArcherTagTimer;

public class ArcherClass extends PvPClass {

    private final int archerTagTime;
    private final int archerTagPercentage;
    private final String playerFormat;
    private final String targetFormat;

    public ArcherClass() {
        super("Archer");

        HCFConfiguration configuration = HCF.getInstance().getConfiguration();
        this.archerTagTime = configuration.getArcherTagTime();
        this.archerTagPercentage = configuration.getArcherTagPercentage();
        this.playerFormat =
                CC.translate("&4[&cArrow Range &4(&7%s&4)]&7 Marked player for " + this.archerTagTime + " seconds.");
        this.targetFormat =
                CC.translate("&4&lMarked! &cAn archer has shot you and marked you (" + archerTagPercentage + "% damage) for " + archerTagTime + " seconds");
        ;

        this.addEffect(PotionEffectType.SPEED, 2);
        this.addEffect(PotionEffectType.DAMAGE_RESISTANCE, 0);

        this.addClickableEffect(Material.SUGAR, 60, PotionEffectType.SPEED, 3);
        this.addClickableEffect(Material.FEATHER, 60, PotionEffectType.JUMP, 7);
        this.addClickableEffect(Material.IRON_INGOT, 120, PotionEffectType.DAMAGE_RESISTANCE, 2);

        this.addHoldableEffect(Material.FEATHER, PotionEffectType.JUMP, 2);
    }

    @Override
    public boolean isApplicable(Player player, PlayerInventory inventory) {
        if (inventory.getHelmet() == null || inventory.getChestplate() == null || inventory.getLeggings() == null || inventory.getBoots() == null) {
            return false;
        }

        if (!(inventory.getHelmet().getType() == Material.LEATHER_HELMET
                && inventory.getChestplate().getType() == Material.LEATHER_CHESTPLATE
                && inventory.getLeggings().getType() == Material.LEATHER_LEGGINGS
                && inventory.getBoots().getType() == Material.LEATHER_BOOTS)) {
            return false;
        }

        return this.checkLimit(player);
    }

    @Override
    public void onEquip(Player player) {

    }

    @Override
    public void onUnEquip(Player player) {

    }

    @Override
    public void handleMove(Player player, Location from, Location to) {

    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {

    }

    @Override
    public void handleDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
            if (!profile.isArcherTagged()) {
                return;
            }
            event.setDamage(event.getDamage() * (1D + (this.archerTagPercentage / 100D)));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        this.handleArrowDamage(event);
    }

    @Override
    public void handle(Event event) {
        if (event instanceof EntityShootBowEvent) {
            this.handleArrowShot((EntityShootBowEvent) event);
        }

        if (event instanceof PlayerRespawnEvent) {
            PlayerRespawnEvent respawnEvent = (PlayerRespawnEvent) event;
            Player player = respawnEvent.getPlayer();
            TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
            profile.removeTimer(ArcherTagTimer.class);
        }
    }

    private void handleArrowShot(EntityShootBowEvent event) {
        Arrow arrow = (Arrow) event.getProjectile();
        arrow.setMetadata("pullback", new FixedMetadataValue(HCF.getInstance(), event.getForce()));
        arrow.setMetadata("location", new FixedMetadataValue(HCF.getInstance(), GsonUtil.GSON.toJson(event.getEntity().getLocation())));
    }

    private void handleArrowDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (!arrow.hasMetadata("pullback")) {
                return;
            }
            float pullback = arrow.getMetadata("pullback").get(0).asFloat();
            if (!arrow.hasMetadata("location")) {
                return;
            }
            Location location = GsonUtil.GSON.fromJson(arrow.getMetadata("location").get(0).asString(), Location.class);
            ProjectileSource source = arrow.getShooter();
            if (!(source instanceof Player)) {
                return;
            }
            Player shooter = (Player) source;
            TeamProfile shooterProfile = HCF.getInstance().getProfileHandler().getProfile(shooter);
            if (!shooterProfile.isActiveClass(ArcherClass.class)) {
                return;
            }

            ItemStack itemInHand = shooter.getItemInHand();
            if (itemInHand.getType() == Material.BOW && HCF.getInstance().getCrateHandler().isKothItem(itemInHand)){
                return;
            }

            Player target = (Player) event.getEntity();

            if (pullback <= 0.5) {
                DamagerUtils.setDamage(event, 1);
                return;
            }

            int damage = 3;
            TeamProfile targetProfile = HCF.getInstance().getProfileHandler().getProfile(target);
            if (targetProfile.isActiveClass(ArcherClass.class)) {
                return;
            }

            if (!targetProfile.isArcherTagged()) {
                damage = 4;
                target.sendMessage(this.targetFormat);
            }

            int distance = (int) location.distance(target.getLocation());
            targetProfile.addTimer(new ArcherTagTimer(this.archerTagTime));
            shooter.sendMessage(String.format(this.playerFormat, distance));
            DamagerUtils.setDamage(event, damage);
        }
    }
}
