package rip.alpha.hcf.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.metadata.FixedMetadataValue;
import rip.alpha.hcf.HCF;

import java.util.HashSet;
import java.util.Set;

public class PistonNerfListener implements Listener {

    private final Set<Material> blacklistedMaterials;

    public PistonNerfListener() {
        this.blacklistedMaterials = new HashSet<>();
        this.blacklistedMaterials.add(Material.MELON_BLOCK);
        this.blacklistedMaterials.add(Material.PUMPKIN);
        this.blacklistedMaterials.add(Material.SUGAR_CANE_BLOCK);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Block pistonBlock = event.getBlock();

        if (pistonBlock.hasMetadata("pistonDelay")) {
            long l = pistonBlock.getMetadata("pistonDelay").get(0).asLong();
            if (l - System.currentTimeMillis() > 0) { //cooldown
                event.setCancelled(true);
                return;
            }
        }

        Block relBlock = pistonBlock.getRelative(event.getDirection());
        if (this.blacklistedMaterials.contains(relBlock.getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtendMonitor(BlockPistonExtendEvent event) {
        Block pistonBlock = event.getBlock();
        pistonBlock.setMetadata("pistonDelay", new FixedMetadataValue(HCF.getInstance(), System.currentTimeMillis() + 1000));
    }
}
