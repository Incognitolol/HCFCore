package rip.alpha.hcf.timer.impl;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketCooldown;
import net.mcscrims.libraries.util.CC;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.settings.Setting;
import rip.alpha.hcf.timer.type.TimestampTimer;

import java.util.concurrent.TimeUnit;

public class CrappleTimer extends TimestampTimer {

    public CrappleTimer() {
        this(HCF.getInstance().getConfiguration().getCrappleTimer());
    }

    public CrappleTimer(int seconds) {
        this(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds));
    }

    public CrappleTimer(long time) {
        super(10, true, time);
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
        LCPacketCooldown packetCooldown = new LCPacketCooldown("Crapple", this.getRemaining(), Material.GOLDEN_APPLE.getId());
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
        LCPacketCooldown packetCooldown = new LCPacketCooldown("Crapple", this.getRemaining(), Material.GOLDEN_APPLE.getId());
        LunarClientAPI.getInstance().sendPacket(player, packetCooldown);
    }

    @Override
    public void onRemove(TeamProfile profile) {
        Player player = profile.toPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(CC.GREEN + "You are no-longer on crapple cooldown");
        LCPacketCooldown packetCooldown = new LCPacketCooldown("Crapple", 0L, Material.GOLDEN_APPLE.getId());
        LunarClientAPI.getInstance().sendPacket(player, packetCooldown);
    }

    @Override
    public void onExpire(TeamProfile profile) {

    }

    @Override
    public void onTimerUpdate(TeamProfile profile) {

    }
}
