package rip.alpha.hcf.team.grid;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Getter;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.util.LongHash;
import rip.alpha.hcf.HCF;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClaimGrid {
    @Getter private final Map<UUID, Long2ObjectMap<Set<ClaimEntry>>> cuboidMap = new ConcurrentHashMap<>();

    public ClaimGrid() {
        for (World world : HCF.getInstance().getServer().getWorlds()) {
            this.registerWorld(world);
        }
    }

    public void registerWorld(World world) {
        this.cuboidMap.put(world.getUID(), new Long2ObjectOpenHashMap<>());
    }

    public ClaimEntry getGridData(Location location) {
        long key = this.toLong(location.getBlockX(), location.getBlockZ());

        Set<ClaimEntry> entries = this.cuboidMap.get(location.getWorld().getUID()).get(key);

        if (entries == null) {
            return null;
        }

        for (ClaimEntry data : entries) {
            if (data.getCuboid().contains(location)) {
                return data;
            }
        }

        return null;
    }

    public Set<ClaimEntry> getGridData(Location center, int xDistance, int yDistance, int zDistance) {
        Location min = new Location(center.getWorld(), center.getBlockX() - xDistance, center.getBlockY() - yDistance, center.getBlockZ() - zDistance);
        Location max = new Location(center.getWorld(), center.getBlockX() + xDistance, center.getBlockY() + yDistance, center.getBlockZ() + zDistance);
        return this.getGridData(min, max);
    }

    public Set<ClaimEntry> getGridData(Location min, Location max) {
        return this.getGridData(min.getWorld().getUID(), min.getBlockX(), min.getBlockZ(), max.getBlockX(), max.getBlockZ());
    }

    public Set<ClaimEntry> getGridData(UUID worldId, int minX, int minZ, int maxX, int maxZ) {
        Set<ClaimEntry> gridData = new HashSet<>();
        int step = 64;

        for (int x = minX; x < maxX + step; x += step) {
            for (int z = minZ; z < maxZ + step; z += step) {
                long key = this.toLong(x, z);

                Optional.ofNullable(this.cuboidMap.get(worldId).get(key)).ifPresent(claimEntries -> {
                    for (ClaimEntry regionEntry : claimEntries) {
                        if (gridData.contains(regionEntry)) {
                            continue;
                        }

                        ACuboid claimCuboid = regionEntry.getCuboid();
                        if ((maxX >= claimCuboid.getMinX())
                                && (minX <= claimCuboid.getMaxX())
                                && (maxZ >= claimCuboid.getMinZ())
                                && (minZ <= claimCuboid.getMaxZ())) {
                            gridData.add(regionEntry);
                        }
                    }
                });
            }
        }

        return gridData;
    }

    public UUID getTeamIdByLocation(Location location) {
        ClaimEntry gridData = this.getGridData(location);
        return gridData == null ? null : gridData.getTeamId();
    }

    public ACuboid getCuboidByLocation(Location location) {
        ClaimEntry gridData = this.getGridData(location);
        return gridData == null ? null : gridData.getCuboid();
    }

    public void setClaim(ACuboid claim, UUID teamId) {
        ClaimEntry regionData = new ClaimEntry(teamId, claim);
        int step = 64;
        for (int x = claim.getMinX(); x < claim.getMaxX() + step; x += step) {
            for (int z = claim.getMinZ(); z < claim.getMaxZ() + step; z += step) {
                if (claim.getWorld() == null) {
                    continue;
                }

                Long2ObjectMap<Set<ClaimEntry>> worldMap = this.cuboidMap.get(claim.getWorld().getUID());

                if (worldMap == null) {
                    continue;
                }

                long key = this.toLong(x, z);
                if (teamId == null) {
                    worldMap.get(key).removeIf(entry -> entry.getCuboid().equals(claim));
                } else {
                    worldMap.computeIfAbsent(key, aLong -> new HashSet<>()).add(regionData);
                }
            }
        }
    }

    private long toLong(int x, int z) {
        return LongHash.toLong(x >> 6, z >> 6);
    }
}
