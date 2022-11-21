package rip.alpha.hcf.timer.impl;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketCooldown;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.settings.Setting;
import rip.alpha.hcf.timer.type.SecondsTimer;

import java.util.concurrent.TimeUnit;

public class CombatTagTimer extends SecondsTimer {

    public CombatTagTimer() {
        this(HCF.getInstance().getConfiguration().getCombatTagTimer());
    }

    public CombatTagTimer(int seconds) {
        super(3, true, seconds);
    }

    @Override
    public void onApply(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (!teamProfile.getSetting(Setting.LUNAR_TIMERS)) {
            return;
        }
        long remaining = TimeUnit.SECONDS.toMillis(this.getRemaining());
        LCPacketCooldown packetCooldown = new LCPacketCooldown("Combat Tag", remaining, Material.DIAMOND_SWORD.getId());
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
        LCPacketCooldown packetCooldown = new LCPacketCooldown("Combat Tag", remaining, Material.DIAMOND_SWORD.getId());
        LunarClientAPI.getInstance().sendPacket(player, packetCooldown);
    }

    @Override
    public void onRemove(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        LCPacketCooldown packetCooldown = new LCPacketCooldown("Combat Tag", 0L, Material.DIAMOND_SWORD.getId());
        LunarClientAPI.getInstance().sendPacket(player, packetCooldown);
    }

    @Override
    public void onExpire(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
    }

    @Override
    public void onDecrement(TeamProfile profile) {

    }
}
