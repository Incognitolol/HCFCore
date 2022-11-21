package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamWithdrawCommand {
    @Command(names = {
            "team withdraw", "t withdraw", "f withdraw", "faction withdraw",
            "team w", "t w", "f w", "faction w"
    }, async = true)
    public static void teamWithdrawCommand(Player player, @Param(name = "amount") String stringAmount) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        PlayerTeam.TeamMember member = team.getMember(player.getUniqueId());
        if (!member.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_CAPTAIN)) {
            player.sendMessage(CC.RED + "You are not high enough in the team hierarchy to withdraw from the teams balance");
            return;
        }

        if (team.getBalance() <= 0) {
            player.sendMessage(CC.RED + "Your team does not have enough balance to withdraw");
            return;
        }

        int amount;

        if (stringAmount.equalsIgnoreCase("all")) {
            amount = team.getBalance();
        } else {
            try {
                amount = Integer.parseInt(stringAmount);
            } catch (NumberFormatException e) {
                player.sendMessage(CC.RED + "That amount is invalid");
                return;
            }
        }

        if (amount <= 0) {
            player.sendMessage(CC.RED + "The amount due is suppose to be positive");
            return;
        }

        int balance = team.getBalance() - amount;

        if (balance < 0) {
            player.sendMessage(CC.RED + "You are attempting to withdraw too much from your teams balance");
            return;
        }

        team.broadcast("&a" + player.getName() + " &cwithdrew &9$" + amount + "&e from the team.");
        team.setBalance(balance);
        team.setSave(true);

        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player.getUniqueId());
        profile.setBalance(profile.getBalance() + amount);
        profile.setSave(true);
    }
}
