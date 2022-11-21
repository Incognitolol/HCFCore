package rip.alpha.hcf.listener;

import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class VehicleListener implements Listener {
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        EntityType entityType = event.getEntityType();
        if (entityType != EntityType.BOAT && entityType != EntityType.MINECART) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (!(vehicle instanceof Boat) && !(vehicle instanceof Minecart)) {
            return;
        }
        event.setCancelled(true);
    }
}
