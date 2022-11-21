package rip.alpha.hcf.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.listener.RecipeListener;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.settings.Setting;
import rip.alpha.hcf.profile.settings.SettingMenu;

public class SettingCommand {

    @Command(names = {"settings", "options"})
    public static void settingsCommand(Player player) {
        new SettingMenu().openMenu(player);
    }

    @Command(names = {"cobble", "cobblestone"}, async = true)
    public static void cobbleCommand(Player player) {
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        teamProfile.setSetting(Setting.COBBLE, !teamProfile.getSetting(Setting.COBBLE));
        boolean cobble = teamProfile.getSetting(Setting.COBBLE);
        player.sendMessage(CC.YELLOW + "You have " + (cobble ? "enabled" : "disabled") + " cobblestone pickup");
    }

    @Command(names = {"mobdrops"}, async = true)
    public static void mobDropCommand(Player player) {
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        teamProfile.setSetting(Setting.MOB_DROPS, !teamProfile.getSetting(Setting.MOB_DROPS));
        boolean mobDrops = teamProfile.getSetting(Setting.MOB_DROPS);
        player.sendMessage(CC.YELLOW + "You have " + (mobDrops ? "enabled" : "disabled") + " mobdrop pickup");
    }

    @Command(names = {"hotfixpotions"}, permission = "op")
    public static void onPotionHotfix(CommandSender sender) {
        RecipeListener.hotfixEnabled = !RecipeListener.hotfixEnabled;
        sender.sendMessage("Potionhotfix is now "+ (RecipeListener.hotfixEnabled ? "enabled" : "disabled"));
    }
}
