package rip.alpha.hcf.timer;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_7_R4.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import rip.foxtrot.spigot.handler.MovementHandler;

@RequiredArgsConstructor
public class TimerMovementHandler implements MovementHandler {

    private final TimerHandler timerHandler;

    @Override
    public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packetPlayInFlying) {
        this.timerHandler.handleMovement(player, from, to);
    }

    @Override
    public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {

    }
}
