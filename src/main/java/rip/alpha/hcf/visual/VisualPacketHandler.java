package rip.alpha.hcf.visual;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.mcscrims.libraries.util.BlockUtil;
import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInBlockDig;
import net.minecraft.server.v1_7_R4.PacketPlayOutBlockChange;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import org.bukkit.Material;
import rip.foxtrot.spigot.handler.PacketHandler;

import java.util.UUID;

public class VisualPacketHandler implements PacketHandler {

    private final VisualHandler visualHandler;
    private final IntSet whitelistedIds;

    public VisualPacketHandler(VisualHandler visualHandler) {
        this.visualHandler = visualHandler;
        this.whitelistedIds = new IntOpenHashSet();

        this.whitelistedIds.add(25);
        this.whitelistedIds.add(95);
        for (Material material : BlockUtil.DISPLAYABLE_BLOCKS) {
            this.whitelistedIds.add(material.getId());
        }
    }

    @Override
    public void handleReceivedPacket(PlayerConnection playerConnection, Packet packet) {
        if (packet instanceof PacketPlayInBlockDig) {
            PacketPlayInBlockDig packetPlayInBlockDig = (PacketPlayInBlockDig) packet;
            int x = packetPlayInBlockDig.c();
            int y = packetPlayInBlockDig.d();
            int z = packetPlayInBlockDig.e();
            String worldName = playerConnection.getPlayer().getWorld().getName();
            UUID uuid = playerConnection.getPlayer().getUniqueId();
            VisualBlockEntry entry = this.visualHandler.getVisualBlockEntry(uuid, worldName, x, y, z);
            if (entry != null) {
                entry.setSent(false);
                playerConnection.getPlayer().sendBlockChange(entry.toLocation(), entry.getMaterial(), entry.getData());
            }
        }
    }

    @Override
    public boolean handleSentPacketCancellable(PlayerConnection connection, Packet packet) {
        if (packet instanceof PacketPlayOutBlockChange) {
            PacketPlayOutBlockChange packetPlayOutBlockChange = (PacketPlayOutBlockChange) packet;
            int id = Block.getId(packetPlayOutBlockChange.block);
            if (this.whitelistedIds.contains(id) || BlockUtil.isNonSolidBlock(id)) {
                int x = packetPlayOutBlockChange.a;
                int y = packetPlayOutBlockChange.b;
                int z = packetPlayOutBlockChange.c;
                String worldName = connection.getPlayer().getWorld().getName();
                UUID uuid = connection.getPlayer().getUniqueId();
                VisualBlockEntry entry = this.visualHandler.getVisualBlockEntry(uuid, worldName, x, y, z);
                if (entry != null) {
                    if (entry.isRemove()) {
                        return true;
                    }

                    if (entry.isSent()) {
                        return false;
                    }

                    entry.setSent(true);
                }
            }
        }
        return true;
    }

    @Override
    public boolean handlesCancellable() {
        return true;
    }
}
