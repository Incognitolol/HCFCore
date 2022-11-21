package rip.alpha.hcf.team.listener;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TaskUtil;
import net.mcscrims.punishments.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.shared.TeamCreateEvent;
import rip.alpha.hcf.team.event.shared.TeamDisbandEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

@RequiredArgsConstructor
public class TeamListener implements Listener {

    private static final String CREATE_TEAM_FORMAT = CC.translate("&a%s has created a new team named %s");
    private static final String DISBAND_TEAM_FORMAT = CC.translate("&e%s&c has been disbanded by &a%s");

    private final TeamHandler teamHandler;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerTeam team = this.teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team");
            return;
        }

        TaskUtil.runAsync(() -> {
            team.updateName(player.getUniqueId(), player.getName());
            team.sendTeamInfo(player);
            team.broadcast("&a" + player.getName() + " &ehas logged on.");
        }, HCF.getInstance());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerTeam team = this.teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            return;
        }

        team.broadcast("&c" + player.getName() + "&e has logged off.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        boolean b = this.teamHandler.handleMove(player, from, to);
        if (b) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTeamCreate(TeamCreateEvent event) {
        Team team = event.getTeam();
        Player player = event.getPlayer();

        if (!PlayerListener.CHAT_ENABLED) {
            return;
        }

        if (team instanceof PlayerTeam) {
            PlayerTeam playerTeam = (PlayerTeam) team;
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.sendMessage(String.format(CREATE_TEAM_FORMAT, player.getName(), playerTeam.getDisplayName(online)));
            }
        }
    }

    @EventHandler
    public void onTeamDisband(TeamDisbandEvent event) {
        Team team = event.getTeam();
        Player player = event.getPlayer();
        if (!PlayerListener.CHAT_ENABLED) {
            return;
        }

        if (team instanceof PlayerTeam) {
            PlayerTeam playerTeam = (PlayerTeam) team;
            playerTeam.broadcast("", "&a" + player.getName() + "&c has disbanded your team!", PlayerTeam.TeamMember.TEAM_MEMBER);
            Bukkit.getOnlinePlayers().stream().filter(online -> playerTeam.getMember(online.getUniqueId()) == null)
                    .forEach(online -> online.sendMessage(String.format(DISBAND_TEAM_FORMAT, playerTeam.getDisplayName(online), player.getName())));
        }
    }
}
