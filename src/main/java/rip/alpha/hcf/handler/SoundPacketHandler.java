package rip.alpha.hcf.handler;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import rip.foxtrot.spigot.handler.PacketHandler;

/**
 * Class made to remove any unwanted sound packets!
 */
public class SoundPacketHandler implements PacketHandler {
    @Override
    public boolean handleSentPacketCancellable(PlayerConnection connection, Packet packet) {
        if (packet instanceof PacketPlayOutNamedSoundEffect) {
            PacketPlayOutNamedSoundEffect packetPlayOutNamedSoundEffect = (PacketPlayOutNamedSoundEffect) packet;
            String soundName = packetPlayOutNamedSoundEffect.a;
            if (soundName.contains("ghast")) {
                return false; //dont send ghast sounds because #retarded
            }
        }
        return true;
    }

    @Override
    public boolean handlesCancellable() {
        return true;
    }
}
