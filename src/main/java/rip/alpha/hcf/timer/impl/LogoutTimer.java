package rip.alpha.hcf.timer.impl;

import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TaskUtil;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.timer.type.TimestampTimer;

import java.util.concurrent.TimeUnit;

public class LogoutTimer extends TimestampTimer {

    public LogoutTimer(int seconds) {
        this(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds));
    }

    public LogoutTimer(long time) {
        super(6, false, time);
    }

    @Override
    public void onApply(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(CC.GREEN + "You have started your logout timer.");
    }

    @Override
    public void onExtend(TeamProfile profile) {

    }

    @Override
    public void onRemove(TeamProfile profile) {

    }

    @Override
    public void onExpire(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        TaskUtil.runSync(() -> {
            player.setMetadata("loggedOut", new FixedMetadataValue(HCF.getInstance(), true));
            player.kickPlayer(CC.RED + "Successfully logged out");
        }, HCF.getInstance());
    }

    @Override
    public void onTimerUpdate(TeamProfile profile) {

    }
}
