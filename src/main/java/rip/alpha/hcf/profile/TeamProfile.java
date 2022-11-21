package rip.alpha.hcf.profile;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.InventoryUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.deathban.Death;
import rip.alpha.hcf.profile.settings.Setting;
import rip.alpha.hcf.profile.statistics.ProfileStatTypes;
import rip.alpha.hcf.pvpclass.PvPClass;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.timer.Timer;
import rip.alpha.hcf.timer.impl.ArcherTagTimer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class TeamProfile {
    //Identifiers
    private final UUID uuid;

    //Cache of previous teams
    private UUID lastTeamClaim = null;

    //Claiming
    private UUID claimingFor = null;
    private UUID claimingForGame = null;
    private Location[] selectedLocations = new Location[2];

    //Stats & Settings
    private EnumMap<ProfileStatTypes, Integer> stats = new EnumMap<>(ProfileStatTypes.class);
    private EnumMap<Setting, Boolean> settings = new EnumMap<>(Setting.class);

    //Values
    private int balance = 250;
    private int lives = 0;

    private boolean save = false, remove = false, pillars = false;

    private ItemStack lastThrownPearlItem;
    private int lastThrownPearlEntityId;

    private long lastClaimAttempt = System.currentTimeMillis(), blockGlitchDelay = -1; //interact delay for block glitching with signs.
    private long equipTime = -1, couldntEquipClassMessage = -1, deathbanTime = -1, kitCooldown = -1, livesCooldown = -1;
    private long teamCreateCooldown = -1, lastCrateAttempt = -1, timeBetweenLives = -1, kickCooldown = -1;
    private int playTime = -1, blockFaceType = -1;

    private boolean usedLife = false, reclaimed = false;

    //PvPClass
    private Class<? extends PvPClass> equipPvPClass = null;

    //Active timers
    private final Map<Integer, Timer> activeTimers = new ConcurrentHashMap<>();

    //Deaths
    private final List<Death> recentDeaths = Lists.newArrayList();

    public void addTimer(Timer timer) {
        boolean hadTimer = this.hasTimer(timer.getClass());
        this.activeTimers.put(timer.getId(), timer);

        if (!hadTimer) {
            timer.onApply(this);
        } else {
            timer.onExtend(this);
        }

        if (timer.isSave()) {
            this.save = true;
        }
    }

    public <T extends Timer> T getTimer(Class<? extends T> clazz) {
        Timer timer = this.activeTimers.get(HCF.getInstance().getTimerHandler().getIdByClass(clazz));

        if (timer == null) {
            return null;
        }

        if (!timer.isActive()) {
            return null;
        }

        return (T) timer;
    }

    public boolean hasTimer(Class<? extends Timer> clazz) {
        Timer timer = this.getTimer(clazz);
        return timer != null && timer.isActive();
    }

    public void removeTimer(Class<? extends Timer> clazz) {
        Timer timer = this.getTimer(clazz);
        if (timer != null) {
            this.removeTimer(timer);
        }
    }

    public void removeTimer(Timer timer) {
        Timer removedTimer = this.activeTimers.remove(timer.getId());
        if (removedTimer != null) {
            timer.onRemove(this);
            if (timer.isSave()) {
                this.save = true;
            }
        }
    }

    public Team getLastClaimTeam() {
        if (this.lastTeamClaim == null) {
            return null;
        }
        return HCF.getInstance().getTeamHandler().getTeamById(this.lastTeamClaim);
    }

    public boolean canAttemptClaim() {
        return this.lastClaimAttempt - System.currentTimeMillis() <= 0;
    }

    public long remainingEquipTime() {
        return Math.max(0, this.equipTime - System.currentTimeMillis());
    }

    public long remainingKitEquipTime() {
        return Math.max(0, this.kitCooldown - System.currentTimeMillis());
    }

    public boolean canEquipKit() {
        if (this.kitCooldown == -1) {
            return true;
        }
        return this.remainingKitEquipTime() <= 0;
    }

    public boolean isNotBlockGlitch() {
        if (this.blockGlitchDelay == -1) {
            return true;
        }
        return Math.max(0, this.blockGlitchDelay - System.currentTimeMillis()) <= 0;
    }

    public boolean onLivesCooldown() {
        if (this.livesCooldown == -1) {
            return false;
        }
        return Math.max(0, this.livesCooldown - System.currentTimeMillis()) > 0;
    }

    public boolean canUseLife() {
        if (this.timeBetweenLives == -1) {
            return true;
        }
        return Math.max(0, this.timeBetweenLives - System.currentTimeMillis()) < 0;
    }

    public boolean readyToEquip() {
        if (this.equipTime == -1) {
            return false;
        }
        return this.remainingEquipTime() <= 0;
    }

    public boolean isArcherTagged() {
        return this.hasTimer(ArcherTagTimer.class);
    }

    public boolean isActiveClass(Class<? extends PvPClass> clazz) {
        if (this.equipPvPClass == null) {
            return false;
        }
        return this.equipPvPClass.equals(clazz);
    }

    public boolean isDeathban() {
        if (this.deathbanTime == -1) {
            return false;
        }
        return this.remainingDeathbanTime() > 0L;
    }

    public long remainingDeathbanTime() {
        return Math.max(0, this.deathbanTime - System.currentTimeMillis());
    }

    public int getStat(ProfileStatTypes statisticsType) {
        return this.stats.getOrDefault(statisticsType, 0);
    }

    public boolean getSetting(Setting setting) {
        return this.settings.getOrDefault(setting, setting.getDefaultValue());
    }

    public void setSetting(Setting setting, boolean value) {
        this.settings.put(setting, value);
    }

    public void incrementStat(ProfileStatTypes statisticsType) {
        this.incrementStat(statisticsType, 1);
    }

    public void incrementStat(ProfileStatTypes statisticsType, int amount) {
        this.stats.put(statisticsType, this.getStat(statisticsType) + amount);
        this.save = true;
    }

    public void decrementStat(ProfileStatTypes statisticsType) {
        this.stats.put(statisticsType, this.stats.get(statisticsType) - 1);
        this.save = true;
    }

    public void incrementPlaytime() {
        this.playTime++;
    }

    public void removeLastPearl() {
        if (this.lastThrownPearlEntityId == -1) {
            return;
        }

        Player player = this.toPlayer();
        if (player == null) {
            return;
        }

        for (EnderPearl entity : player.getWorld().getEntitiesByClass(EnderPearl.class)) {
            if (entity.getEntityId() == this.lastThrownPearlEntityId) {
                entity.remove();
                break;
            }
        }

        this.lastThrownPearlEntityId = -1;
    }

    public String createDeathInventory() {
        ItemStack[] inventoryArray = null, armorArray = null;

        Player player = this.toPlayer();
        if (player != null && player.getInventory() != null) {
            inventoryArray = player.getInventory().getContents();
            armorArray = player.getInventory().getArmorContents();
        }

        String inventory = inventoryArray != null ? InventoryUtils.itemStackArrayToBase64(inventoryArray) : "";
        String armor = armorArray != null ? InventoryUtils.itemStackArrayToBase64(armorArray) : "";

        return inventory + ";" + armor;
    }

    public String createDeathInventory(List<ItemStack> inventoryContents, List<ItemStack> armorContents) {

        String inventory = inventoryContents != null ? InventoryUtils.itemStackArrayToBase64((ItemStack[]) inventoryContents.toArray()) : "";
        String armor = (armorContents != null) ? InventoryUtils.itemStackArrayToBase64((ItemStack[]) armorContents.toArray()) : "";

        return inventory + ";" + armor;
    }

    public void loadDeathInventory(Death death) {
        if (death == null) {
            return;
        }
        String[] parts = death.getDeathInventory().split(";");

        Player player = this.toPlayer();
        if (player == null) {
            return;
        }

        try {
            player.getInventory().setContents(InventoryUtils.itemStackArrayFromBase64(parts[0]));
            player.getInventory().setArmorContents(InventoryUtils.itemStackArrayFromBase64(parts[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Player toPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public void addDeath(Death death) {
        this.recentDeaths.add(death);
        this.recentDeaths.sort(Comparator.comparingLong(o -> -o.getDate().getTime()));

        List<Death> recentDeaths = this.recentDeaths.stream().limit(5).collect(Collectors.toList());

        this.recentDeaths.clear();
        this.recentDeaths.addAll(recentDeaths);

        this.save = true;
    }

    public void fromDocument(Document document) {
        if (document.containsKey("timers")) {
            JsonArray timerArray = HCF.PARSER.parse(document.getString("timers")).getAsJsonArray();
            for (JsonElement element : timerArray) {
                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject object = element.getAsJsonObject();
                Timer timer = HCF.getInstance().getTimerHandler().parseTimer(object);

                if (timer == null) {
                    continue;
                }

                if (timer.isSave()) {
                    this.activeTimers.put(timer.getId(), timer);
                }
            }
        }

        this.balance = document.getInteger("balance", this.balance);
        this.deathbanTime = document.get("deathban", this.deathbanTime);
        this.playTime = document.getInteger("playtime", 0);
        this.lives = document.getInteger("lives", 0);
        this.reclaimed = document.getBoolean("reclaimed", false);

        if (document.containsKey("statistics")) {
            Document statisticsDocument = (Document) document.get("statistics");
            if (!statisticsDocument.isEmpty()) {
                for (String key : statisticsDocument.keySet()) {
                    ProfileStatTypes type = ProfileStatTypes.getByName(key);
                    if (type == null) {
                        continue;
                    }
                    int value = (int) statisticsDocument.get(key);
                    this.stats.put(type, value);
                }
            }
        }

        if (document.containsKey("settings")) {
            Document settingsDocument = document.get("settings", Document.class);
            if (!settingsDocument.isEmpty()) {
                for (String key : settingsDocument.keySet()) {
                    Setting setting = Setting.getSettingByName(key);
                    if (setting == null) {
                        continue; //if we remove a setting for example.
                    }
                    Boolean bool = settingsDocument.getBoolean(key);
                    if (bool == null) {
                        continue;
                    }
                    this.settings.put(setting, bool);
                }
            }
        }

        ProfileStatTypes.getValues().forEach(profileStatisticsType -> this.stats.putIfAbsent(profileStatisticsType, 0));

        if (document.containsKey("recentDeaths")) {
            JsonElement element = HCF.PARSER.parse(document.getString("recentDeaths"));
            if (element != null && element.isJsonArray()) {
                JsonArray jsonArray = element.getAsJsonArray();

                for (JsonElement jsonElement : jsonArray) {
                    if (jsonElement != null && jsonElement.isJsonObject()) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        this.addDeath(new Death(jsonObject));
                    }
                }
            }
        }
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("uuid", this.uuid.toString());
        document.put("balance", this.balance);
        document.put("deathban", this.deathbanTime);
        document.put("playtime", this.playTime);
        document.put("lives", this.lives);
        document.put("reclaimed", this.reclaimed);

        if (this.activeTimers.size() > 0) {
            JsonArray timerArray = new JsonArray();
            this.activeTimers.values().forEach(timer -> {
                if (timer.isSave()) {
                    timerArray.add(timer.toJson());
                }
            });
            document.put("timers", timerArray.toString());
        }

        Document statisticsDocument = new Document();
        this.stats.keySet().forEach(statisticsType -> {
            int statistic = this.getStat(statisticsType);
            statisticsDocument.put(statisticsType.name(), statistic);
        });
        document.put("statistics", statisticsDocument);

        Document settingsDocument = new Document();
        this.settings.keySet().forEach(setting -> {
            Boolean value = this.settings.get(setting);
            settingsDocument.put(setting.name(), value);
        });
        document.put("settings", settingsDocument);

        if (!this.recentDeaths.isEmpty()) {
            JsonArray recentDeathsArray = new JsonArray();
            this.recentDeaths.stream().limit(5).forEach(death -> recentDeathsArray.add(death.toJson()));
            document.put("recentDeaths", recentDeathsArray.toString());
        }

        return document;
    }
}
