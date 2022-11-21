package rip.alpha.hcf.visual;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class VisualBlockEntry {

    private final String worldName;
    private final int x, y, z;
    private final UUID teamId;
    private final boolean pillar, claimTemp;

    private boolean sent, remove;

    private final Material material;
    private final byte data;

    private final Material previousMaterial;
    private final byte previousData;

    public VisualBlockEntry(Location location, UUID teamId, boolean pillar, boolean claimTemp, Material material, byte data, Material previousMaterial, byte previousData) {
        this(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), teamId, pillar,
                claimTemp, false, false, material, data, previousMaterial, previousData);
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(this.worldName), this.x, this.y, this.z); //Bukkit#getWorld(String) uses a map so its efficient
    }

    @Override
    public VisualBlockEntry clone() {
        return new VisualBlockEntry(this.worldName, this.x, this.y, this.z, this.teamId, this.pillar,
                this.claimTemp, this.remove, this.sent, this.material, this.data, this.previousMaterial, this.previousData);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof VisualBlockEntry) {
            VisualBlockEntry entry = (VisualBlockEntry) o;
            return worldName.equals(entry.getWorldName()) && entry.getX() == x && entry.getY() == y && entry.getZ() == z && entry.getData() == data;
        }
        return false;
    }
}
