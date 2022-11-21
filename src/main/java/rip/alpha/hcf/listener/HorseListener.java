package rip.alpha.hcf.listener;

import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.DamagerUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.text.DecimalFormat;

public class HorseListener implements Listener {

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityHorseDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Horse)) {
            return;
        }
        Player damager = DamagerUtils.getDamager(event);
        if (damager == null) {
            return;
        }
        Horse horse = (Horse) event.getEntity();
        if (!horse.isTamed()) {
            return;
        }
        if (horse.getOwner() == null) {
            return;
        }
        PlayerTeam team = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(damager);
        if (team == null) {
            return;
        }
        PlayerTeam.TeamMember member = team.getMember(horse.getOwner().getUniqueId());
        if (member == null) {
            return;
        }
        event.setCancelled(true);
        damager.sendMessage(CC.translate("&eThis horse belongs to &2" + member.getName() + " &ewho is in your faction."));
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (!(vehicle instanceof Horse)){
            return;
        }
        Horse horse = (Horse) vehicle;
        if (!horse.isTamed()) {
            return;
        }
        if (horse.getOwner() == null) {
            return;
        }
        Entity entity = event.getEntered();
        if (!(entity instanceof Player)){
            return;
        }
        Player player = (Player) entity;
        if (horse.getOwner().getUniqueId().equals(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(CC.RED + "This is not your horse!");
    }

//    @EventHandler
//    public void onSugarGive(VehicleEnterEvent event) {
//        if (event.getVehicle() instanceof Horse && event.getEntered() instanceof Player) {
//            Horse horse = (Horse) event.getVehicle();
//            Player player = (Player) event.getEntered();
//            if (player.getItemInHand().getType() == Material.WATCH){
//                player.sendMessage(CC.GOLD + "Horse info: ");
//                player.sendMessage(CC.YELLOW + "Variant: " + CC.GRAY + horse.getVariant());
//                player.sendMessage(CC.YELLOW + "Style: " + CC.GRAY + horse.getStyle());
//                player.sendMessage(CC.YELLOW + "Color: " + CC.GRAY + horse.getColor());
//                player.sendMessage(CC.YELLOW + "Speed: " + CC.GRAY + this.decimalFormat.format((((CraftLivingEntity) horse).getHandle().getAttributeInstance(GenericAttributes.d).getValue() * 10)));
//                player.sendMessage(CC.YELLOW + "Max Health: " + CC.GRAY + "[" + horse.getHealth() + "/" + horse.getMaxHealth() + "]");
//                player.sendMessage(CC.YELLOW + "Max Jump-Height: " + CC.GRAY + horse.getJumpStrength());
//                event.setCancelled(true);
//            } else if (player.getItemInHand().getType() == Material.SUGAR) {
//                EntityLiving craftHorse = ((CraftLivingEntity) horse).getHandle();
//                double horseSpeed = craftHorse.getAttributeInstance(GenericAttributes.d).getValue() * 10;
//                double increasedSpeed = horseSpeed + (horseSpeed * 0.25);
//
//                if (increasedSpeed >= 7.5) {
//                    player.sendMessage(CC.RED + "Your horse's speed is already at the maximum speed.");
//                    event.setCancelled(true);
//                    return;
//                }
//
//                event.setCancelled(true);
//                craftHorse.getAttributeInstance(GenericAttributes.d).setValue(increasedSpeed);
//                player.sendMessage(CC.GREEN + "You have increased your horse's speed to " + this.decimalFormat.format(increasedSpeed) + ".");
//            }
//        }
//    }
}
