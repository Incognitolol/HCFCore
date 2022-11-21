package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.player.TeamRallySetEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.concurrent.TimeUnit;

public class TeamRallyCommand {
    @Command(names = {"team rally", "t rally", "f rally", "faction rally"}, async = true)
    public static void teamRallyCommand(Player player) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        team.setRallyTime(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5));
        Location location = player.getLocation();
        team.setRally(location);
        team.broadcast("&a" + player.getName() + "&e has set a rally point at &a" + location.getBlockX() + ", " + location.getBlockZ() + " (" + location.getWorld().getName() + ")&e.");

        if (team.getPreviousRallyWorldId() == null) {
            team.setPreviousRallyWorldId(location.getWorld().getUID().toString());
        }
        new TeamRallySetEvent(team).call(HCF.getInstance());
        team.setPreviousRallyWorldId(location.getWorld().getUID().toString());
    }
}
