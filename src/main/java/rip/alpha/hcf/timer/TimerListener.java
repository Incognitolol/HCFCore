package rip.alpha.hcf.timer;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.DamagerUtils;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.TeamProfileHandler;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.event.other.ClaimChangeEvent;
import rip.alpha.hcf.team.impl.SystemTeam;
import rip.alpha.hcf.timer.impl.CombatTagTimer;
import rip.alpha.hcf.timer.impl.CrappleTimer;
import rip.alpha.hcf.timer.impl.EnderpearlTimer;
import rip.alpha.hcf.timer.impl.GoppleTimer;
import rip.alpha.hcf.timer.impl.HomeTimer;
import rip.alpha.hcf.timer.impl.LogoutTimer;
import rip.alpha.hcf.timer.impl.PvPTimer;
import rip.alpha.hcf.timer.impl.StuckTimer;

@RequiredArgsConstructor
public class TimerListener implements Listener {

    private final TimerHandler timerHandler;
    private final TeamProfileHandler teamProfileHandler;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player damager = DamagerUtils.getDamager(event);
        if (damager == null) {
            return;
        }
        Player damaged = (Player) event.getEntity();
        if (damaged.getUniqueId().equals(damager.getUniqueId())) {
            return;
        }
        TeamProfile damagerProfile = this.teamProfileHandler.getProfile(damager);
        TeamProfile damagedProfile = this.teamProfileHandler.getProfile(damaged);

        if (damagedProfile.hasTimer(PvPTimer.class)) {
            damager.sendMessage(CC.RED + "That player has pvp protection.");
            event.setCancelled(true);
            return;
        }

        if (damagerProfile.hasTimer(PvPTimer.class)) {
            damager.sendMessage(CC.RED + "You cannot damage other players with pvp timer.");
            event.setCancelled(true);
            return;
        }

        this.timerHandler.cancelTimer(damagerProfile, HomeTimer.class);
        this.timerHandler.cancelTimer(damagerProfile, StuckTimer.class);
        this.timerHandler.cancelTimer(damagerProfile, LogoutTimer.class);

        this.timerHandler.cancelTimer(damagedProfile, HomeTimer.class);
        this.timerHandler.cancelTimer(damagedProfile, StuckTimer.class);
        this.timerHandler.cancelTimer(damagedProfile, LogoutTimer.class);

        if (!damaged.isDead()) {
            damagerProfile.addTimer(new CombatTagTimer());
        }
        if (!damager.isDead()) {
            damagedProfile.addTimer(new CombatTagTimer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!(event.getCause() == EntityDamageEvent.DamageCause.LAVA
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK)) {
            return;
        }
        Player player = (Player) event.getEntity();
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (!teamProfile.hasTimer(PvPTimer.class)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT_")) {
            return;
        }
        Player player = event.getPlayer();
        if (!event.hasItem()) {
            return;
        }
        ItemStack itemStack = event.getItem();
        if (itemStack.getType() != Material.ENDER_PEARL) {
            return;
        }
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (teamProfile == null) {
            return;
        }
        EnderpearlTimer enderpearlTimer = teamProfile.getTimer(EnderpearlTimer.class);
        if (enderpearlTimer == null) {
            return;
        }
        player.sendMessage(CC.RED + "You still have " + enderpearlTimer.formatDetailedRemaining() + " of enderpearl timer.");
        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) {
            return;
        }
        EnderPearl enderPearl = (EnderPearl) event.getEntity();
        if (!(enderPearl.getShooter() instanceof Player)) {
            return;
        }
        Player player = (Player) enderPearl.getShooter();
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (profile == null) {
            return;
        }


        ItemStack stack = player.getItemInHand().clone();
        stack.setAmount(1);

        profile.setLastThrownPearlItem(stack);
        profile.setLastThrownPearlEntityId(enderPearl.getEntityId());

        profile.addTimer(new EnderpearlTimer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack itemStack = event.getItem();
        Player player = event.getPlayer();

        if (itemStack.getType() == Material.GOLDEN_APPLE) {
            TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
            int durability = itemStack.getDurability();

            if (durability == 0) { //crapple
                if (!HCF.getInstance().getConfiguration().isCrappleEnabled()) {
                    player.sendMessage(CC.RED + "Crapples are disabled on this map");
                    event.setCancelled(true);
                    return;
                }

                CrappleTimer crappleTimer = teamProfile.getTimer(CrappleTimer.class);

                if (crappleTimer != null) {
                    player.sendMessage(CC.RED + "You cannot eat a crapple for " + crappleTimer.formatDetailedRemaining() + ".");
                    event.setCancelled(true);
                    return;
                }

                teamProfile.addTimer(new CrappleTimer());
            } else if (durability == 1) { //gopple
                if (!HCF.getInstance().getConfiguration().isGoppleEnabled()) {
                    player.sendMessage(CC.RED + "Gopples are disabled on this map");
                    event.setCancelled(true);
                    return;
                }

                GoppleTimer goppleTimer = teamProfile.getTimer(GoppleTimer.class);

                if (goppleTimer != null) {
                    player.sendMessage(CC.RED + "You cannot eat a gopple for " + goppleTimer.formatDetailedRemaining() + ".");
                    event.setCancelled(true);
                    return;
                }

                teamProfile.addTimer(new GoppleTimer());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        this.timerHandler.handleMovement(player, event.getFrom(), event.getTo());

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            if (event.isCancelled()) {
                TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
                if (profile == null) {
                    return;
                }

                if (profile.getLastThrownPearlItem() != null) {
                    player.getInventory().addItem(profile.getLastThrownPearlItem());
                }
            }
        }
    }

    @EventHandler
    public void onClaimChange(ClaimChangeEvent event) {
        TeamProfile profile = event.getTeamProfile();

        PvPTimer pvPTimer = profile.getTimer(PvPTimer.class);
        if (pvPTimer == null) {
            return;
        }

        if (event.getTo() == null) {
            if (!pvPTimer.isPaused()) {
                return;
            }
            pvPTimer.setPaused(false);
            profile.setSave(true);
            return;
        }

        if (event.getTo() instanceof SystemTeam) {
            SystemTeam systemTeam = (SystemTeam) event.getTo();
            if (systemTeam.isSafezone()) {
                if (pvPTimer.isPaused()) {
                    return;
                }
                pvPTimer.setPaused(true);
                profile.setSave(true);
            } else {
                if (!pvPTimer.isPaused()) {
                    return;
                }
                pvPTimer.setPaused(false);
                profile.setSave(true);
            }
        } else {
            if (!pvPTimer.isPaused()) {
                return;
            }
            pvPTimer.setPaused(false);
            profile.setSave(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(event.getPlayer());
        PvPTimer pvPTimer = profile.getTimer(PvPTimer.class);
        if (pvPTimer != null) {
            Team lastClaim = profile.getLastClaimTeam();
            if (lastClaim instanceof SystemTeam) {
                SystemTeam systemTeam = (SystemTeam) lastClaim;
                if (systemTeam.isSafezone()) {
                    pvPTimer.setPaused(true);
                    return;
                }
            }
            pvPTimer.setPaused(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(event.getPlayer());
        PvPTimer pvPTimer = profile.getTimer(PvPTimer.class);
        if (pvPTimer != null) {
            Team lastClaim = profile.getLastClaimTeam();
            if (lastClaim instanceof SystemTeam) {
                SystemTeam systemTeam = (SystemTeam) lastClaim;
                if (systemTeam.isSafezone()) {
                    pvPTimer.setPaused(true);
                    return;
                }
            }
            pvPTimer.setPaused(false);
        }
    }
}
