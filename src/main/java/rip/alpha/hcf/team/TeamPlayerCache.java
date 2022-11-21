package rip.alpha.hcf.team;

import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamPlayerCache {

    private final Map<UUID, UUID> playerUUIDToTeamId;

    public TeamPlayerCache() {
        this.playerUUIDToTeamId = new HashMap<>();
    }

    public void cacheUUID(UUID uuid, PlayerTeam team) {
        this.playerUUIDToTeamId.put(uuid, team.getId());
    }

    public void uncacheUUID(UUID uuid) {
        this.playerUUIDToTeamId.remove(uuid);
    }

    public UUID getTeamId(UUID uuid) {
        return this.playerUUIDToTeamId.get(uuid);
    }
}
