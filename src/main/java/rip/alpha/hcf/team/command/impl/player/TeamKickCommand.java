package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.timer.impl.CombatTagTimer;

import java.util.UUID;

public class TeamKickCommand {
    @Command(names = {"team kick", "t kick", "f kick", "faction kick"}, async = true)
    public static void teamKickCommand(Player player, @Param(name = "target") UUID targetUUID) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam team = teamHandler.getPlayerTeamByPlayer(player);

        if (team == null) {
            player.sendMessage(CC.RED + "You are not in a team, use /team create <name> to create a team");
            return;
        }

        if (team.hasDTRFreeze()) {
            player.sendMessage(CC.RED + "You cannot join that team because it is on dtr freeze.");
            return;
        }

        PlayerTeam.TeamMember playerMember = team.getMember(player.getUniqueId());
        PlayerTeam.TeamMember targetMember = team.getMember(targetUUID);

        if (targetMember == null) {
            player.sendMessage(CC.RED + "That player is not in the team");
            return;
        }

        if (targetMember.isHigherOrEqual(playerMember.getRole())) {
            player.sendMessage(CC.RED + "You do not have permission to kick that player from the team");
            return;
        }

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(targetUUID);
        if (teamProfile != null){
            teamProfile.setKickCooldown(System.currentTimeMillis() + 1000);
        }

        team.removeMember(targetUUID);
        team.broadcast("&a" + player.getName() + " &chas kicked &e" + targetMember.getName() + "&c from the team!");

        Player targetPlayer = Bukkit.getPlayer(targetUUID);
        if (targetPlayer != null) {
            if (teamProfile != null && teamProfile.hasTimer(CombatTagTimer.class)){
                player.sendMessage(CC.RED + "You may not kick someone on combat tag.");
                return;
            }

            if (team.getClaim() != null && team.getClaim().contains(targetPlayer.getLocation())){
                HCF.getInstance().getTimerHandler().teleportToSafeLocation(targetPlayer, team);
            }

            targetPlayer.sendMessage(CC.translate("&cYou have been kicked from the team!"));
        }

        team.setSave(true);
    }
}
