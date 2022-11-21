package rip.alpha.hcf.crates;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.mcscrims.libraries.Libraries;
import net.mcscrims.libraries.hologram.Hologram;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.cuboid.SafeLocation;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class Crate {
    private final UUID uuid;
    private final String name;
    private final Int2ObjectMap<CrateItem> items = new Int2ObjectOpenHashMap();

    private ItemStack key;
    private SafeLocation safeLocation;
    private int openDelay;
    private int hologramId = -1;

    public CrateItem[] getCrateItems(int count) {
        int totalWeight = getTotalWeight();

        CrateItem[] toReturn = new CrateItem[count];

        Random random = new Random();
        int totalItems = this.items.size();

        for (int i = 0; i < count; i++) {
            CrateItem item;

            // keep iterating until we choose one
            int r = random.nextInt(totalWeight);
            int k = random.nextInt(totalItems);
            while (r > (item = this.items.get(k)).getWeight()) { //doing this is safer for JVM
                r = random.nextInt(totalWeight);
                k = random.nextInt(totalItems);
            }

            toReturn[i] = item;
        }

        return toReturn;
    }

    public int getTotalWeight() {
        int totalWeight = 0;

        for (CrateItem item : items.values()) {
            totalWeight += item.getWeight();
        }

        return totalWeight;
    }

    public void setSafeLocation(SafeLocation location) {
        this.safeLocation = location;
        if (location == null) {
            if (this.hologramId != -1) {
                Libraries.getInstance().getHologramHandler().removeHologram(this.hologramId);
            }
            this.hologramId = -1;
        } else {
            Hologram hologram = Libraries.getInstance().getHologramHandler().getHologram(this.hologramId);
            if (hologram != null) {
                hologram.updateLocation(location.toBukkit().add(0.5D, -0.1D, 0.5D));
            } else {
                hologram = new Hologram(location.toBukkit().add(0.5D, -0.1D, 0.5D));
                hologram.addLine(CC.GRAY + CC.STRIKE_THROUGH + "-------------");
                hologram.addLine(CC.BLUE + this.getName());
                hologram.addLine(CC.BLUE + "Crate");
                hologram.addLine(CC.GRAY + CC.STRIKE_THROUGH + "-------------");
                this.hologramId = Libraries.getInstance().getHologramHandler().registerHologram(hologram);
            }
        }
    }
}
