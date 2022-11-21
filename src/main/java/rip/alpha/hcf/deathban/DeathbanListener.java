package rip.alpha.hcf.deathban;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.BungeeUtil;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TimeUtil;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.EnumClientCommand;
import net.minecraft.server.v1_7_R4.PacketPlayInClientCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.settings.Setting;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.timer.impl.ArcherTagTimer;
import rip.alpha.hcf.timer.impl.CombatTagTimer;
import rip.alpha.hcf.timer.impl.EnderpearlTimer;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class DeathbanListener implements Listener {

    private final DeathbanHandler handler;
    private final Executor executor = Executors.newFixedThreadPool(2);

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(event.getUniqueId());
        if (profile == null) {
            return;
        }

        if (profile.isDeathban()) {
            if (profile.getLives() > 0) {
                if (profile.onLivesCooldown()) {
                    if (profile.canUseLife()) {
                        profile.setLives(profile.getLives() - 1);
                        profile.setLivesCooldown(-1);
                        profile.setDeathbanTime(-1);
                        profile.setUsedLife(true);
                        profile.setSave(true);
                    } else {
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, CC.YELLOW + "You are currently on lives cooldown!");
                    }
                    return;
                }

                profile.setLivesCooldown(System.currentTimeMillis() + 30000);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, CC.YELLOW + "You are currently death-banned for " + TimeUtil.formatLongIntoDetailedString(TimeUnit.MILLISECONDS.toSeconds(profile.remainingDeathbanTime())) +
                        "\nYou have available" + CC.GOLD + profile.getLives() + CC.YELLOW + " lives to use. To use one, rejoin within 30 seconds.");
                return;
            }

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, TimeUtil.formatLongIntoDetailedString(TimeUnit.MILLISECONDS.toSeconds(profile.remainingDeathbanTime())));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (teamProfile == null) {
            return;
        }
        if (!(teamProfile.isUsedLife())) {
            return;
        }
        player.sendMessage(CC.GREEN + "You have used one life.");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        UUID killerId = killer == null ? null : killer.getUniqueId();
        Location location = player.getLocation();

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player.getUniqueId());
        TeamProfile killerProfile = null;
        if (killerId != null) {
            killerProfile = HCF.getInstance().getProfileHandler().getProfile(killerId);
        }

        this.handler.handleDeath(teamProfile, killerProfile, location);

        event.setDeathMessage(null);
        EntityDamageEvent damageEvent = player.getLastDamageCause();
        if (damageEvent != null) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                TeamProfile targetProfile = HCF.getInstance().getProfileHandler().getProfile(target.getUniqueId());
                if (targetProfile.getSetting(Setting.DEATH_MESSAGES)) {
                    try {
                        target.sendMessage(this.handler.getDeathMessage(player.getUniqueId(), killerId, damageEvent.getCause()));
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }
            }
        }

        if (HCF.getInstance().getConfiguration().isKitmap()) {

            this.executor.execute(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                CraftPlayer craftPlayer = ((CraftPlayer) player);
                EntityPlayer entityPlayer = craftPlayer.getHandle();
                PacketPlayInClientCommand respawnCommand = new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN);
                entityPlayer.playerConnection.networkManager.k.add(respawnCommand);
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (profile != null) {
            HCF.getInstance().getServer().getScheduler().scheduleAsyncDelayedTask(HCF.getInstance(), () -> {
                profile.removeTimer(CombatTagTimer.class);
                profile.removeTimer(ArcherTagTimer.class);
                profile.removeTimer(EnderpearlTimer.class);
            }, 2L);

            if (profile.isDeathban()) {
                BungeeUtil.sendToServer(player, "lobby");
            }

            Team team = HCF.getInstance().getTeamHandler().getTeamByLocation(event.getRespawnLocation());
            if (team != null) {
                profile.setLastTeamClaim(team.getId());
            }
        }
    }
}
