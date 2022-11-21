package rip.alpha.hcf.timer.impl;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketCooldown;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.settings.Setting;
import rip.alpha.hcf.timer.type.TimestampTimer;

import java.util.concurrent.TimeUnit;

public class ArcherTagTimer extends TimestampTimer {

    public ArcherTagTimer(int seconds) {
        this(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds));
    }

    public ArcherTagTimer(long time) {
        super(4, false, time);
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
        LCPacketCooldown packetCooldown = new LCPacketCooldown("Archer Tag", this.getRemaining(), Material.BOW.getId());
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
        LCPacketCooldown packetCooldown = new LCPacketCooldown("Archer Tag", this.getRemaining(), Material.BOW.getId());
        LunarClientAPI.getInstance().sendPacket(player, packetCooldown);
    }

    @Override
    public void onRemove(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        LCPacketCooldown packetCooldown = new LCPacketCooldown("Archer Tag", 0L, Material.BOW.getId());
        LunarClientAPI.getInstance().sendPacket(player, packetCooldown);
    }

    @Override
    public void onExpire(TeamProfile profile) {

    }

    @Override
    public void onTimerUpdate(TeamProfile profile) {

    }
}
