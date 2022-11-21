package rip.alpha.hcf.profile;

import net.mcscrims.libraries.mongo.EasyMongoCollection;
import org.bson.Document;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.statistics.StatsListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TeamProfileHandler {

    private final Map<UUID, TeamProfile> teamProfileMap;
    private final EasyMongoCollection profileCollection;

    public TeamProfileHandler(HCF instance) {
        this.teamProfileMap = new ConcurrentHashMap<>();
        this.profileCollection = instance.getMongoHelper().fetchMongoCollection("profiles");

        instance.getServer().getPluginManager().registerEvents(new TeamProfileListener(this), instance);
        instance.getServer().getPluginManager().registerEvents(new StatsListener(this), instance);

        HCF.getInstance().getScheduledExecutorService().scheduleAtFixedRate(new TeamProfileTask(this), 1, 1, TimeUnit.MINUTES);
        HCF.getInstance().getScheduledExecutorService().scheduleAtFixedRate(new TeamPlaytimeTask(), 1, 1, TimeUnit.SECONDS);
    }

    TeamProfile createProfile(UUID uuid) {
        return new TeamProfile(uuid);
    }

    void addProfile(TeamProfile profile) {
        this.teamProfileMap.put(profile.getUuid(), profile);
    }

    void loadProfile(TeamProfile profile) {
        Document document = this.profileCollection.fetchDocument("uuid", profile.getUuid().toString());
        if (document == null) {
            return;
        }
        profile.fromDocument(document);
    }

    void removeProfile(UUID uuid) {
        this.teamProfileMap.remove(uuid);
    }

    public TeamProfile getProfileOrLoad(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        TeamProfile profile = this.getProfile(uuid);
        if (profile != null) {
            return profile;
        }

        profile = this.createProfile(uuid);
        this.loadProfile(profile);
        this.addProfile(profile);
        profile.setRemove(true); //set to uncache

        return profile;
    }

    public TeamProfile getProfile(UUID uuid) {
        return this.teamProfileMap.get(uuid);
    }

    public TeamProfile getProfile(Player player) {
        return this.getProfile(player.getUniqueId());
    }

    public void saveProfile(TeamProfile profile) {
        this.profileCollection.insert("uuid", profile.getUuid().toString(), profile.toDocument());
        profile.setSave(false);
    }

    public Set<TeamProfile> getProfiles() {
        return new HashSet<>(this.teamProfileMap.values());
    }
}
