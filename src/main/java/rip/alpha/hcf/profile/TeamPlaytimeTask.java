package rip.alpha.hcf.profile;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;

public class TeamPlaytimeTask implements Runnable {
    @Override
    public void run() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(online.getUniqueId());
            profile.incrementPlaytime();
        }
    }
}
