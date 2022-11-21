package rip.alpha.hcf.profile;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.Team;

@RequiredArgsConstructor
public class TeamProfileListener implements Listener {

    private final TeamProfileHandler handler;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPreLoginEventLow(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        TeamProfile profile = handler.getProfile(event.getUniqueId());

        if (profile != null) {
            profile.setRemove(false);
        } else {
            profile = handler.createProfile(event.getUniqueId());
            this.handler.loadProfile(profile);
        }

        this.handler.addProfile(profile);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPreLoginEventHigh(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            TeamProfile profile = this.handler.getProfile(event.getUniqueId());
            if (profile == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.RED + "Your team profile failed to load");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            TeamProfile profile = this.handler.getProfile(event.getPlayer());
            if (profile != null) {
                profile.setRemove(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoginMonitor(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            Player player = event.getPlayer();
            TeamProfile profile = this.handler.getProfile(player);
            if (profile != null) {
                profile.setRemove(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        TeamProfile profile = this.handler.getProfile(player);

        if (profile == null) {
            player.kickPlayer(CC.RED + "Your team profile failed to load");
            return;
        }

        profile.setRemove(false);
        Team team = HCF.getInstance().getTeamHandler().getTeamByLocation(player.getLocation());
        if (team != null) {
            profile.setLastTeamClaim(team.getId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        TeamProfile profile = this.handler.getProfile(event.getPlayer().getUniqueId());
        profile.setPillars(false);
        profile.setClaimingFor(null);
        profile.setClaimingForGame(null);
        profile.setLastTeamClaim(null);
        profile.setSave(true);
        profile.setRemove(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player.getUniqueId());
            if (Math.max(0, teamProfile.getKickCooldown() - System.currentTimeMillis()) > 0) {
                event.setCancelled(true);
            }
        }
    }
}
