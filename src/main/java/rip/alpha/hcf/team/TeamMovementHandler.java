package rip.alpha.hcf.team;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_7_R4.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import rip.foxtrot.spigot.handler.MovementHandler;

@RequiredArgsConstructor
public class TeamMovementHandler implements MovementHandler {

    private final TeamHandler teamHandler;

    @Override
    public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packetPlayInFlying) {
        boolean b = this.teamHandler.handleMove(player, from, to);
        if (b) {
            int x = from.getBlockX();
            int y = from.getBlockY();
            int z = from.getBlockZ();
            Location updated = from.clone();
            updated.setX(x);
            updated.setY(y);
            updated.setZ(z);
            player.teleport(updated);
        }
    }

    @Override
    public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {

    }
}
