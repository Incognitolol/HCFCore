package rip.alpha.hcf.timer.impl;

import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.impl.SystemTeam;
import rip.alpha.hcf.timer.type.SecondsTimer;

import java.util.concurrent.TimeUnit;

public class PvPTimer extends SecondsTimer {

    public PvPTimer() {
        this(0, 30, 0);
    }

    public PvPTimer(int seconds) {
        super(1, true, seconds);
    }

    public PvPTimer(int hours, int mins, int seconds) {
        this(seconds);
        seconds += TimeUnit.HOURS.toSeconds(hours);
        seconds += TimeUnit.MINUTES.toSeconds(mins);
        this.setSeconds(seconds);
    }

    @Override
    public void onApply(TeamProfile profile) {
        Team lastClaim = profile.getLastClaimTeam();
        if (lastClaim != null) {
            if (lastClaim instanceof SystemTeam) {
                SystemTeam systemTeam = (SystemTeam) lastClaim;
                if (systemTeam.isSafezone()) {
                    this.setPaused(true);
                }
            }
        }
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
        player.sendMessage(CC.RED + "Your pvp protection has been removed");
    }

    @Override
    public void onExpire(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(CC.RED + "Your pvp protection has been expired");
    }

    @Override
    public void onDecrement(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }

        int seconds = this.getSeconds();
        int modular = seconds > 60 ? (60 * 5) : (seconds > 10) ? 10 : 1;
        if (seconds % modular == 0L) {
            player.sendMessage(CC.translate("&aYour pvp protection will expire in &f" + this.formatDetailedRemaining()));
        }
    }
}
