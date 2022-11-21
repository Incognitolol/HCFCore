package rip.alpha.hcf.team;

import lombok.Getter;
import net.mcscrims.command.CommandFramework;
import net.mcscrims.libraries.Libraries;
import net.mcscrims.libraries.mongo.EasyMongoCollection;
import net.mcscrims.libraries.util.BlockUtil;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;
import net.mcscrims.libraries.util.listeners.ClassUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.command.param.PlayerTeamParam;
import rip.alpha.hcf.team.command.param.SystemTeamParam;
import rip.alpha.hcf.team.command.param.TeamChatModeParam;
import rip.alpha.hcf.team.command.param.TeamParam;
import rip.alpha.hcf.team.event.other.ClaimChangeEvent;
import rip.alpha.hcf.team.grid.ClaimEntry;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.impl.SystemTeam;
import rip.alpha.hcf.team.listener.*;
import rip.alpha.hcf.team.task.TeamCacheTask;
import rip.alpha.hcf.team.task.TeamDTRTask;
import rip.alpha.hcf.team.task.TeamSaveTask;
import rip.alpha.hcf.timer.impl.CombatTagTimer;
import rip.alpha.hcf.timer.impl.PvPTimer;
import rip.alpha.hcf.visual.VisualBlockEntry;
import rip.foxtrot.spigot.fSpigot;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public class TeamHandler {

    private static final String CLAIM_LEAVING_MESSAGE = CC.translate("&eNow leaving:&r %s");
    private static final String CLAIM_ENTER_MESSAGE = CC.translate("&eNow entering:&r %s");
    private static final String TEAM_NAME_FORMAT = CC.translate("%s&e (&r%s&e)");
    private static final String WARZONE_NAME = CC.translate("&cWarzone&r");
    private static final String WILDERNESS_NAME = CC.translate("&aWilderness&r");

    private final EasyMongoCollection playerTeamCollection;
    private final EasyMongoCollection systemTeamCollection;

    private final TeamPlayerCache teamPlayerCache;

    private final Map<UUID, PlayerTeam> playerTeamMap;
    private final Map<UUID, SystemTeam> systemTeamMap;

    private final Set<String> blacklistedNames;

    public TeamHandler(TeamPlayerCache teamPlayerCache, HCF instance) {
        this.playerTeamMap = new HashMap<>();
        this.systemTeamMap = new HashMap<>();

        this.teamPlayerCache = teamPlayerCache;

        this.playerTeamCollection = instance.getMongoHelper().fetchMongoCollection("player_teams");
        this.systemTeamCollection = instance.getMongoHelper().fetchMongoCollection("system_teams");

        this.blacklistedNames =
                Arrays.stream(instance.getConfiguration().getBlacklistedTeamNames())
                        .map(String::toLowerCase).collect(Collectors.toSet());

        CommandFramework commandFramework = Libraries.getInstance().getCommandFramework();
        commandFramework.registerParameter(new TeamParam(this), Team.class);
        commandFramework.registerParameter(new PlayerTeamParam(this), PlayerTeam.class);
        commandFramework.registerParameter(new SystemTeamParam(this), SystemTeam.class);
        commandFramework.registerParameter(new TeamChatModeParam(), PlayerTeam.TeamChatMode.class);
        commandFramework.registerClasses(ClassUtils.getClassesInPackage(instance, "rip.alpha.hcf.team.command.impl.player"));
        commandFramework.registerClasses(ClassUtils.getClassesInPackage(instance, "rip.alpha.hcf.team.command.impl.system"));
        commandFramework.registerClasses(ClassUtils.getClassesInPackage(instance, "rip.alpha.hcf.team.command.impl.admin"));


        PluginManager pluginManager = instance.getServer().getPluginManager();
        pluginManager.registerEvents(new TeamListener(this), instance);
        pluginManager.registerEvents(new TeamProtectionListener(this), instance);
        pluginManager.registerEvents(new PlayerTeamListener(this), instance);
        pluginManager.registerEvents(new SystemTeamListener(this), instance);
        pluginManager.registerEvents(new TeamClaimListener(this), instance);

        ScheduledExecutorService executorService = HCF.getInstance().getScheduledExecutorService();
        executorService.scheduleAtFixedRate(new TeamSaveTask(this), 1, 1, TimeUnit.MINUTES);
        executorService.scheduleAtFixedRate(new TeamDTRTask(this), 1, 1, TimeUnit.MINUTES);
        executorService.scheduleAtFixedRate(new TeamCacheTask(), 1, 1, TimeUnit.MINUTES);

        fSpigot.INSTANCE.addMovementHandler(new TeamMovementHandler(this));
        this.load();
    }

    public boolean isBlacklistedTeamName(String name) {
        return this.blacklistedNames.contains(name.toLowerCase());
    }

    public void addTeam(Team team) {
        if (team instanceof PlayerTeam) {
            PlayerTeam playerTeam = (PlayerTeam) team;
            this.playerTeamMap.put(playerTeam.getId(), playerTeam);
        } else if (team instanceof SystemTeam) {
            SystemTeam systemTeam = (SystemTeam) team;
            this.systemTeamMap.put(systemTeam.getId(), systemTeam);
        }
    }

    public void removeTeam(Team team) {
        if (team instanceof PlayerTeam) {
            PlayerTeam playerTeam = (PlayerTeam) team;
            playerTeam.getMembers().forEach(teamMember -> this.teamPlayerCache.uncacheUUID(teamMember.getUuid()));
            this.playerTeamMap.remove(team.getId());
            this.playerTeamCollection.deleteOne("id", team.getId().toString());
        } else if (team instanceof SystemTeam) {
            this.systemTeamMap.remove(team.getId());
            this.systemTeamCollection.deleteOne("id", team.getId().toString());
        }
    }

    public Set<Team> getTeams() {
        Set<Team> teams = new HashSet<>();
        teams.addAll(playerTeamMap.values());
        teams.addAll(systemTeamMap.values());
        return teams;
    }

    public Team getTeamByName(String name) {
        for (Team team : this.getTeams()) {
            if (team.getName().equalsIgnoreCase(name)) {
                return team;
            }
        }
        return null;
    }

    public Team getTeamById(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        Team team = this.systemTeamMap.get(uuid);
        if (team == null) {
            team = this.playerTeamMap.get(uuid);
        }
        return team;
    }

    public PlayerTeam getPlayerTeamById(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return this.playerTeamMap.get(uuid);
    }

    public PlayerTeam getPlayerTeamByName(String name) {
        for (PlayerTeam team : this.playerTeamMap.values()) {
            if (team.getName().equalsIgnoreCase(name)) {
                return team;
            }
        }
        return null;
    }

    public Collection<PlayerTeam> getPlayerTeams() {
        return new ArrayList<>(this.playerTeamMap.values());
    }

    public PlayerTeam getPlayerTeamByPlayer(UUID uuid) {
        return this.getPlayerTeamById(this.teamPlayerCache.getTeamId(uuid));
    }

    public PlayerTeam getPlayerTeamByPlayer(Player player) {
        return this.getPlayerTeamByPlayer(player.getUniqueId());
    }

    public Set<SystemTeam> getSystemTeams() {
        return new HashSet<>(this.systemTeamMap.values());
    }

    public SystemTeam getSystemTeamByName(String name) {
        for (SystemTeam systemTeam : this.getSystemTeams()) {
            if (systemTeam.getName().equalsIgnoreCase(name)) {
                return systemTeam;
            }
        }
        return null;
    }

    public SystemTeam getSystemTeamById(UUID uuid) {
        return this.systemTeamMap.get(uuid);
    }

    public Team getTeamByLocation(Location location) {
        return this.getTeamById(HCF.getInstance().getClaimGrid().getTeamIdByLocation(location));
    }

    public void saveTeam(Team team) {
        EasyMongoCollection mongoCollection = null;

        if (team instanceof SystemTeam) {
            mongoCollection = this.systemTeamCollection;
        } else if (team instanceof PlayerTeam) {
            mongoCollection = this.playerTeamCollection;
        }

        if (mongoCollection == null) {
            return;
        }

        mongoCollection.insert("id", team.getId().toString(), team.toDocument());
        team.setSave(false);
    }

    public String createClaim(Team team, ACuboid cuboid) {
        if (team.getClaim() != null) {
            return CC.RED + "Your team already has a claim!";
        }

        ACuboid expandedClaim = cuboid.expand(3);

        Set<ClaimEntry> mapSet =
                HCF.getInstance().getClaimGrid()
                        .getGridData(expandedClaim.getWorld().getUID(), expandedClaim.getMinX(), expandedClaim.getMinZ(), expandedClaim.getMaxX(), expandedClaim.getMaxZ());

        for (ClaimEntry entry : mapSet) {
            if (entry.getTeamId() == null) {
                continue;
            }
            ACuboid other = entry.getCuboid();
            if (other.overlaps(cuboid)) {
                return CC.RED + "You are attempting to claim over another claim";
            }

            if (team instanceof SystemTeam) {
                continue;
            }

            if (other.overlaps(expandedClaim)) {
                return CC.RED + "You are attempting to claim too close to another claim";
            }
        }

        if (team instanceof PlayerTeam) {
            int x = Math.abs(cuboid.getMaxX() - cuboid.getMinX()) + 1; //because the block itself isnt accounted for
            int z = Math.abs(cuboid.getMaxZ() - cuboid.getMinZ()) + 1; //because the block itself isnt accounted for

            if (x < 5 || z < 5) {
                return CC.RED + "This is an invalid claim, the claim has to be atleast 5x5";
            }
            if (x > 3 * z || z > 3 * x) {
                return CC.RED + "One side of your claim cannot be more than 3 times larger than the other!";
            }
            int blocks = x * z;
            if (blocks > (400 * 8)) {
                return CC.RED + "Your claim is too big";
            }
            PlayerTeam playerTeam = (PlayerTeam) team;
            int price = this.calculatePrice(blocks);

            if (playerTeam.getBalance() < price) {
                return CC.RED + "Your team does not have enough balance to purchase this claim";
            }
            if (playerTeam.getClaim() != null) {
                return "Your team already has a claim, please use /team unclaim to remove your teams claim";
            }
            playerTeam.setBalance(playerTeam.getBalance() - price);
        }

        team.setClaim(cuboid);
        team.setSave(true);
        return CC.GREEN + "Your claim has been made";
    }

    public void hideMap(Player player, TeamProfile profile) {
        if (profile.isPillars()) {
            HCF.getInstance().getVisualHandler().clearVisualBlocks(player, VisualBlockEntry::isPillar);
            profile.setPillars(false);
            player.sendMessage(CC.GREEN + "You are no-longer seeing team claim pillars");
        }
    }

    public void showMap(Player player, TeamProfile profile) {
        if (!profile.isPillars()) {
            Map<String, Material> teamMap = new HashMap<>();
            String worldName = player.getWorld().getName();

            Set<ClaimEntry> entries = HCF.getInstance().getClaimGrid().getGridData(player.getLocation(), 50, 10, 50);

            if (!entries.isEmpty()) {
                for (ClaimEntry entry : entries) {
                    Team team = HCF.getInstance().getTeamHandler().getTeamById(entry.getTeamId());
                    if (team == null) {
                        continue;
                    }
                    if (team.getClaim() != null) {
                        if (!team.getClaim().getWorldName().equals(worldName)) {
                            continue;
                        }
                        Material material = BlockUtil.getRandom();
                        teamMap.put(team.getDisplayName(player), material);
                        HCF.getInstance().getVisualHandler().showCuboid(player, material, team.getId(), team.getClaim());
                    }
                }
            }

            if (teamMap.size() <= 0) {
                player.sendMessage(CC.RED + "No teams found within an area around you.");
                return;
            }

            player.sendMessage(CC.GREEN + "You are now seeing all nearby team claim pillars");
            player.sendMessage(CC.GOLD + "Nearby teams: ");
            teamMap.forEach((teamName, material) -> player.sendMessage(CC.GRAY + teamName + CC.GRAY + ": " + CC.YELLOW + material.name()));
            profile.setPillars(true);
        }
    }

    public void clearClaiming(Player player, TeamProfile profile) {
        HCF.getInstance().getVisualHandler().clearVisualBlocks(player, VisualBlockEntry::isClaimTemp);
        profile.setClaimingFor(null);
        profile.setSelectedLocations(new Location[2]);
        HCF.getInstance().getTeamHandler().hideMap(player, profile);
        player.getInventory().removeItem(TeamClaimListener.CLAIM_WAND);
        player.updateInventory();
    }

    public int calculatePrice(int blocks) {
        return blocks * 4;
    }

    public int calculatePrice(int x, int z) {
        return this.calculatePrice(x * z);
    }

    public int calculatePrice(ACuboid cuboid) {
        int x = Math.abs(cuboid.getMaxX() - cuboid.getMinX()) + 1; //because the block itself isnt accounted for
        int z = Math.abs(cuboid.getMaxZ() - cuboid.getMinZ()) + 1; //because the block itself isnt accounted for
        return this.calculatePrice(x, z);
    }

    public boolean handleMove(Player player, Location from, Location to) {
        if (from.getBlockZ() == to.getBlockZ() && from.getBlockX() == to.getBlockX()) {
            return false;
        }
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);

        UUID lastClaimUUID = profile.getLastTeamClaim();
        Team toTeam = HCF.getInstance().getTeamHandler().getTeamByLocation(to);

        if (lastClaimUUID == null && toTeam == null) { //Wilderness -> Wilderness
            return false;
        }

        if (lastClaimUUID != null && toTeam != null) {
            if (lastClaimUUID.equals(toTeam.getId())) { //Team -> Team
                return false;
            }
        }

        boolean handleUnder15Mans = false;
        if (!HCF.getInstance().getConfiguration().isAllowUnder15MansInKoths()) {

            PlayerTeam playerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
            if (playerTeam != null && playerTeam.getOnlineMembers().size() < 15) {
                handleUnder15Mans = true;
            }
        }

        boolean combatTagged = HCF.getInstance().getProfileHandler().getProfile(player).hasTimer(CombatTagTimer.class);
        boolean pvpCooldown = HCF.getInstance().getProfileHandler().getProfile(player).hasTimer(PvPTimer.class);

        if (toTeam != null) {
            if (toTeam instanceof SystemTeam) {
                SystemTeam systemTeam = (SystemTeam) toTeam;
                if (systemTeam.isSafezone() && combatTagged) {
                    return true;
                } else if (systemTeam.isGame() && (pvpCooldown || handleUnder15Mans)) {
                    return true;
                } else if (systemTeam.isDontAllowPvpTimer() && pvpCooldown){
                    return true;
                }
            } else if (toTeam instanceof PlayerTeam) {
                if (pvpCooldown) {
                    return true;
                }
            }
        }

        Team fromTeam = lastClaimUUID == null ? null : HCF.getInstance().getTeamHandler().getTeamById(lastClaimUUID);
        String fromTeamMessage = String.format(CLAIM_LEAVING_MESSAGE, this.formatTeamMessage(player, from, fromTeam));
        String toTeamMessage = String.format(CLAIM_ENTER_MESSAGE, this.formatTeamMessage(player, to, toTeam));
        profile.setLastTeamClaim(toTeam == null ? null : toTeam.getId());

        new ClaimChangeEvent(player, profile, fromTeam, toTeam).call(HCF.getInstance());

        player.sendMessage(fromTeamMessage);
        player.sendMessage(toTeamMessage);

        return false;
    }

    private String formatTeamMessage(Player player, Location location, Team team) {
        boolean safezone = false;

        if (team instanceof SystemTeam) {
            SystemTeam systemTeam = (SystemTeam) team;
            if (systemTeam.isSafezone()) {
                safezone = true;
            }
        }

        String teamName;
        if (team == null) {
            teamName = HCF.getInstance().getBorderHandler().inWarzone(location) ? WARZONE_NAME : WILDERNESS_NAME;
        } else {
            teamName = team.getDisplayName(player);
        }
        String deathbanMessage = safezone ? CC.GREEN + "Safezone" : CC.RED + "Deathban";
        return String.format(TEAM_NAME_FORMAT, teamName, deathbanMessage);
    }

    private void load() {
        if (this.playerTeamMap.size() > 0 || this.systemTeamMap.size() > 0) {
            HCF.getInstance().getLogger().warning("THERE ARE ALREADY TEAMS IN THE MAPS");
        }

        this.systemTeamMap.clear();
        this.systemTeamCollection.fetchAllDocuments().forEach(document -> {
            UUID teamId = UUID.fromString(document.getString("id"));
            String name = document.getString("name");
            String color = document.getString("color");
            SystemTeam systemTeam = new SystemTeam(teamId, name, color);
            systemTeam.fromDocument(document);
            this.systemTeamMap.put(teamId, systemTeam);
        });

        this.playerTeamMap.clear();
        this.playerTeamCollection.fetchAllDocuments().forEach(document -> {
            UUID teamId = UUID.fromString(document.getString("id"));
            String name = document.getString("name");
            PlayerTeam playerTeam = new PlayerTeam(teamId, name, null, null);
            playerTeam.fromDocument(document);
            this.playerTeamMap.put(teamId, playerTeam);
        });

        HCF.log(logger -> logger.info("Loaded " + this.systemTeamMap.size() + " System Teams"));
        HCF.log(logger -> logger.info("Loaded " + this.playerTeamMap.size() + " Player Teams"));
    }
}
