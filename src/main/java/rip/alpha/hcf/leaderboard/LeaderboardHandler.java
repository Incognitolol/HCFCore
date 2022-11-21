package rip.alpha.hcf.leaderboard;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LeaderboardHandler {

    private final HCF instance;
    private final Int2ObjectMap<UUID> teamLeaderboardMap;

    public LeaderboardHandler(HCF instance) {
        this.instance = instance;
        this.teamLeaderboardMap = new Int2ObjectOpenHashMap<>(10);
        HCF.getInstance().getScheduledExecutorService().scheduleAtFixedRate(new LeaderboardTask(this), 0, 1, TimeUnit.MINUTES);
    }

    public UUID getTeam(int index) {
        if (index < 0 || index > 9) {
            return null;
        }
        return this.teamLeaderboardMap.get(index);
    }

    public void update() {
        List<PlayerTeam> teams = new ArrayList<>(this.instance.getTeamHandler().getPlayerTeams());
        teams.sort(Comparator.comparingInt(team -> -team.getPoints()));
        this.teamLeaderboardMap.clear();
        for (int i = 0; i < Math.min(teams.size(), 10); i++) {
            this.teamLeaderboardMap.put(i, teams.get(i).getId());
        }
    }
}
