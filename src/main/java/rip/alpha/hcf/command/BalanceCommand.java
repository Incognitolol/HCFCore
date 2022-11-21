package rip.alpha.hcf.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.BalanceUtil;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;

import java.util.UUID;

public class BalanceCommand {

    private static final String SELF_BALANCE_FORMAT = CC.translate("&aBalance: $%s");
    private static final String OTHER_BALANCE_FORMAT = CC.translate("&aBalance of %s: $%s");

    @Command(names = {"bal", "balance", "money", "cash", "coins"}, async = true)
    public static void balanceCommand(CommandSender sender, @Param(name = "target", defaultValue = "self") UUID targetUUID) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfileOrLoad(targetUUID);
        boolean self = false;

        if (sender instanceof Player) {
            if (((Player) sender).getUniqueId().equals(targetUUID)) {
                self = true;
            }
        }

        int balance = profile.getBalance();
        String formattedBalance = BalanceUtil.formatBalance(balance);
        String name = self ? null : UUIDFetcher.getName(targetUUID); //make it null so it doesnt fetch when itself because it doesnt need to.
        String message = self ? String.format(SELF_BALANCE_FORMAT, formattedBalance) : String.format(OTHER_BALANCE_FORMAT, name, formattedBalance);
        sender.sendMessage(message);
    }

    @Command(names = {"setbal", "setbalance"}, async = true, permission = "op")
    public static void setBalance(CommandSender sender, @Param(name = "target") UUID targetUUID, @Param(name = "amount") int amount) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfileOrLoad(targetUUID);
        profile.setBalance(amount);
        profile.setSave(true);
        sender.sendMessage(CC.translate("&aYou have set the balance of " + UUIDFetcher.getName(targetUUID) + " to $" + BalanceUtil.formatBalance(amount)));
    }

    @Command(names = "pay", async = true)
    public static void payCommand(CommandSender sender, @Param(name = "target") UUID targetUUID, @Param(name = "amount") int amount) {
        if (!(sender instanceof Player)) {
            return;
        }

        Player player = (Player) sender;

        if (player.getUniqueId().equals(targetUUID)) {
            player.sendMessage(CC.RED + "You cannot pay yourself");
            return;
        }

        TeamProfile senderProfile = HCF.getInstance().getProfileHandler().getProfile(player.getUniqueId());

        if (amount <= 0) {
            player.sendMessage(CC.RED + "That amount must be positive.");
            return;
        }

        if (senderProfile.getBalance() < amount) {
            player.sendMessage(CC.RED + "You dont have enough balance to pay them this.");
            return;
        }

        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfileOrLoad(targetUUID);

        profile.setBalance(profile.getBalance() + amount);
        senderProfile.setBalance(senderProfile.getBalance() - amount);

        String formattedAmount = BalanceUtil.formatBalance(amount);

        player.sendMessage(CC.GREEN + "You paid " + UUIDFetcher.getName(targetUUID) + CC.YELLOW + " $" + formattedAmount + ".");
        Player targetPlayer = Bukkit.getPlayer(targetUUID);
        if (targetPlayer != null) {
            targetPlayer.sendMessage(CC.GREEN + "You received " + CC.YELLOW + "$" + formattedAmount + CC.GREEN + " from " + player.getName());
        }
    }
}
