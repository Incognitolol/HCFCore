package rip.alpha.hcf.team.listener;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.DamagerUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.other.ClaimChangeEvent;
import rip.alpha.hcf.team.impl.SystemTeam;
import rip.alpha.hcf.timer.impl.PvPTimer;

import java.util.Arrays;

@RequiredArgsConstructor
public class SystemTeamListener implements Listener {

    private final TeamHandler teamHandler;
    private final int[] debuffs = new int[]{2, 7, 18, 19, 20};

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player victim = (Player) event.getEntity();
        TeamProfile victimProfile = HCF.getInstance().getProfileHandler().getProfile(victim);
        Team victimClaimTeam = victimProfile.getLastClaimTeam();
        if (victimClaimTeam instanceof SystemTeam && ((SystemTeam) victimClaimTeam).isSafezone()) {
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                victim.teleport(victim.getWorld().getSpawnLocation());
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!event.hasItem()) {
            return;
        }
        ItemStack itemStack = event.getItem();
        if (!((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))) {
            return;
        }
        if (itemStack.getType() != Material.POTION) {
            return;
        }
        try {
            if (itemStack.getDurability() != (short) 0) {
                Potion potion = Potion.fromItemStack(itemStack);

                if (potion == null) {
                    return;
                }

                if (!potion.isSplash()) {
                    return;
                }

                if (potion.getType() == null){
                    return;
                }

                PotionEffectType potionEffectType = potion.getType().getEffectType();
                int id = potionEffectType.getId();

                if (Arrays.binarySearch(this.debuffs, id) < 0){
                    return;
                }

                TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);

                if (teamProfile.hasTimer(PvPTimer.class)) {
                    player.sendMessage(CC.RED + "You cannot do this while your PVP Timer is active!");
                    player.sendMessage(CC.RED + "Type '" + CC.YELLOW + "/pvp enable" + CC.RESET + "' to remove your timer.");
                    event.setCancelled(true);
                    return;
                }

                Team team = teamProfile.getLastClaimTeam();
                if (team instanceof SystemTeam) {
                    SystemTeam systemTeam = (SystemTeam) team;
                    if (systemTeam.isSafezone()) {

                        player.sendMessage(CC.RED + "You cannot launch debuffs from inside spawn!");
                        player.updateInventory();
                        event.setCancelled(true);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player damager = DamagerUtils.getDamager(event);
        if (damager == null) {
            return;
        }
        TeamProfile damagerProfile = HCF.getInstance().getProfileHandler().getProfile(damager);
        Team damagerClaimTeam = damagerProfile.getLastClaimTeam();
        if (damagerClaimTeam instanceof SystemTeam && ((SystemTeam) damagerClaimTeam).isSafezone()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }
        Player player = event.getPlayer();
        Team claimTeam = this.teamHandler.getTeamByLocation(event.getTo());
        if (!(claimTeam instanceof SystemTeam)) {
            return;
        }
        SystemTeam systemTeam = (SystemTeam) claimTeam;

        if (!systemTeam.isEnderpearl() || systemTeam.isSafezone()) {
            player.sendMessage(CC.RED + "You cannot enderpearl into that zone");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClaimChange(ClaimChangeEvent event) {
        if (event.getTo() instanceof SystemTeam) {
            SystemTeam systemTeam = (SystemTeam) event.getTo();
            if (!systemTeam.isSafezone()) {
                return;
            }
            Player player = event.getPlayer();
            player.setExhaustion(0);
            player.setSaturation(20);
            player.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (teamProfile == null) {
            return;
        }
        Team team = teamProfile.getLastClaimTeam();
        if (!(team instanceof SystemTeam)) {
            return;
        }
        SystemTeam systemTeam = (SystemTeam) team;
        if (!systemTeam.isSafezone()) {
            return;
        }
        if (player.getFoodLevel() < event.getFoodLevel()) {
            return;
        }
        event.setCancelled(true);
    }
}
