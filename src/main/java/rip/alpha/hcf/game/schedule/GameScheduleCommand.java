package rip.alpha.hcf.game.schedule;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.SimpleText;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.game.Game;
import rip.alpha.hcf.game.GameHandler;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameScheduleCommand {

    private static final GameHandler GAME_HANDLER = HCF.getInstance().getGameHandler();
    private static final GameScheduleHandler GAME_SCHEDULE_HANDLER = GAME_HANDLER.getGameScheduleHandler();

    @Command(names = {"schedule add"}, permission = "op", async = true)
    public static void scheduleAddCommand(CommandSender commandSender, @Param(name = "game") Game game, @Param(name = "time", wildcard = true) String timeString) {
        try {
            Date date = GameScheduleEntry.DATE_PARSE_FORMAT.parse(timeString);
            long unixTime = date.getTime();

            if (GAME_SCHEDULE_HANDLER.getGameScheduleMap().containsKey(unixTime)) {
                commandSender.sendMessage(CC.RED + "There is already a scheduled game at that time.");
                return;
            }

            String name = game.getName();
            String color = game.getColor();

            GameScheduleEntry gameScheduleEntry = new GameScheduleEntry(unixTime, game.getName(), color + name);
            GAME_SCHEDULE_HANDLER.registerEntry(gameScheduleEntry);
            commandSender.sendMessage(CC.GREEN + "Your game has been scheduled for " + GameScheduleEntry.DATE_CHAT_FORMAT.format(date));
        } catch (ParseException e) {
            commandSender.sendMessage(CC.RED + "Your time format must be as the following");
            commandSender.sendMessage(CC.RED + " - yyyy/mm/dd hh:mm:ss");
            commandSender.sendMessage(CC.RED + " - Eg. 2000/01/01 01:01:01");
        }
    }

    @Command(names = {"schedule remove"}, permission = "op", async = true)
    public static void scheduleRemoveCommand(CommandSender commandSender, @Param(name = "time", wildcard = true) String timeString) {
        try {
            Date date = GameScheduleEntry.DATE_CHAT_FORMAT.parse(timeString);
            long unixTime = date.getTime();

            if (!GAME_SCHEDULE_HANDLER.getGameScheduleMap().containsKey(unixTime)) {
                commandSender.sendMessage(CC.RED + "There is no game hosted at that time.");
                return;
            }

            GAME_SCHEDULE_HANDLER.getGameScheduleMap().remove(unixTime);
            GAME_SCHEDULE_HANDLER.dropEntry(unixTime);
            commandSender.sendMessage(CC.GREEN + "The game scheduled for " + GameScheduleEntry.DATE_CHAT_FORMAT.format(date) + " has been cancelled");
        } catch (ParseException e) {
            commandSender.sendMessage(CC.RED + "Your time format must be as the following");
            commandSender.sendMessage(CC.RED + " - yyyy/mm/dd hh:mm:ss");
            commandSender.sendMessage(CC.RED + " - Eg. 2000/01/01 01:01:01");
        }
    }

    @Command(names = {"schedule removebyunix"}, permission = "op", async = true)
    public static void scheduleRemoveByUnixCommand(CommandSender commandSender, @Param(name = "unixTime") Long unixTime) {
        if (!GAME_SCHEDULE_HANDLER.getGameScheduleMap().containsKey(unixTime)) {
            commandSender.sendMessage(CC.RED + "There is no game hosted at that time.");
            return;
        }

        GAME_SCHEDULE_HANDLER.getGameScheduleMap().remove(unixTime);
        commandSender.sendMessage(CC.GREEN + "The game scheduled for " + GameScheduleEntry.DATE_CHAT_FORMAT.format(new Date(unixTime)) + " has been cancelled");
    }

    @Command(names = {"schedule list", "koth schedule"}, async = true)
    public static void scheduleListCommand(CommandSender sender) {
        List<GameScheduleEntry> entryList = GAME_SCHEDULE_HANDLER.getSortedScheduleList();

        if (entryList.size() <= 0) {
            sender.sendMessage(CC.RED + "There are no scheduled games.");
            return;
        }

        List<SimpleText> message = new ArrayList<>();
        message.add(new SimpleText("&7&m---------------------------------------"));
        message.add(new SimpleText("&6Game Schedule &7(" + entryList.size() + ")"));

        for (GameScheduleEntry entry : entryList) {
            SimpleText simpleText = new SimpleText(entry.getColorName() + " &ecan be contested on " + entry.formatDateTime() + "!");

            if (sender.isOp()) {
                simpleText.hover("&7Click to remove from schedule list");
                simpleText.click("/schedule removebyunix " + entry.getStartTime());
            }

            message.add(simpleText);
        }

        message.add(new SimpleText("&7&m---------------------------------------"));
        message.forEach(text -> text.send(sender));
    }

}
