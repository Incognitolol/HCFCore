package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.command.TeamCommandConstants;
import rip.alpha.hcf.team.event.shared.TeamCreateEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TeamCreateCommand {
    @Command(names = {"team create", "t create", "f create", "faction create"}, async = true)
    public static void teamCreate(Player player, @Param(name = "teamName") String name) {
        if (name.length() > 16) {
            player.sendMessage(CC.RED + "Your team name cannot be above 16 characters.");
            return;
        }

        if (name.length() < 3) {
            player.sendMessage(CC.RED + "Your team name cannot be under 3 characters.");
            return;
        }

        if (TeamCommandConstants.ALPHANUMERIC_PATTERN.matcher(name).find()) {
            player.sendMessage(CC.RED + "Team names must be alphanumeric.");
            return;
        }

        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        if (teamHandler.isBlacklistedTeamName(name)) {
            player.sendMessage(CC.RED + name + " is a blacklisted team name!");
            return;
        }

        PlayerTeam playerTeam = teamHandler.getPlayerTeamByPlayer(player);

        if (playerTeam != null) {
            player.sendMessage(CC.RED + "You are already in a team, Please leave your current team");
            return;
        }

        Team team = teamHandler.getTeamByName(name);

        if (team != null) {
            player.sendMessage(CC.RED + "There is already a team with this name ");
            return;
        }

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player.getUniqueId());

        if (teamProfile.getTeamCreateCooldown() - System.currentTimeMillis() > 0) {
            player.sendMessage(CC.RED + "You are currently on team create cooldown.");
            return;
        }

        teamProfile.setTeamCreateCooldown(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10));

        playerTeam = new PlayerTeam(UUID.randomUUID(), name, player.getUniqueId(), player.getName());
        playerTeam.setSave(true);
        teamHandler.addTeam(playerTeam);

        new TeamCreateEvent(player, playerTeam).call(HCF.getInstance());
    }
}
