package rip.alpha.hcf.game;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GameTask implements Runnable {

    private final GameHandler gameHandler;

    @Override
    public void run() {
        try {
            for (Game game : this.gameHandler.getGames()) {
                if (!game.isActive()) {
                    continue;
                }
                game.tick();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
