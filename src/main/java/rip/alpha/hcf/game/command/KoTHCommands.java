package rip.alpha.hcf.game.command;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.SimpleText;
import net.mcscrims.libraries.util.TimeUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.game.GameHandler;
import rip.alpha.hcf.game.impl.KoTHGame;
import rip.alpha.hcf.game.listener.GameClaimListener;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.shared.TeamCreateEvent;
import rip.alpha.hcf.team.impl.SystemTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class KoTHCommands {

    private static final TeamHandler TEAM_HANDLER = HCF.getInstance().getTeamHandler();
    private static final GameHandler GAME_HANDLER = HCF.getInstance().getGameHandler();

    @Command(names = {"koth create"}, async = true, permission = "op")
    public static void kothCreateCommand(Player player, @Param(name = "name", wildcard = true) String name) {
        createKOTH(player, name, CC.BLUE);
    }

    @Command(names = {"koth settime"}, async = true, permission = "op")
    public static void setCurrentTimeCommand(CommandSender sender, @Param(name = "koth") KoTHGame koTHGame, @Param(name = "time") String timeString) {
        Long time = TimeUtil.parseTime(timeString);

        if (time == null) {
            sender.sendMessage(CC.RED + "That time is an invalid format");
            return;
        }

        if (time <= 0) {
            time = 0L;
        }

        if (!koTHGame.isActive()) {
            sender.sendMessage(CC.KOTH + CC.RED + "You cannot set the time of a non active koth");
            return;
        }

        koTHGame.setCurrentTimeMillis(time);
        sender.sendMessage(CC.KOTH + "You have updated the current koth time.");
    }

    @Command(names = {"koth setstarttime"}, async = true, permission = "op")
    public static void setDefaultTimeCommand(CommandSender sender, @Param(name = "koth") KoTHGame koTHGame, @Param(name = "time") String timeString) {
        Long time = TimeUtil.parseTime(timeString);

        if (time == null) {
            sender.sendMessage(CC.RED + "That time is an invalid format");
            return;
        }

        if (time <= 0) {
            time = 0L;
        }
        koTHGame.setStartTimeMillis(time);
        sender.sendMessage(CC.KOTH + "You have updated the default koth time.");
    }

    @Command(names = {"koth start"}, async = true, permission = "hcf.koth.start")
    public static void kothStartCommand(CommandSender sender, @Param(name = "koth") KoTHGame koTHGame) {
        if (koTHGame.getOwningTeam() == null) {
            return;
        }

        if (koTHGame.getOwningTeam().getClaim() == null) {
            sender.sendMessage(CC.KOTH + CC.RED + "You need to claim the koths claim using /systeam claim " + koTHGame.getName());
            return;
        }

        if (koTHGame.getCaptureCuboid() == null) {
            sender.sendMessage(CC.KOTH + CC.RED + "You need to set the koths capzone using /koth setcapzone " + koTHGame.getName());
            return;
        }

        if (koTHGame.isActive()) {
            sender.sendMessage(CC.KOTH + CC.RED + "That koth is already active");
            return;
        }

        koTHGame.start();
    }

    @Command(names = {"koth cancel", "koth stop"}, async = true, permission = "hcf.koth.cancel")
    public static void kothCancelCommand(CommandSender sender, @Param(name = "koth") KoTHGame koTHGame) {
        if (!koTHGame.isActive()) {
            sender.sendMessage(CC.KOTH + CC.RED + "That koth is not currently active");
            return;
        }

        koTHGame.end(true);
    }

    @Command(names = {"koth claimcapzone"}, async = true, permission = "op")
    public static void setCapzoneCommand(Player player, @Param(name = "koth") KoTHGame koTHGame) {

        if (!(koTHGame.getOwningTeam() != null && koTHGame.getOwningTeam().getClaim() != null)) {
            player.sendMessage(CC.KOTH + CC.GREEN + "You need to set a team claim first using /systeam claim");
            return;
        }

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (teamProfile == null) {
            return;
        }
        teamProfile.setSelectedLocations(new Location[2]);
        teamProfile.setClaimingForGame(koTHGame.getId());

        if (!player.getInventory().contains(GameClaimListener.GAME_CLAIM_WAND)) {
            player.getInventory().addItem(GameClaimListener.GAME_CLAIM_WAND);
        }

        player.sendMessage(CC.KOTH + CC.GREEN + "You are now claiming the capzone for " + koTHGame.getName());
    }

    @Command(names = {"koth toggle"}, async = true, permission = "op")
    public static void kothToggleCommand(CommandSender sender, @Param(name = "koth") KoTHGame koTHGame) {
        if (koTHGame.isActive()) {
            koTHGame.end(true);
        } else {
            koTHGame.start();
        }
    }

    @Command(names = "koth remove", async = true, permission = "hcf.koth.remove")
    public static void kothRemoveCommand(CommandSender sender, @Param(name = "koth") KoTHGame koTHGame) {
        GAME_HANDLER.getGameMap().remove(koTHGame.getId());
        GAME_HANDLER.getGamesCollection().deleteOne("id", koTHGame.getId().toString());
        sender.sendMessage(CC.GREEN + "You have removed " + koTHGame.getName());
    }

    @Command(names = {"koth list"}, async = true)
    public static void kothListCommand(CommandSender sender) {
        List<SimpleText> messages = new ArrayList<>();

        messages.add(new SimpleText("&7&m---------------------------------"));

        List<KoTHGame> games = new ArrayList<>(GAME_HANDLER.getGamesByType(KoTHGame.class));
        games.removeIf(koTHGame -> koTHGame.getCaptureCuboid() == null);
        games.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()));
        for (KoTHGame koth : games) {
            if (koth.getCaptureCuboid() == null) {
                continue;
            }
            Location center = koth.getCaptureCuboid().getCenter();
            String location = " &e(" + center.getBlockX() + ", " + center.getBlockZ() + ")";
            SimpleText simpleText = new SimpleText("&e - " + koth.getColor() + koth.getName() + location);

            if (sender.isOp() || (sender.hasPermission("koth.start") && sender.hasPermission("koth.cancel"))) {
                simpleText.hover(CC.GOLD + "Click to toggle koth");
                simpleText.click("/koth toggle " + koth.getName().replace(" ", "-"));
            }

            messages.add(simpleText);
        }

        messages.add(new SimpleText("&7&m---------------------------------"));
        if (messages.size() > 2) {
            messages.forEach(simpleText -> simpleText.send(sender));
        }
    }

    public static void createKOTH(Player player, String name, String color) {
        Team team = TEAM_HANDLER.getTeamByName(name);

        if (team != null) {
            player.sendMessage(CC.KOTH + CC.RED + "A team with that name already exists, you cannot create a koth over it");
            return;
        }

        SystemTeam systemTeam = new SystemTeam(UUID.randomUUID(), name, color);
        boolean isPalace = name.equalsIgnoreCase("Palace");

        if (isPalace) {
            systemTeam.setEnderpearl(false);
            systemTeam.setCanHome(false);
        }

        UUID gameId = UUID.randomUUID();
        systemTeam.setSave(true);
        systemTeam.setLinkedGameId(gameId);
        TEAM_HANDLER.addTeam(systemTeam);
        new TeamCreateEvent(player, systemTeam).call(HCF.getInstance());
        KoTHGame koTHGame = new KoTHGame(gameId, systemTeam.getId());

        if (isPalace) {
            koTHGame.setStartTimeMillis(TimeUnit.MINUTES.toMillis(30));
        }

        GAME_HANDLER.addGame(koTHGame);
        player.sendMessage(CC.KOTH + CC.GREEN + "You have made that koth, now use /systeam claim " + name);
    }
}
