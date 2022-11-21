package rip.alpha.hcf.glowstone.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.shared.TeamCreateEvent;
import rip.alpha.hcf.team.impl.SystemTeam;

import java.util.UUID;

public class GlowstoneCommand {
    private static final TeamHandler TEAM_HANDLER = HCF.getInstance().getTeamHandler();

    @Command(names = "glowstone create", permission = "hcf.glowstone.create", async = true)
    public static void glowstoneCreate(Player player) {
        String name = "Glowstone";
        Team team = TEAM_HANDLER.getTeamByName(name);

        if (team != null) {
            player.sendMessage(CC.RED + "That team already exists, use /teamadmin forcedisband if its a player team");
            return;
        }

        team = new SystemTeam(UUID.randomUUID(), name, CC.GOLD);
        team.setSave(true);
        TEAM_HANDLER.addTeam(team);

        player.sendMessage(CC.GREEN + "You have successfully created glowstone team named " + name);
        new TeamCreateEvent(player, team).call(HCF.getInstance());
    }

    @Command(names = "glowstone scan", permission = "hcf.glowstone.scan")
    public static void glowstoneScan(Player player) {
        HCF.getInstance().getGlowstoneHandler().scanBlocks();
        player.sendMessage(CC.GREEN + "You have updated the glowstone block cache;");
    }

    @Command(names = "glowstone reset", permission = "hcf.glowstone.reset", async = true)
    public static void glowstoneReset(Player player) {
        player.sendMessage(CC.GREEN + "You are now resetting glowstone, please wait.");
        HCF.getInstance().getGlowstoneHandler().resetBlocks();
    }
}
