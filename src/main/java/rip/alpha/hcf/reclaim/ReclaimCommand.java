package rip.alpha.hcf.reclaim;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.permissions.PermissionsPlugin;
import net.mcscrims.permissions.profile.PermissionProfile;
import net.mcscrims.permissions.rank.Rank;
import net.mcscrims.punishments.PunishmentPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;

public class ReclaimCommand {

    @Command(names = {"reclaim"})
    public static void reclaimCommand(Player player){
        PermissionProfile permissionProfile = PermissionsPlugin.getInstance().getPermissionAPI().getProfile(player.getUniqueId());
        Rank currentRank = permissionProfile.getCurrentRank();

        if (currentRank == null){
            player.sendMessage(CC.RED + "You do not have a reclaim.");
            return;
        }

        ReclaimEntry reclaimEntry = HCF.getInstance().getReclaimHandler().getReclaimEntry(currentRank.getName().toLowerCase());

        if (reclaimEntry == null){
            player.sendMessage(CC.RED + "You do not have a reclaim.");
            return;
        }

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);

        if (teamProfile.isReclaimed()){
            player.sendMessage(CC.RED + "You have already reclaimed.");
            return;
        }

        teamProfile.setReclaimed(true);
        teamProfile.setSave(true);

        String playerName = player.getName();
        reclaimEntry.giveToPlayer(playerName);
        String colorName = currentRank.getChatColor() + playerName;
        String broadcast = String.format("&6[Reclaim] %s &ehas claimed their %s &erank!", colorName, currentRank.getColorName());
        Bukkit.broadcastMessage(CC.translate(broadcast));
    }

    @Command(names = {"reclaimreset"}, permission = "op")
    public static void reclaimReset(CommandSender sender, @Param(name = "target") Player target){
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(target);

        if (!teamProfile.isReclaimed()){
            sender.sendMessage(CC.RED + "That player has not reclaimed");
            return;
        }

        teamProfile.setReclaimed(false);
        teamProfile.setSave(true);
        sender.sendMessage(CC.GREEN + "You have reset the reclaim for " + target.getName());
    }
}
