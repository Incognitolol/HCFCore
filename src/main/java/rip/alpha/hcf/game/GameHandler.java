package rip.alpha.hcf.game;

import lombok.Getter;
import net.mcscrims.command.CommandFramework;
import net.mcscrims.libraries.Libraries;
import net.mcscrims.libraries.mongo.EasyMongoCollection;
import net.mcscrims.libraries.util.TaskUtil;
import org.bson.Document;
import org.bukkit.plugin.PluginManager;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.game.command.CTPCommands;
import rip.alpha.hcf.game.command.KoTHCommands;
import rip.alpha.hcf.game.command.PalaceCommand;
import rip.alpha.hcf.game.command.param.CTPParam;
import rip.alpha.hcf.game.command.param.GameParam;
import rip.alpha.hcf.game.command.param.KoTHParam;
import rip.alpha.hcf.game.impl.CTPGame;
import rip.alpha.hcf.game.impl.KoTHGame;
import rip.alpha.hcf.game.listener.GameClaimListener;
import rip.alpha.hcf.game.listener.GameListener;
import rip.alpha.hcf.game.schedule.GameScheduleHandler;
import rip.alpha.hcf.team.impl.SystemTeam;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
public class GameHandler {

    private static final String GAME_CLASSES_PACKAGE = "rip.alpha.hcf.game.impl";

    private final EasyMongoCollection gamesCollection;

    private final ConcurrentHashMap<UUID, Game> gameMap;
    private final Map<UUID, UUID> claimingMap;
    private final GameScheduleHandler gameScheduleHandler;

    public GameHandler(HCF instance) {
        this.gamesCollection = instance.getMongoHelper().fetchMongoCollection("games");

        this.gameMap = new ConcurrentHashMap<>();
        this.claimingMap = new HashMap<>();

        HCF.getInstance().getScheduledExecutorService()
                .scheduleAtFixedRate(new GameTask(this), 1L, 1L, TimeUnit.MILLISECONDS);
        TaskUtil.runAsync(this::load, HCF.getInstance());

        PluginManager pluginManager = instance.getServer().getPluginManager();
        pluginManager.registerEvents(new GameListener(this), instance);
        pluginManager.registerEvents(new GameClaimListener(this), instance);

        CommandFramework commandFramework = Libraries.getInstance().getCommandFramework();

        commandFramework.registerParameter(new GameParam(this), Game.class);
        commandFramework.registerParameter(new KoTHParam(this), KoTHGame.class);
        commandFramework.registerParameter(new CTPParam(this), CTPGame.class);

        commandFramework.registerClass(KoTHCommands.class);
        commandFramework.registerClass(PalaceCommand.class);
        commandFramework.registerClass(CTPCommands.class);

        this.gameScheduleHandler = new GameScheduleHandler(instance, this);
    }

    public void addGame(Game game) {
        this.gameMap.put(game.getId(), game);
        this.saveGame(game);
    }

    public Game getGameById(UUID uuid) {
        return this.gameMap.get(uuid);
    }

    public Game getGameByName(String name) {
        for (Game game : this.getGames()) {
            if (game.getName().equalsIgnoreCase(name)) {
                return game;
            }
        }
        return null;
    }

    public <T> Set<T> getGamesByType(Class<T> clazz) {
        Set<T> set = new HashSet<>();
        for (Game game : this.getGames()) {
            if (game.getClass().equals(clazz)) {
                set.add((T) game);
            }
        }
        return set;
    }

    private void load() {
        this.gameMap.clear();

        for (Document document : this.gamesCollection.fetchAllDocuments()) {
            try {
                String clazz = document.getString("gameClass");
                UUID uuid = UUID.fromString(document.getString("id"));
                Class<? extends Game> eventClass = (Class<? extends Game>) Class.forName(GAME_CLASSES_PACKAGE + "." + clazz);

                if (eventClass.getSuperclass().equals(CapturableGame.class)) {
                    if (!document.containsKey("owningTeamId")) {
                        continue;
                    }
                    UUID owningTeamId = UUID.fromString(document.getString("owningTeamId"));
                    SystemTeam team = HCF.getInstance().getTeamHandler().getSystemTeamById(owningTeamId);
                    if (team == null) {
                        continue; //void any games that are linked to non existing teams now
                    }
                    Game game = eventClass.getConstructor(UUID.class, UUID.class).newInstance(uuid, owningTeamId);
                    game.fromDocument(document);
                    this.gameMap.put(game.getId(), game);
                }
            } catch (Exception e) {
                e.printStackTrace();
                HCF.log(logger -> logger.severe("Couldnt load a game"));
            }
        }
    }

    public CopyOnWriteArraySet<Game> getGames() {
        return new CopyOnWriteArraySet<>(this.gameMap.values());
    }

    public void save() {
        for (Game game : this.getGames()) {
            this.saveGame(game);
        }
    }

    private void saveGame(Game game) {
        Document document = game.toDocument();
        if (document == null) {
            return;
        }
        this.gamesCollection.insert("id", game.getId().toString(), document);
    }
}
