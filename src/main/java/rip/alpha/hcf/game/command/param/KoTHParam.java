package rip.alpha.hcf.game.command.param;

import lombok.RequiredArgsConstructor;
import net.mcscrims.command.param.IParameter;
import net.mcscrims.libraries.util.CC;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.game.Game;
import rip.alpha.hcf.game.GameHandler;
import rip.alpha.hcf.game.impl.KoTHGame;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class KoTHParam implements IParameter<KoTHGame> {

    private final GameHandler gameHandler;

    @Override
    public KoTHGame transform(CommandSender commandSender, String name) {
        Game game = this.gameHandler.getGameByName(name.replace("-", " "));

        if (!(game instanceof KoTHGame)) {
            commandSender.sendMessage(CC.RED + "That game either doesnt exist or isn't a game to begin with");
            return null;
        }

        return (KoTHGame) game;
    }

    @Override
    public List<String> getTabComplete(CommandSender commandSender) {
        List<String> tabComplete = new ArrayList<>();
        this.gameHandler.getGamesByType(KoTHGame.class).forEach(game -> tabComplete.add(game.getName().replace(" ", "-")));
        return tabComplete;
    }
}
