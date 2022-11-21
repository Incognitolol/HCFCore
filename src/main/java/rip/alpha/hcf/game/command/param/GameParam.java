package rip.alpha.hcf.game.command.param;

import lombok.RequiredArgsConstructor;
import net.mcscrims.command.param.IParameter;
import net.mcscrims.libraries.util.CC;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.game.Game;
import rip.alpha.hcf.game.GameHandler;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class GameParam implements IParameter<Game> {

    private final GameHandler gameHandler;

    @Override
    public Game transform(CommandSender commandSender, String name) {
        Game game = this.gameHandler.getGameByName(name.replace("-", " "));

        if (game == null) {
            commandSender.sendMessage(CC.RED + "That game either doesnt exist or isn't a game to begin with");
            return null;
        }

        return game;
    }

    @Override
    public List<String> getTabComplete(CommandSender commandSender) {
        List<String> tabComplete = new ArrayList<>();
        this.gameHandler.getGames().forEach(game -> tabComplete.add(game.getName().replace(" ", "-")));
        return tabComplete;
    }
}
