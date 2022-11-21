package rip.alpha.hcf.game.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TimeUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.game.GameHandler;
import rip.alpha.hcf.game.impl.CTPGame;
import rip.alpha.hcf.game.impl.KoTHGame;
import rip.alpha.hcf.game.listener.GameClaimListener;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.shared.TeamCreateEvent;
import rip.alpha.hcf.team.impl.SystemTeam;

import java.util.UUID;

public class CTPCommands {

    private static final TeamHandler TEAM_HANDLER = HCF.getInstance().getTeamHandler();
    private static final GameHandler GAME_HANDLER = HCF.getInstance().getGameHandler();

    @Command(names = {"ctp create"}, permission = "op", async = true)
    public static void ctpCommand(Player player, @Param(name = "name", wildcard = true) String name) {
        Team team = TEAM_HANDLER.getTeamByName(name);

        if (team != null) {
            player.sendMessage(CC.CAPTURE_THE_POINT + CC.RED + "A team with that name already exists, you cannot create a ctp over it");
            return;
        }

        SystemTeam systemTeam = new SystemTeam(UUID.randomUUID(), name, CC.D_PURPLE);

        UUID gameId = UUID.randomUUID();
        systemTeam.setSave(true);
        systemTeam.setLinkedGameId(gameId);
        TEAM_HANDLER.addTeam(systemTeam);
        new TeamCreateEvent(player, systemTeam).call(HCF.getInstance());

        CTPGame ctpGame = new CTPGame(gameId, systemTeam.getId());
        GAME_HANDLER.addGame(ctpGame);
        player.sendMessage(CC.CAPTURE_THE_POINT + CC.GREEN + "You have made that ctp, now use /systeam claim " + name);
    }

    @Command(names = "ctp remove", async = true, permission = "hcf.ctp.remove")
    public static void ctpRemoveCommand(CommandSender sender, @Param(name = "ctp") CTPGame ctpGame) {
        GAME_HANDLER.getGameMap().remove(ctpGame.getId());
        GAME_HANDLER.getGamesCollection().deleteOne("id", ctpGame.getId().toString());
        sender.sendMessage(CC.GREEN + "You have removed " + ctpGame.getName());
    }

    @Command(names = {"ctp setstarttime"}, async = true, permission = "op")
    public static void setDefaultTimeCommand(CommandSender sender, @Param(name = "ctp") CTPGame ctpGame, @Param(name = "time") String timeString) {
        Long time = TimeUtil.parseTime(timeString);

        if (time == null) {
            sender.sendMessage(CC.RED + "That time is an invalid format");
            return;
        }

        if (time <= 0) {
            time = 0L;
        }
        ctpGame.setStartTimeMillis(time);
        sender.sendMessage(CC.CAPTURE_THE_POINT + "You have updated the default ctp time.");
    }

    @Command(names = {"ctp start"}, async = true, permission = "hcf.ctp.start")
    public static void ctpStartCommand(CommandSender sender, @Param(name = "ctp") CTPGame ctpGame) {
        if (ctpGame.getOwningTeam() == null) {
            return;
        }

        if (ctpGame.getOwningTeam().getClaim() == null) {
            sender.sendMessage(CC.CAPTURE_THE_POINT + CC.RED + "You need to claim the ctps claim using /systeam claim " + ctpGame.getName());
            return;
        }

        if (ctpGame.getCaptureCuboid() == null) {
            sender.sendMessage(CC.CAPTURE_THE_POINT + CC.RED + "You need to set the ctps capzone using /ctp setcapzone " + ctpGame.getName());
            return;
        }

        if (ctpGame.isActive()) {
            sender.sendMessage(CC.CAPTURE_THE_POINT + CC.RED + "That ctp is already active");
            return;
        }

        ctpGame.start();
    }

    @Command(names = {"ctp cancel", "ctp stop"}, async = true, permission = "hcf.ctp.cancel")
    public static void ctpCancelCommand(CommandSender sender, @Param(name = "ctp") CTPGame ctpGame) {
        if (!ctpGame.isActive()) {
            sender.sendMessage(CC.CAPTURE_THE_POINT + CC.RED + "That ctp is not currently active");
            return;
        }

        ctpGame.end(true);
    }

    @Command(names = {"ctp claimcapzone"}, async = true, permission = "op")
    public static void setCapzoneCommand(Player player, @Param(name = "ctp") CTPGame ctpGame) {

        if (!(ctpGame.getOwningTeam() != null && ctpGame.getOwningTeam().getClaim() != null)) {
            player.sendMessage(CC.CAPTURE_THE_POINT + CC.GREEN + "You need to set a team claim first using /systeam claim");
            return;
        }

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (teamProfile == null) {
            return;
        }
        teamProfile.setSelectedLocations(new Location[2]);
        teamProfile.setClaimingForGame(ctpGame.getId());

        if (!player.getInventory().contains(GameClaimListener.GAME_CLAIM_WAND)) {
            player.getInventory().addItem(GameClaimListener.GAME_CLAIM_WAND);
        }

        player.sendMessage(CC.CAPTURE_THE_POINT + CC.GREEN + "You are now claiming the capzone for " + ctpGame.getName());
    }
}
