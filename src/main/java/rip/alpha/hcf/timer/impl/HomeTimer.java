package rip.alpha.hcf.timer.impl;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketCooldown;
import net.mcscrims.libraries.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.settings.Setting;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.timer.type.SecondsTimer;

import java.util.concurrent.TimeUnit;

public class HomeTimer extends SecondsTimer {
    public HomeTimer(int seconds) {
        super(0, false, seconds);
    }

    @Override
    public void onApply(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(CC.GREEN + "You are now teleporting to your base point");

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (!teamProfile.getSetting(Setting.LUNAR_TIMERS)) {
            return;
        }
        long remaining = TimeUnit.SECONDS.toMillis(this.getRemaining());
        LCPacketCooldown packetCooldown = new LCPacketCooldown("Home", remaining, Material.WORKBENCH.getId());
        LunarClientAPI.getInstance().sendPacket(player, packetCooldown);
    }

    @Override
    public void onExtend(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (!teamProfile.getSetting(Setting.LUNAR_TIMERS)) {
            return;
        }
        long remaining = TimeUnit.SECONDS.toMillis(this.getRemaining());
        LCPacketCooldown packetCooldown = new LCPacketCooldown("Home", remaining, Material.WORKBENCH.getId());
        LunarClientAPI.getInstance().sendPacket(player, packetCooldown);
    }

    @Override
    public void onRemove(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(CC.RED + "Your teleport timer has been cancelled");
        LCPacketCooldown packetCooldown = new LCPacketCooldown("Home", 0L, Material.WORKBENCH.getId());
        LunarClientAPI.getInstance().sendPacket(player, packetCooldown);
    }

    @Override
    public void onExpire(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        PlayerTeam team = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
        if (team == null) {
            return;
        }
        if (team.getClaim() == null) {
            return;
        }
        if (team.getHome() == null) {
            return;
        }
        Bukkit.getScheduler().callSyncMethod(HCF.getInstance(), () -> player.teleport(team.getHome()));
    }

    @Override
    public void onDecrement(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(CC.YELLOW + "You will be teleported to your HQ in " + this.getRemaining());
    }
}