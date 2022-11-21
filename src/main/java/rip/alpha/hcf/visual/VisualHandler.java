package rip.alpha.hcf.visual;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketWorldBorderCreateNew;
import lombok.Getter;
import net.mcscrims.libraries.util.BlockUtil;
import net.mcscrims.libraries.util.TaskUtil;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.settings.Setting;
import rip.foxtrot.spigot.fSpigot;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Getter
public class VisualHandler {

    private final Map<UUID, Set<VisualBlockEntry>> visualBlocks;
    private final Map<UUID, Set<LCVisualBorderEntry>> lunarWallMap;

    private final int wallBorderHeightDiff;
    private final int wallBorderHorizontalDiff;

    public VisualHandler(HCF instance) {
        this.visualBlocks = new HashMap<>();
        this.lunarWallMap = new HashMap<>();

        this.wallBorderHeightDiff = 3;
        this.wallBorderHorizontalDiff = 12;

        fSpigot.INSTANCE.addPacketHandler(new VisualPacketHandler(this));

        instance.getServer().getPluginManager().registerEvents(new VisualListener(this), instance);

        HCF.getInstance().getScheduledExecutorService().scheduleAtFixedRate(new VisualTask(this),
                100, 100, TimeUnit.MILLISECONDS);
    }

    public void showPillar(Player player, Material pillarMaterial, UUID teamId, Location location, boolean temp) {
        TaskUtil.runAsync(() -> {
            location.getWorld().getChunkAtAsync(location, chunk -> {
                for (int i = 0; i < 256; i++) {
                    Location currentLocation = location.clone();
                    currentLocation.setY(i);
                    Block block = chunk.getBlock(currentLocation.getBlockX(), currentLocation.getBlockY(), currentLocation.getBlockZ());
                    Material material = block.getType();
                    Material currentEntryMaterial = i % 3 == 0 ? pillarMaterial : Material.GLASS;
                    if (material == Material.AIR || !material.isSolid()) {
                        byte data = block.getData();
                        VisualBlockEntry entry = new VisualBlockEntry(currentLocation, teamId, true, temp,
                                currentEntryMaterial, (byte) 0, material, data);
                        this.addVisualBlockEntry(player.getUniqueId(), entry);
                        this.sendBlockChange(player, entry, currentLocation);
                    }
                }
            });
        }, HCF.getInstance());
    }

    public void showCuboid(Player player, Material pillarMaterial, UUID teamId, ACuboid cuboid) {
        TaskUtil.runAsync(() -> {
            Location playerLocation = player.getLocation();
            Location[] corners = cuboid.getCorners();
            for (Location location : corners) {
                if (!location.getWorld().getName().equals(playerLocation.getWorld().getName())) {
                    continue;
                }
                if (location.distanceSquared(playerLocation) > 10000) {
                    continue;
                }
                this.showPillar(player, pillarMaterial, teamId, location, false);
            }
        }, HCF.getInstance());
    }

    public void showClaimBorders(Player player, byte blockData, UUID teamId, ACuboid cuboid) {
        TaskUtil.runAsync(() -> {
            Location playerLocation = player.getLocation();

            boolean shouldGetLunarWalls = LunarClientAPI.getInstance().isRunningLunarClient(player);
            if (shouldGetLunarWalls) {
                shouldGetLunarWalls = HCF.getInstance().getProfileHandler().getProfile(player).getSetting(Setting.LUNAR_BORDERS);
            }

            if (shouldGetLunarWalls) {
                Set<LCVisualBorderEntry> lunarWalls = this.getLunarWalls(player.getUniqueId());
                Color color = Color.RED;

                if (blockData == 5) {
                    color = Color.GREEN;
                } else if (blockData == 11) {
                    color = Color.BLUE;
                }

                LCVisualBorderEntry borderEntry = new LCVisualBorderEntry(teamId, cuboid, color);
                if (!lunarWalls.contains(borderEntry)) {
                    String worldId = cuboid.getWorld().getUID().toString();
                    int minX = cuboid.getMinX();
                    int maxX = cuboid.getMaxX();
                    int minZ = cuboid.getMinZ();
                    int maxZ = cuboid.getMaxZ();
                    LCPacketWorldBorderCreateNew borderCreateNew =
                            new LCPacketWorldBorderCreateNew(teamId.toString(), worldId, true, false, false, color.getRGB(), minX, minZ, maxX, maxZ);
                    LunarClientAPI.getInstance().sendPacket(player, borderCreateNew);
                    this.addLunarWallEntry(player.getUniqueId(), borderEntry);
                }
            } else {
                Set<Location> borderLocations =
                        cuboid.generateBorderLocations(player, playerLocation, (this.wallBorderHeightDiff - 1), (this.wallBorderHorizontalDiff - 1));

                for (Location location : borderLocations) {
                    if (Math.abs(location.getBlockX() - playerLocation.getBlockX()) > this.wallBorderHorizontalDiff) {
                        continue;
                    }
                    if (Math.abs(location.getBlockZ() - playerLocation.getBlockZ()) > this.wallBorderHorizontalDiff) {
                        continue;
                    }

                    int initialY = playerLocation.getBlockY();

                    location.getWorld().getChunkAtAsync(location, chunk -> {
                        for (int y = -3; y <= 3; y++) {
                            Location yLocation = location.clone();
                            yLocation.setY(initialY + y);

                            if (this.hasVisualBlockEntry(
                                    player.getUniqueId(), location.getWorld().getName(), yLocation.getBlockX(), yLocation.getBlockY(), yLocation.getBlockZ())) {
                                continue;
                            }

                            Block blockAtLocation = chunk.getBlock(yLocation.getBlockX(), yLocation.getBlockY(), yLocation.getBlockZ());

                            if (blockAtLocation == null) {
                                continue;
                            }

                            if (!BlockUtil.isNonSolidBlock(blockAtLocation)) {
                                continue;
                            }

                            VisualBlockEntry visualBlockEntry = new VisualBlockEntry(yLocation, teamId, false, false,
                                    Material.STAINED_GLASS, blockData, blockAtLocation.getType(), blockAtLocation.getData());

                            this.addVisualBlockEntry(player.getUniqueId(), visualBlockEntry);
                            player.sendBlockChange(yLocation, Material.STAINED_GLASS, blockData);
                        }
                    });
                }
            }
        }, HCF.getInstance());
    }

