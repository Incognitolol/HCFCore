package rip.alpha.hcf.pvpclass;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;

@RequiredArgsConstructor
public class PvPClassTask implements Runnable {

    private final PvPClassHandler pvPClassHandler;

    @Override
    public void run() {
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.willBeOnline()) {
                    continue;
                }

                TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
                if (profile == null) {
                    continue;
                }

                Class<? extends PvPClass> equipClass = profile.getEquipPvPClass();

                if (equipClass != null) {
                    PvPClass pvPClass = this.pvPClassHandler.getPvPClass(equipClass);
                    if (pvPClass != null) {
                        boolean applicable = false;
                        PlayerInventory inventory = player.getInventory();

                        if (this.wearingFullArmor(player)) {
                            applicable = pvPClass.isApplicable(player, inventory);
                        }

                        pvPClass.onTick(player, inventory);
                        if (!applicable) {
                            this.pvPClassHandler.unequipClass(player, profile, pvPClass);
                        } else {
                            continue;
                        }
                    }
                }


                for (PvPClass pvPClass : this.pvPClassHandler.getPvPClasses()) {
                    if (this.wearingFullArmor(player) && pvPClass.isApplicable(player, player.getInventory())) {
                        this.pvPClassHandler.equipClass(player, profile, pvPClass);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean wearingFullArmor(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (ItemStack itemStack : inventory.getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return false;
            }
        }
        return true;
    }
}
