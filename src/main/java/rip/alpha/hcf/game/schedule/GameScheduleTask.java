package rip.alpha.hcf.game.schedule;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.settings.Setting;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class GameScheduleTask implements Runnable {

    private final GameScheduleHandler handler;
    private final LongSet broadcastTimes;

    public GameScheduleTask(GameScheduleHandler handler) {
        this.handler = handler;
        this.broadcastTimes = new LongOpenHashSet(5);
        this.broadcastTimes.add(10800L); //3 hours
        this.broadcastTimes.add(7200L); //2 hours
        this.broadcastTimes.add(3600L); //1 hours
        this.broadcastTimes.add(1800L); //30 mins
        this.broadcastTimes.add(900L); //15 mins
    }

    @Override
    public void run() {
        GameScheduleEntry gameScheduleEntry = handler.getNextScheduledGame();
        if (gameScheduleEntry == null) {
            return;
        }

        if (!gameScheduleEntry.canBeStarted()) {
            long seconds = TimeUnit.MILLISECONDS.toSeconds(gameScheduleEntry.remainingTime());
            if (this.broadcastTimes.contains(seconds) && gameScheduleEntry.getLastTime() != seconds) {
                gameScheduleEntry.setLastTime(seconds);
                this.broadcastMessage(gameScheduleEntry.getColorName() +
                        " &6starts in &e" + TimeUtil.formatLongIntoDetailedString(seconds));
            }

            return;
        }

        this.handler.startGame(gameScheduleEntry);
    }

    private void broadcastMessage(String message) {
        message = CC.translate(CC.KOTH + message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
            if (profile == null) {
                continue;
            }
            if (!profile.getSetting(Setting.KOTH_MESSAGES)) {
                continue;
            }
            player.sendMessage(message);
        }
    }
}
