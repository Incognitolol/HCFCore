package rip.alpha.hcf.timer.impl;

import lombok.Getter;
import net.mcscrims.libraries.util.CC;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.timer.type.SecondsTimer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Getter
public class StuckTimer extends SecondsTimer {
    private static final Random RANDOM = new Random();

    private Location startLocation = null;

    public StuckTimer(int mins, Location location) {
        this((int) (TimeUnit.MINUTES.toSeconds(mins)));
        this.startLocation = location;
    }

    public StuckTimer(int seconds) {
        super(9, false, seconds);
    }

    @Override
    public void onApply(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(CC.GREEN + "You have started your stuck timer.");
    }

    @Override
    public void onExtend(TeamProfile profile) {

    }

    @Override
    public void onRemove(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(CC.RED + "Your stuck timer has been cancelled.");
    }

    @Override
    public void onExpire(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        Location playerLocation = player.getLocation();

        if (!this.startLocation.getWorld().equals(playerLocation.getWorld())) {
            return;
        }

        if (playerLocation.distanceSquared(this.startLocation) > 25) {
            player.sendMessage(CC.RED + "Your teleport was cancelled because you have moved more than 5 blocks");
            return;
        }


        Team currentTeamClaim = profile.getLastClaimTeam();

        if (currentTeamClaim == null) {
            player.sendMessage(CC.RED + "You are attempting to teleport from no claim.");
            return;
        }

        HCF.getInstance().getTimerHandler().teleportToSafeLocation(player, currentTeamClaim);
    }

    @Override
    public void onDecrement(TeamProfile profile) {

    }

    public Location getTeleportLocation(World world, String[] locationStringArray) {
        int x = Integer.parseInt(locationStringArray[0]);
        int z = Integer.parseInt(locationStringArray[1]);
        int y = (int) (world.getHighestBlockYAt(x, z) + 1.5);

        return new Location(world, x, y, z);
    }

    public String[] getRandomLocation(List<String> possibleLocations) {
        return possibleLocations.get(RANDOM.nextInt(possibleLocations.size())).split(",");
    }
}
