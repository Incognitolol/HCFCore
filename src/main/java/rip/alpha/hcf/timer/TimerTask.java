package rip.alpha.hcf.timer;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.TeamProfileHandler;
import rip.alpha.hcf.timer.type.SecondsTimer;
import rip.alpha.hcf.timer.type.TimestampTimer;

import java.util.HashSet;

@RequiredArgsConstructor
public class TimerTask extends BukkitRunnable {

    private final TeamProfileHandler profileHandler;

    @Override
    public void run() {
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                TeamProfile profile = this.profileHandler.getProfile(player);
                for (Timer timer : new HashSet<>(profile.getActiveTimers().values())) {
                    if (timer instanceof SecondsTimer) {
                        SecondsTimer secondsTimer = (SecondsTimer) timer;

                        if (secondsTimer.isPaused()) {
                            continue;
                        }

                        secondsTimer.decrement();
                        secondsTimer.onDecrement(profile);
                    }

                    if (timer instanceof TimestampTimer) {
                        TimestampTimer timestampTimer = (TimestampTimer) timer;
                        timestampTimer.onTimerUpdate(profile);
                    }

                    if (!timer.isActive()) {
                        profile.removeTimer(timer);
                        timer.onExpire(profile);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
