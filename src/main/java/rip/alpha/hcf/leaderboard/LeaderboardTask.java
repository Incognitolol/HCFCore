package rip.alpha.hcf.leaderboard;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LeaderboardTask implements Runnable {

    private final LeaderboardHandler leaderboardHandler;

    @Override
    public void run() {
        this.leaderboardHandler.update();
    }
}