    public void clearVisualBlocks(Player player, Predicate<VisualBlockEntry> predicate) {
        TaskUtil.runAsync(() -> {
            Set<VisualBlockEntry> entries = this.getVisualBlocks(player.getUniqueId());
            if (entries == null) {
                return;
            }
            for (VisualBlockEntry entry : entries) {
                if (predicate.test(entry)) {
                    this.removeVisualBlockEntry(player.getUniqueId(), entry);
                    if (!entry.getWorldName().equals(player.getWorld().getName())) {
                        continue;
                    }
                    player.sendBlockChange(entry.toLocation(), entry.getPreviousMaterial(), entry.getPreviousData());
                }
            }
        }, HCF.getInstance());
    }

    public void clearVisualBlocksByBlockData(Player player, byte blockData) {
        this.clearVisualBlocks(player, entry -> {
            if (entry.isClaimTemp()) {
                return false;
            }
            if (entry.isPillar()) {
                return false;
            }
            return blockData == entry.getData();
        });
    }

    public void sendBlockChange(Player player, VisualBlockEntry entry, Location location) {
        player.sendBlockChange(location, entry.getMaterial(), entry.getData());
        this.addVisualBlockEntry(player.getUniqueId(), entry);
    }

    public VisualBlockEntry getVisualBlockEntry(UUID uuid, String worldName, int x, int y, int z) {
        Set<VisualBlockEntry> entries = this.getVisualBlocks(uuid);
        if (entries == null || entries.size() <= 0) {
            return null;
        }
        for (VisualBlockEntry entry : entries) {
            if (worldName.equals(entry.getWorldName())) {
                if (entry.getX() == x && entry.getY() == y && entry.getZ() == z) {
                    return entry;
                }
            }
        }
        return null;
    }

    public boolean hasVisualBlockEntry(UUID uuid, String worldName, int x, int y, int z) {
        return this.getVisualBlockEntry(uuid, worldName, x, y, z) != null;
    }

    public void addVisualBlockEntry(UUID uuid, VisualBlockEntry entry) {
        Set<VisualBlockEntry> entries = this.getVisualBlocks(uuid);
        if (entries != null) {
            entries.add(entry);
        }
    }

    public void addLunarWallEntry(UUID uuid, LCVisualBorderEntry visualBorderEntry) {
        Set<LCVisualBorderEntry> entries = this.getLunarWalls(uuid);
        if (entries != null) {
            entries.add(visualBorderEntry);
        }
    }

    public void removeVisualBlockEntry(UUID uuid, VisualBlockEntry entry) {
        Set<VisualBlockEntry> entries = this.getVisualBlocks(uuid);
        if (entries != null) {
            entries.remove(entry);
        }
    }

    public Set<VisualBlockEntry> getVisualBlocks(UUID uuid) {
        return this.visualBlocks.get(uuid);
    }

    public Set<LCVisualBorderEntry> getLunarWalls(UUID uuid) {
        return this.lunarWallMap.get(uuid);
    }
}
