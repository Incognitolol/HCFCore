package rip.alpha.hcf.end;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.gson.GsonUtil;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;

public class EndCommands {
    @Command(names = "setendexit", permission = "hcf.setendexit", async = true)
    public static void setEndExitCommand(Player player) {
        Location location = player.getLocation();
        if (!location.getWorld().getName().equalsIgnoreCase("world")) {
            player.sendMessage(CC.RED + "You must be in overworld for this");
            return;
        }

        HCF.getInstance().getEndHandler().setEndExitOverworld(location);
        FileConfiguration endConfig = HCF.getInstance().getEndHandler().getEndConfig().getConfig();
        endConfig.set("end.endExitOverworld.location", GsonUtil.GSON.toJson(location));
        HCF.getInstance().getEndHandler().getEndConfig().save();

        player.sendMessage(CC.GREEN + "You have set the end exit location to " + location.getBlockX() + ", " + location.getBlockZ());
    }

    @Command(names = "setendportal", permission = "hcf.setendportal", async = true)
    public static void setEndPortalCommand(Player player) {
        Location location = player.getLocation();
        if (!location.getWorld().getName().equalsIgnoreCase("world_the_end")) {
            player.sendMessage(CC.RED + "You must be in end for this");
            return;
        }

        HCF.getInstance().getEndHandler().setEndExit(location);
        FileConfiguration endConfig = HCF.getInstance().getEndHandler().getEndConfig().getConfig();
        endConfig.set("end.endPortal.location", GsonUtil.GSON.toJson(location));
        HCF.getInstance().getEndHandler().getEndConfig().save();
        player.sendMessage(CC.GREEN + "You have set the end portal location to " + location.getBlockX() + ", " + location.getBlockZ());
    }
}
