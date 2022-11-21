package rip.alpha.hcf.team.command.impl.admin;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.timer.impl.CombatTagTimer;

import java.util.UUID;

public class ATeamForceKickCommand {
    @Command(names = {"teamadmin forcekick", "tadmin forcekick", "ta forcekick"}, permission = "teamadmin.forcekick", async = true)
    public static void teamAdminForceKick(Player player, @Param(name = "target", defaultValue = "self") UUID targetUUID) {
        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        PlayerTeam targetTeam = teamHandler.getPlayerTeamByPlayer(targetUUID);

        if (targetTeam == null){
            player.sendMessage(CC.RED + "That player is not in a team.");
            return;
        }

        PlayerTeam.TeamMember targetMember = targetTeam.getMember(targetUUID);

        if (targetMember == null) {
            player.sendMessage(CC.RED + "That player is not in the team");
            return;
        }

        if (targetMember.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_LEADER)){
            player.sendMessage(CC.RED + "You cannot kick the leader from the faction!");
            return;
        }

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(targetUUID);
        if (teamProfile != null){
            teamProfile.setKickCooldown(System.currentTimeMillis() + 1000);
        }

        targetTeam.removeMember(targetUUID);
        targetTeam.broadcast("&a" + player.getName() + " &chas kicked &e" + targetMember.getName() + "&c from the team!");

        Player targetPlayer = Bukkit.getPlayer(targetUUID);
        if (targetPlayer != null) {
            if (teamProfile != null && teamProfile.hasTimer(CombatTagTimer.class)){
                player.sendMessage(CC.RED + "You may not kick someone on combat tag.");
                return;
            }

            if (targetTeam.getClaim().contains(targetPlayer.getLocation())){
                HCF.getInstance().getTimerHandler().teleportToSafeLocation(targetPlayer, targetTeam);
            }

            targetPlayer.sendMessage(CC.translate("&cYou have been kicked from the team!"));
        }

        targetTeam.setSave(true);
    }
}
