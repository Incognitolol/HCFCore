package rip.alpha.hcf.timer.impl;

import net.mcscrims.libraries.util.CC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.timer.type.TimestampTimer;

import java.util.concurrent.TimeUnit;

public class GoppleTimer extends TimestampTimer {

    public GoppleTimer() {
        this(0, 0, HCF.getInstance().getConfiguration().getGoppleTimer());
    }

    public GoppleTimer(int hours, int mins, int seconds) {
        super(2, true, System.currentTimeMillis() +
                TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(mins) + TimeUnit.SECONDS.toMillis(seconds));
    }

    public GoppleTimer(long time) {
        super(2, true, time);
    }

    @Override
    public void onApply(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(ChatColor.DARK_GREEN + "███" + ChatColor.BLACK + "██" + ChatColor.DARK_GREEN + "███");
        player.sendMessage(ChatColor.DARK_GREEN + "███" + ChatColor.BLACK + "█" + ChatColor.DARK_GREEN + "████");
        player.sendMessage(ChatColor.DARK_GREEN + "██" + ChatColor.GOLD + "████" + ChatColor.DARK_GREEN + "██" + ChatColor.GOLD + " Gopple:");
        player.sendMessage(ChatColor.DARK_GREEN + "█" + ChatColor.GOLD + "██" + ChatColor.WHITE + "█" + ChatColor.GOLD + "███" + ChatColor.DARK_GREEN + "█" + ChatColor.DARK_GREEN + " Consumed");
        player.sendMessage(ChatColor.DARK_GREEN + "█" + ChatColor.GOLD + "█" + ChatColor.WHITE + "█" + ChatColor.GOLD + "████" + ChatColor.DARK_GREEN + "█" + ChatColor.YELLOW + " Cooldown Remaining:");
        player.sendMessage(ChatColor.DARK_GREEN + "█" + ChatColor.GOLD + "██████" + ChatColor.DARK_GREEN + "█" + ChatColor.BLUE + " " + this.formatDetailedRemaining());
        player.sendMessage(ChatColor.DARK_GREEN + "█" + ChatColor.GOLD + "██████" + ChatColor.DARK_GREEN + "█");
        player.sendMessage(ChatColor.DARK_GREEN + "██" + ChatColor.GOLD + "████" + ChatColor.DARK_GREEN + "██");
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
        player.sendMessage(CC.GREEN + "You are no-longer on gapple cooldown");
    }

    @Override
    public void onExpire(TeamProfile profile) {

    }

    @Override
    public void onTimerUpdate(TeamProfile profile) {

    }
}
