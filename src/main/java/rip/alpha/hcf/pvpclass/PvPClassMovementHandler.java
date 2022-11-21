package rip.alpha.hcf.pvpclass;

import net.minecraft.server.v1_7_R4.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.foxtrot.spigot.handler.MovementHandler;

public class PvPClassMovementHandler implements MovementHandler {
    @Override
    public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packetPlayInFlying) {
        if (from.getBlockZ() == to.getBlockZ() && from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()) {
            return;
        }
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (profile.getEquipPvPClass() != null) {
            PvPClass pvPClass = HCF.getInstance().getPvPClassHandler().getPvPClass(profile.getEquipPvPClass());
            pvPClass.handleMove(player, from, to);
        }
    }

    @Override
    public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {

    }
}
