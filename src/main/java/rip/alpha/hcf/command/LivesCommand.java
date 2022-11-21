package rip.alpha.hcf.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;

import java.util.UUID;

public class LivesCommand {
    private static final String SELF_LIVES_FORMAT = CC.translate("&eLives:&6 %s");
    private static final String OTHER_LIVES_FORMAT = CC.translate("&eLives of %s:&6 %s");

    @Command(names = "lives add", permission = "hcf.lives.add", async = true)
    public static void livesAdd(CommandSender sender, @Param(name = "target") UUID uuid, @Param(name = "amount") int lives) {
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfileOrLoad(uuid);
        teamProfile.setLives(Math.max(0, teamProfile.getLives() + lives));
        teamProfile.setSave(true);
        Player target = Bukkit.getPlayer(uuid);
        if (target != null) {
            target.sendMessage(CC.YELLOW + "You have been given " + CC.GOLD + lives + CC.YELLOW + " lives.");
        }
        sender.sendMessage(CC.YELLOW + "You have given " + CC.GOLD + lives + CC.YELLOW + " lives to " + CC.GOLD + UUIDFetcher.getName(uuid));
    }

    @Command(names = "lives remove", permission = "hcf.lives.remove", async = true)
    public static void livesRemove(CommandSender sender, @Param(name = "target") UUID uuid, @Param(name = "amount") int lives) {
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfileOrLoad(uuid);
        teamProfile.setLives(Math.max(0, teamProfile.getLives() - lives));
        teamProfile.setSave(true);
        Player target = Bukkit.getPlayer(uuid);
        if (target != null) {
            target.sendMessage(CC.GOLD + lives + CC.YELLOW + " has been taken away from you.");
        }
        sender.sendMessage(CC.YELLOW + "You have taken " + CC.GOLD + lives + CC.YELLOW + " lives from " + CC.GOLD + UUIDFetcher.getName(uuid));
    }

    @Command(names = {"lives set"}, permission = "hcf.lives.set", async = true)
    public static void livesSetCommand(CommandSender sender, @Param(name = "target") UUID uuid, @Param(name = "amount") int lives) {
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfileOrLoad(uuid);
        teamProfile.setLives(lives);
        teamProfile.setSave(true);
        sender.sendMessage(CC.YELLOW + "You have set " + CC.GOLD + lives + CC.YELLOW + " lives to " + CC.GOLD + UUIDFetcher.getName(uuid));
    }

    @Command(names = {"revive", "pvp revive"}, async = true)
    public static void reviveCommand(Player player, @Param(name = "target") UUID targetUUID) {
        TeamProfile playerProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        TeamProfile targetProfile = HCF.getInstance().getProfileHandler().getProfileOrLoad(targetUUID);

        if (!targetProfile.isDeathban()) {
            player.sendMessage(CC.RED + "That player is not deathbanned.");
            return;
        }

        if (playerProfile.getLives() <= 0) {
            player.sendMessage(CC.RED + "You do not have enough lives to revive that player.");
            return;
        }

        playerProfile.setLives(playerProfile.getLives() - 1);
        playerProfile.setSave(true);

        targetProfile.setDeathbanTime(-1);
        targetProfile.setSave(true);

        player.sendMessage(CC.GREEN + "You have revived that player");
    }

    @Command(names = {"lives", "pvp lives"}, async = true)
    public static void livesCheck(Player player, @Param(name = "target", defaultValue = "self") UUID targetUUID) {
        boolean hasOtherPermission = player.hasPermission("hcf.lives.other");
        boolean self = player.getUniqueId().equals(targetUUID);

        if (!self && !hasOtherPermission) {
            player.sendMessage(CC.RED + "You do not have permission to view other peoples lives.");
            return;
        }

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfileOrLoad(targetUUID);

        int lives = teamProfile.getLives();

        String name = self ? null : UUIDFetcher.getName(targetUUID);
        String message = self ? String.format(SELF_LIVES_FORMAT, lives) : String.format(OTHER_LIVES_FORMAT, name, lives);
        player.sendMessage(message);
    }
}
