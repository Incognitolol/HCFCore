package rip.alpha.hcf.listener;

import net.mcscrims.libraries.util.BlockUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.timer.impl.EnderpearlTimer;

import java.util.ArrayList;
import java.util.List;

public class EnderpearlListener implements Listener {
    private final List<BlockFace> sides = new ArrayList<>();

    public EnderpearlListener() {
        this.sides.add(BlockFace.NORTH);
        this.sides.add(BlockFace.EAST);
        this.sides.add(BlockFace.SOUTH);
        this.sides.add(BlockFace.WEST);
    }

    @EventHandler
    public void onEnderpearlTeleport(PlayerTeleportEvent event) {
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            Location landLoc = event.getTo();

            if ((landLoc.getY() - landLoc.getBlockY() >= 0.5D) && (BlockUtil.isNonSolidBlock(landLoc.getBlock().getRelative(BlockFace.UP)))) {
                landLoc.add(0.0D, 0.5D, 0.0D);
            }

            Block block = landLoc.getBlock();

            if (block == null) {
                return;
            }
            if (block.getType() == Material.SNOW) {
                block = block.getRelative(BlockFace.DOWN);
            }
            if (block == null) {
                return;
            }

            if ((block.getType() == Material.AIR) || (block.isLiquid())) {
                landLoc.setX(block.getX() + 0.5D);
                landLoc.setY(block.getY());
                landLoc.setZ(block.getZ() + 0.5D);
            } else if ((block.getType() == Material.IRON_DOOR_BLOCK) || (block.getType() == Material.WOODEN_DOOR)) {
                landLoc.setX(block.getX() + 0.5D);
                landLoc.setY(block.getY());
                landLoc.setZ(block.getZ() + 0.5D);
            } else {
                double x = landLoc.getX() >= 0.0D ? landLoc.getX() - (int) landLoc.getX() : 1.0D + (landLoc.getX() - (int) landLoc.getX());
                double z = landLoc.getZ() >= 0.0D ? landLoc.getZ() - (int) landLoc.getZ() : 1.0D + (landLoc.getZ() - (int) landLoc.getZ());

                boolean xIsSafe = false;
                boolean zIsSafe = false;
                if ((x >= 0.5D) && (z >= 0.5D)) {
                    Block xaxis = block.getRelative(BlockFace.EAST);
                    Block zaxis = block.getRelative(BlockFace.SOUTH);

                    if (xaxis.isEmpty()) {
                        xIsSafe = true;
                    }
                    if (zaxis.isEmpty()) {
                        zIsSafe = true;
                    }

                    if ((zIsSafe) && (xIsSafe)) {
                        if (xaxis.getRelative(BlockFace.DOWN).isEmpty()) {
                            landLoc.setX(zaxis.getX() + 0.5D);
                            landLoc.setZ(zaxis.getZ() + 0.5D);
                        } else if (zaxis.getRelative(BlockFace.DOWN).isEmpty()) {
                            landLoc.setX(xaxis.getX() + 0.5D);
                            landLoc.setZ(xaxis.getZ() + 0.5D);
                        } else if ((int) (Math.random() * 2) == 0) {
                            landLoc.setX(zaxis.getX() + 0.5D);
                            landLoc.setZ(zaxis.getZ() + 0.5D);
                        } else {
                            landLoc.setX(xaxis.getX() + 0.5D);
                            landLoc.setZ(xaxis.getZ() + 0.5D);
                        }
                    } else if (zIsSafe) {
                        landLoc.setX(zaxis.getX() + 0.5D);
                        landLoc.setZ(zaxis.getZ() + 0.5D);
                    } else if (xIsSafe) {
                        landLoc.setX(xaxis.getX() + 0.5D);
                        landLoc.setZ(xaxis.getZ() + 0.5D);
                    }
                } else if ((x < 0.5D) && (z >= 0.5D)) {
                    Block xaxis = block.getRelative(BlockFace.WEST);
                    Block zaxis = block.getRelative(BlockFace.SOUTH);

                    if (zaxis.isEmpty()) {
                        zIsSafe = true;
                    }
                    if (xaxis.isEmpty()) {
                        xIsSafe = true;
                    }

                    if ((zIsSafe) && (xIsSafe)) {
                        if (xaxis.getRelative(BlockFace.DOWN).isEmpty()) {
                            landLoc.setX(zaxis.getX() + 0.5D);
                            landLoc.setZ(zaxis.getZ() + 0.5D);
                        } else if (zaxis.getRelative(BlockFace.DOWN).isEmpty()) {
                            landLoc.setX(xaxis.getX() + 0.5D);
                            landLoc.setZ(xaxis.getZ() + 0.5D);
                        } else if ((int) (Math.random() * 2) == 0) {
                            landLoc.setX(zaxis.getX() + 0.5D);
                            landLoc.setZ(zaxis.getZ() + 0.5D);
                        } else {
                            landLoc.setX(xaxis.getX() + 0.5D);
                            landLoc.setZ(xaxis.getZ() + 0.5D);
                        }
                    } else if (zIsSafe) {
                        landLoc.setX(zaxis.getX() + 0.5D);
                        landLoc.setZ(zaxis.getZ() + 0.5D);
                    } else if (xIsSafe) {
                        landLoc.setX(xaxis.getX() + 0.5D);
                        landLoc.setZ(xaxis.getZ() + 0.5D);
                    }
                } else if ((x >= 0.5D) && (z < 0.5D)) {
                    Block xaxis = block.getRelative(BlockFace.EAST);
                    Block zaxis = block.getRelative(BlockFace.NORTH);

                    if (xaxis.isEmpty()) {
                        xIsSafe = true;
                    }
                    if (zaxis.isEmpty()) {
                        zIsSafe = true;
                    }

                    if ((zIsSafe) && (xIsSafe)) {
                        if (xaxis.getRelative(BlockFace.DOWN).isEmpty()) {
                            landLoc.setX(zaxis.getX() + 0.5D);
                            landLoc.setZ(zaxis.getZ() + 0.5D);
                        } else if (zaxis.getRelative(BlockFace.DOWN).isEmpty()) {
                            landLoc.setX(xaxis.getX() + 0.5D);
                            landLoc.setZ(xaxis.getZ() + 0.5D);
                        } else if ((int) (Math.random() * 2) == 0) {
                            landLoc.setX(zaxis.getX() + 0.5D);
                            landLoc.setZ(zaxis.getZ() + 0.5D);
                        } else {
                            landLoc.setX(xaxis.getX() + 0.5D);
                            landLoc.setZ(xaxis.getZ() + 0.5D);
                        }
                    } else if (zIsSafe) {
                        landLoc.setX(zaxis.getX() + 0.5D);
                        landLoc.setZ(zaxis.getZ() + 0.5D);
                    } else if (xIsSafe) {
                        landLoc.setX(xaxis.getX() + 0.5D);
                        landLoc.setZ(xaxis.getZ() + 0.5D);
                    }
                } else if ((x < 0.5D) && (z < 0.5D)) {
                    Block xaxis = block.getRelative(BlockFace.WEST);
                    Block zaxis = block.getRelative(BlockFace.NORTH);

                    if (xaxis.isEmpty()) {
                        xIsSafe = true;
                    }
                    if (zaxis.isEmpty()) {
                        zIsSafe = true;
                    }

                    if ((zIsSafe) && (xIsSafe)) {
                        if (xaxis.getRelative(BlockFace.DOWN).isEmpty()) {
                            landLoc.setX(zaxis.getX() + 0.5D);
                            landLoc.setZ(zaxis.getZ() + 0.5D);
                        } else if (zaxis.getRelative(BlockFace.DOWN).isEmpty()) {
                            landLoc.setX(xaxis.getX() + 0.5D);
                            landLoc.setZ(xaxis.getZ() + 0.5D);
                        } else if ((int) (Math.random() * 2) == 0) {
                            landLoc.setX(zaxis.getX() + 0.5D);
                            landLoc.setZ(zaxis.getZ() + 0.5D);
                        } else {
                            landLoc.setX(xaxis.getX() + 0.5D);
                            landLoc.setZ(xaxis.getZ() + 0.5D);
                        }
                    } else if (zIsSafe) {
                        landLoc.setX(zaxis.getX() + 0.5D);
                        landLoc.setZ(zaxis.getZ() + 0.5D);
                    } else if (xIsSafe) {
                        landLoc.setX(xaxis.getX() + 0.5D);
                        landLoc.setZ(xaxis.getZ() + 0.5D);
                    }
                }
            }
            Block current = landLoc.getBlock();
            Block below = current.getRelative(BlockFace.DOWN);
            Block above = current.getRelative(BlockFace.UP);

            if ((isSafe(current)) && (isBottomSlab(below)) && (isTopSlab(above))) {
                event.setTo(landLoc);
                return;
            }

            if ((!isSafe(current)) || ((!isSafe(below)) && (!isSafe(above)))) {
                for (BlockFace face : this.sides) {
                    current = landLoc.getBlock().getRelative(face);
                    below = current.getRelative(BlockFace.DOWN);
                    above = current.getRelative(BlockFace.UP);

                    if ((isSafe(current)) && ((isSafe(below)) || (isSafe(above)))) {
                        Location safe = current.getLocation();
                        landLoc.setX(safe.getBlockX() + 0.5D);
                        landLoc.setZ(safe.getBlockZ() + 0.5D);
                        break;
                    }
                }

                event.setCancelled(true);

                Player player = event.getPlayer();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYour pearl has been refunded due to a glitch."));

                TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
                if (teamProfile != null) {
                    teamProfile.removeTimer(EnderpearlTimer.class);
                }
                return;
            }

            event.setTo(landLoc);
        }
    }

    public boolean isBottomSlab(Block block) {
        if ((block.getTypeId() == 44) && (block.getData() < 8)) {
            return true;
        }
        if ((block.getTypeId() == 187) && (block.getData() == 0)) {
            return true;
        }
        return (block.getTypeId() == 126) && (block.getData() < 8);
    }

    public boolean isTopSlab(Block block) {
        if ((block.getTypeId() == 44) && (block.getData() >= 8)) {
            return true;
        }
        if ((block.getTypeId() == 187) && (block.getData() == 8)) {
            return true;
        }
        return (block.getTypeId() == 126) && (block.getData() >= 8);
    }

    public boolean isSafe(Block block) {
        return (block.getType() == Material.SNOW) || (block.isLiquid()) || (block.isEmpty());
    }
}
