package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;

public class TeamDepositeCommand {
    @Command(names = {
            "team deposit", "t deposit", "f deposit", "faction deposit",
            "team d", "t d", "f d", "faction d"
    }, async = true)
    public static void teamDeposit(Player player, @Param(name = "amount") String stringAmount) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player.getUniqueId());

        if (profile.getBalance() <= 0) {
            player.sendMessage(CC.RED + "You do not have enough money to deposit into the team");
            return;
        }

        int amount;

        if (stringAmount.equalsIgnoreCase("all")) {
            amount = profile.getBalance();
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

        int balance = profile.getBalance() - amount;

        if (balance < 0) {
            player.sendMessage(CC.RED + "You are attempting to deposit too much from your balance.");
            return;
        }

        team.setBalance(team.getBalance() + amount);
        team.broadcast("&a" + player.getName() + " &ehas deposited &9$" + amount + "&e to the team.");
        team.setSave(true);

        profile.setBalance(balance);
        profile.setSave(true);
    }
}
