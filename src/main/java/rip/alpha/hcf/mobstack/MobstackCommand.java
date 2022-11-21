package rip.alpha.hcf.mobstack;

import net.mcscrims.command.annotation.Command;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import rip.alpha.hcf.HCF;

public class MobstackCommand {

    @Command(names = {"mobstack butcher"}, permission = "op")
    public static void mobStackButcher(CommandSender commandSender) {
        int i = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClasses(LivingEntity.class)) {
                LivingEntity livingEntity = (LivingEntity) entity;
                if (HCF.getInstance().getMobstackHandler().isMobStackEntity(livingEntity)) {
                    livingEntity.remove();
                    i++;
                }
            }
        }
        commandSender.sendMessage("Removed a total of " + i);
    }

}
