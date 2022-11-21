package rip.alpha.hcf.team.command.impl.system;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.shared.TeamCreateEvent;
import rip.alpha.hcf.team.impl.SystemTeam;

import java.util.UUID;

public class STeamCreateCommand {
    @Command(names = {"systemteam create", "systeam create", "steam create"}, async = true, permission = "op")
    public static void systemTeamCreateCommand(Player player, @Param(name = "name", wildcard = true) String name) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        Team team = teamHandler.getTeamByName(name);

        if (team != null) {
            player.sendMessage(CC.RED + "That team already exists, use /teamadmin forcedisband if its a player team");
            return;
        }

        team = new SystemTeam(UUID.randomUUID(), name, CC.WHITE);
        team.setSave(true);
        teamHandler.addTeam(team);

        player.sendMessage(CC.GREEN + "You have successfully created a system team named " + name);
        new TeamCreateEvent(player, team).call(HCF.getInstance());
    }

}
