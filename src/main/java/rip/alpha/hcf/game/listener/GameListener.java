package rip.alpha.hcf.game.listener;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.event.LCPlayerRegisterEvent;
import com.lunarclient.bukkitapi.nethandler.shared.LCPacketWaypointRemove;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.cuboid.Cuboid;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.game.CapturableGame;
import rip.alpha.hcf.game.Game;
import rip.alpha.hcf.game.GameHandler;
import rip.alpha.hcf.game.impl.CTPGame;
import rip.alpha.hcf.game.impl.KoTHGame;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.event.shared.TeamDisbandEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.impl.SystemTeam;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class GameListener implements Listener {

    private final GameHandler gameHandler;

    @EventHandler
    public void onTeamDisband(TeamDisbandEvent event) {
        Team team = event.getTeam();
        if (team instanceof SystemTeam) {
            SystemTeam systemTeam = (SystemTeam) team;
            Game game = systemTeam.getGame();
            if (game instanceof CapturableGame) {
                this.gameHandler.getGameMap().remove(game.getId());
                this.gameHandler.getGamesCollection().deleteOne("id", game.getId().toString());
            }
        } else if (team instanceof PlayerTeam) {
            for (CTPGame game : this.gameHandler.getGamesByType(CTPGame.class)) {
                game.removePointEntry(team.getId());
            }
        }
    }

    @EventHandler
    public void onLCPlayerRegisterEvent(LCPlayerRegisterEvent event) {
        Player player = event.getPlayer();
        List<Game> games = new ArrayList<>(HCF.getInstance().getGameHandler().getGames());
        games.removeIf(game -> game.getName() == null);
        games.removeIf(game -> !game.isActive());

        games.removeIf(game -> {
            if (game instanceof CapturableGame) {
                CapturableGame capturableGame = (CapturableGame) game;
                return capturableGame.getCaptureCuboid() == null;
            }
            return false;
        });

        if (games.isEmpty()) {
            return;
        }

        for (Game game : games) {
            if (game instanceof KoTHGame) {
                KoTHGame koTHGame = (KoTHGame) game;
                Cuboid cuboid = koTHGame.getCaptureCuboid();

                LCWaypoint waypoint = new LCWaypoint(koTHGame.waypointNameColor + koTHGame.getName(), cuboid.getCenter(), koTHGame.waypointColor.getRGB(), false, true);
                LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove(koTHGame.waypointNameColor + koTHGame.getName(), cuboid.getWorld().getUID().toString());
                LunarClientAPI.getInstance().sendPacket(player, removePacket);
                LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
            } else if (game instanceof CTPGame) {
                CTPGame ctpGame = (CTPGame) game;
                Cuboid cuboid = ctpGame.getCaptureCuboid();

                LCWaypoint waypoint = new LCWaypoint(ctpGame.waypointNameColor + ctpGame.getName(), cuboid.getCenter(), ctpGame.waypointColor.getRGB(), false, true);
                LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove(ctpGame.waypointNameColor + ctpGame.getName(), cuboid.getWorld().getUID().toString());
                LunarClientAPI.getInstance().sendPacket(player, removePacket);
                LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
            }
        }
    }
}
