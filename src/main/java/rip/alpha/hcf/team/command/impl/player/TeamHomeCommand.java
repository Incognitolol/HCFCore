package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.impl.SystemTeam;
import rip.alpha.hcf.timer.impl.CombatTagTimer;
import rip.alpha.hcf.timer.impl.HomeTimer;
import rip.alpha.hcf.timer.impl.PvPTimer;

public class TeamHomeCommand {
    @Command(names =
            {
                    "team home", "t home", "f home", "faction home",
                    "team hq", "t hq", "f hq", "faction hq", "hq"
            }, async = true)
    public static void teamHome(Player player) {

        if (player.getWorld().getName().equalsIgnoreCase("world_the_end")) {
            player.sendMessage(CC.RED + "You cannot f home in the end.");
            return;
        }

        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        if (team.getClaim() == null) {
            player.sendMessage(CC.RED + "Your team doesn't have a claim, use /team claim to begin claiming");
            return;
        }

        if (team.getHome() == null) {
            player.sendMessage(CC.RED + "Your team doesn't have a home, use /team sethome to set a home");
            return;
        }

        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);

        if (profile.hasTimer(PvPTimer.class)) {
            player.sendMessage(CC.RED + "You have pvp timer, you cannot teleport while having pvp timer.");
            return;
        }

        if (profile.hasTimer(HomeTimer.class)) {
            player.sendMessage(CC.RED + "You are already teleporting home");
            return;
        }

        if (profile.hasTimer(CombatTagTimer.class)) {
            player.sendMessage(CC.RED + "You cannot teleport whilst combat tagged.");
            return;
        }

        int currentTaggedMembers = 0;
        for (PlayerTeam.TeamMember member : team.getOnlineMembers()) {
            TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(member.getUuid());
            if (teamProfile.hasTimer(CombatTagTimer.class)) {
                currentTaggedMembers++;
            }
        }

        if (currentTaggedMembers >= 10) {
            player.sendMessage(CC.RED + "You cannot use team home while 10 or more members of your team are combat tagged.");
            return;
        }


        Team lastTeamClaim = profile.getLastClaimTeam();
        if (lastTeamClaim instanceof PlayerTeam) {
            PlayerTeam playerTeam = (PlayerTeam) lastTeamClaim;
            if (!playerTeam.getId().equals(team.getId())) {
                player.sendMessage(CC.RED + "You cannot /team home in another teams claim, use /team stuck");
                return;
            }
        } else if (lastTeamClaim instanceof SystemTeam) {
            SystemTeam systemTeam = (SystemTeam) lastTeamClaim;

            if (!systemTeam.isCanHome()) {
                player.sendMessage(CC.RED + "You cannot /team home in this system team claim");
                return;
            }

            if (systemTeam.isSafezone()) {
                new HomeTimer(1).addTimer(profile);
                return;
            }
        }

        Location location = player.getLocation();
        World world = location.getWorld();

        if (world.getEnvironment() == World.Environment.NETHER) {
            new HomeTimer(20).addTimer(profile);
        } else {
            new HomeTimer(10).addTimer(profile);
        }
    }
}
