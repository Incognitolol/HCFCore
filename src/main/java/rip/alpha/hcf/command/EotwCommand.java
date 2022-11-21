package rip.alpha.hcf.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.game.Game;
import rip.alpha.hcf.game.command.KoTHCommands;
import rip.alpha.hcf.game.impl.KoTHGame;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.SystemTeam;

import java.util.UUID;

public class EotwCommand {
    public static final TeamHandler TEAM_HANDLER = HCF.getInstance().getTeamHandler();

    @Command(names = "eotw start", permission = "hcf.eotw", async = true)
    public static void eotwCommand(Player player){
        HCF.getInstance().getServer().getWorlds().forEach(world ->
                HCF.getInstance().getClaimGrid().registerWorld(world));

        Team spawn = TEAM_HANDLER.getTeamByName("Spawn");
        if (spawn != null){
            ACuboid spawnClaim = spawn.getClaim();
            KoTHCommands.createKOTH(player, "EOTW", CC.DARK_RED);

            SystemTeam systemTeam = TEAM_HANDLER.getSystemTeamByName("EOTW");
            systemTeam.setClaim(spawnClaim);
            systemTeam.setSave(true);
        }

        for (Team team : TEAM_HANDLER.getTeams()){
            team.setHome(null);
            team.setClaim(null);
            team.setSave(true);
        }

        HCF.getInstance().setEotw(true);
        Bukkit.broadcastMessage(CC.GOLD + "[EOTW] " + CC.DARK_RED + " has begun!");
    }
}
