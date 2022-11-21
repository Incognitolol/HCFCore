package rip.alpha.hcf.combatlogger;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.TaskUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.impl.SystemTeam;

@RequiredArgsConstructor
public class CombatLoggerListener implements Listener {

    private final CombatLoggerHandler handler;

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        player.removeMetadata("loggedOut", HCF.getInstance());
        CombatLoggerVillager combatLoggerVillager = this.handler.getVillager(player.getUniqueId());

        if (combatLoggerVillager == null) {
            return;
        }

        if (combatLoggerVillager.isDead()) {
            return;
        }

        combatLoggerVillager.getBukkitEntity().remove();
        player.teleport(combatLoggerVillager.getBukkitEntity().getLocation());
        player.setHealth(combatLoggerVillager.getHealth());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("loggedOut")) {
            return;
        }

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        Team team = teamProfile.getLastClaimTeam();
        if (team instanceof SystemTeam) {
            SystemTeam systemTeam = (SystemTeam) team;
            if (systemTeam.isSafezone()) {
                return;
            }
        }

        TaskUtil.runSync(() -> this.handler.spawnVillager(player), HCF.getInstance());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.hasMetadata("CombatLogger") && !entity.isDead()) {
                event.setCancelled(true);
            }
        }
    }
}
