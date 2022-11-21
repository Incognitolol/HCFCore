package rip.alpha.hcf.game.schedule;

import lombok.Getter;
import net.mcscrims.libraries.Libraries;
import net.mcscrims.libraries.mongo.EasyMongoCollection;
import net.mcscrims.libraries.util.TaskUtil;
import org.bson.Document;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.game.Game;
import rip.alpha.hcf.game.GameHandler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public class GameScheduleHandler {

    private final GameHandler gameHandler;
    private final EasyMongoCollection scheduleCollection;
    private final Map<Long, GameScheduleEntry> gameScheduleMap;

    public GameScheduleHandler(HCF instance, GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.scheduleCollection = instance.getMongoHelper().fetchMongoCollection("game_schedule");
        this.gameScheduleMap = new HashMap<>();
        this.load();

        HCF.getInstance().getScheduledExecutorService().scheduleAtFixedRate(new GameScheduleTask(this), 500, 500, TimeUnit.MILLISECONDS);
        Libraries.getInstance().getCommandFramework().registerClass(GameScheduleCommand.class);
    }

    public List<GameScheduleEntry> getSortedScheduleList() {
        return this.getGameScheduleMap()
                .values()
                .stream()
                .filter(gameScheduleEntry -> !gameScheduleEntry.isStarted())
                .sorted(Comparator.comparingLong(GameScheduleEntry::getStartTime))
                .collect(Collectors.toList());
    }

    public GameScheduleEntry getNextScheduledGame() {
        List<GameScheduleEntry> sorted = this.getSortedScheduleList();
        if (sorted.size() <= 0) {
            return null;
        }
        return sorted.get(0);
    }

    public void registerEntry(GameScheduleEntry entry) {
        this.gameScheduleMap.put(entry.getStartTime(), entry);
    }

    public void dropEntry(long unixTime) {
        this.scheduleCollection.deleteOne("startTime", unixTime);
    }

    public void save() {
        TaskUtil.runAsync(() -> {
            for (GameScheduleEntry value : this.gameScheduleMap.values()) {
                this.save(value);
            }
        }, HCF.getInstance());
    }

    public void save(GameScheduleEntry gameScheduleEntry) {
        TaskUtil.runAsync(() -> {
            if (gameScheduleEntry.isStarted()) {
                return; //dont save anything that has been stared
            }
            this.scheduleCollection.insert("startTime", gameScheduleEntry.getStartTime(), gameScheduleEntry.toDocument());
        }, HCF.getInstance());
    }

    public void startGame(GameScheduleEntry entry) {
        entry.setStarted(true);
        this.gameScheduleMap.remove(entry.getStartTime());
        Game game = this.gameHandler.getGameByName(entry.getGameName());

        if (game == null) {
            return;
        }

        if (game.isActive()) {
            HCF.log(logger -> logger.info("Schedule attempted to start a game that was already active!"));
            return;
        }

        game.start();
    }

    private void load() {
        for (Document document : this.scheduleCollection.fetchAllDocuments()) {
            GameScheduleEntry gameScheduleEntry = GameScheduleEntry.fromDocument(document);
            if (gameScheduleEntry.canBeStarted()) {
                continue; //dont start anything that may be old :shrug:
            }
            this.registerEntry(gameScheduleEntry);
        }
    }
}
