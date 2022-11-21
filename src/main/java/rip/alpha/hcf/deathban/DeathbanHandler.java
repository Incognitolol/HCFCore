package rip.alpha.hcf.deathban;

import lombok.Getter;
import net.mcscrims.basic.Basic;
import net.mcscrims.basic.profile.BasicProfile;
import net.mcscrims.libraries.Libraries;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.RoundingUtil;
import net.mcscrims.libraries.util.TimeUtil;
import net.mcscrims.libraries.util.cuboid.SafeLocation;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import net.mcscrims.permissions.PermissionsPlugin;
import net.mcscrims.permissions.profile.PermissionProfile;
import net.mcscrims.permissions.rank.Rank;
import net.minecraft.server.v1_7_R4.EntityLightning;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_7_R4.PacketPlayOutSpawnEntityWeather;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.HCFConfiguration;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.settings.Setting;
import rip.alpha.hcf.profile.statistics.ProfileStatTypes;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.impl.SystemTeam;
import rip.alpha.hcf.timer.impl.PvPTimer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
public class DeathbanHandler {

    private final long defaultDeathbanTime;

    public DeathbanHandler(HCF instance) {
        PluginManager pluginManager = instance.getServer().getPluginManager();
        pluginManager.registerEvents(new DeathbanListener(this), instance);

        HCFConfiguration configuration = HCF.getInstance().getConfiguration();
        this.defaultDeathbanTime = TimeUtil.parseTime(configuration.getDeathbanTime());

        Libraries.getInstance().getCommandFramework().registerClass(DeathbanCommand.class);
    }

    public void handleDeath(TeamProfile profile, TeamProfile killerProfile, Location location) {
        long time = HCF.getInstance().getDeathbanHandler().getDeathbanTime(profile.getUuid());

        EntityLightning lightning = new EntityLightning(((CraftWorld) location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ(), false, false);
        PacketPlayOutSpawnEntityWeather packetPlayOutSpawnEntityWeather = new PacketPlayOutSpawnEntityWeather(lightning);
        PacketPlayOutNamedSoundEffect packetPlayOutNamedSoundEffect = new PacketPlayOutNamedSoundEffect("ambient.weather.thunder", location.getX(), location.getY(), location.getZ(), 100F, 1F);

        List<Player> toSend = new ArrayList<>(Bukkit.getOnlinePlayers());
        toSend.removeIf(target -> !location.getWorld().getUID().equals(target.getWorld().getUID())
                || location.getWorld().getUID().equals(target.getWorld().getUID())
                && location.distanceSquared(target.getLocation()) > 62500);

        toSend.forEach(target -> {
            TeamProfile targetProfile = HCF.getInstance().getProfileHandler().getProfile(target);
            if (targetProfile.getSetting(Setting.LIGHTING)) {
                ((CraftPlayer) target).getHandle().playerConnection.sendPacket(packetPlayOutSpawnEntityWeather);
                ((CraftPlayer) target).getHandle().playerConnection.sendPacket(packetPlayOutNamedSoundEffect);
            }
        });

        profile.removeLastPearl();

        PlayerTeam killerTeam = null;
        if (killerProfile != null) {
            killerProfile.incrementStat(ProfileStatTypes.KILLS);

            killerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(killerProfile.getUuid());
            if (killerTeam != null) {
                killerTeam.addPoints(1);
            }

            BasicProfile basicKillerProfile = Basic.getInstance().getBasicAPI().getProfile(killerProfile.getUuid());
            if (basicKillerProfile != null) {
                basicKillerProfile.addXp(50);
            }

            if (!HCF.getInstance().getConfiguration().isKitmap()) {
                int deadBalance = profile.getBalance();
                if (deadBalance > 0) {
                    killerProfile.setBalance(killerProfile.getBalance() + deadBalance);
                    killerProfile.setSave(true);
                    profile.setBalance(0);
                    profile.setSave(true);
                    Player player = killerProfile.toPlayer();
                    if (player != null) {
                        player.sendMessage(CC.GOLD + "You have received " + CC.YELLOW + "$" + deadBalance);
                    }
                }
            }
        }

        UUID playerId = profile.getUuid();
        UUID targetId = killerProfile == null ? null : killerProfile.getUuid();

        profile.setTimeBetweenLives(System.currentTimeMillis() + 600000);
        profile.incrementStat(ProfileStatTypes.DEATHS);
        profile.addDeath(new Death(playerId, targetId, new SafeLocation(location), new Date(), profile.createDeathInventory()));

        if (time != -1) {
            profile.setDeathbanTime(System.currentTimeMillis() + time);
            profile.setSave(true);
        }

        if (!HCF.getInstance().getConfiguration().isKitmap()) {
            profile.addTimer(new PvPTimer());
            PlayerTeam team = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(playerId);
            if (team != null) {
                boolean wasRaidable = team.isRaidable();
                double lostDtr = this.getDTRLoss(location);
                team.removePoints(1);
                team.setDtr(team.getDtr() - lostDtr);
                team.setRegen(0, 45, 0);

                team.broadcast(CC.RED + UUIDFetcher.getCachedName(playerId) + "&e has died and cost your team " + lostDtr + " DTR &7(" + RoundingUtil.round(team.getDtr(), 2) + ")");

                if (!wasRaidable && team.isRaidable()) { //if they were not raidable & they became raidable
                    team.removePoints(50);
                    team.broadcast(CC.RED + "Due to your team going raidable, you has lost 50 points.");

                    if (killerTeam != null) {
                        killerTeam.addPoints(50);
                        killerTeam.broadcast(CC.RED + "Due to making " + team.getName() + " raidable, you have gained 50 points.");
                    }
                }

                team.setSave(true);
            }
        }
    }

    public String getDeathMessage(UUID playerId, UUID killerId, EntityDamageEvent.DamageCause death) {
        String deathMessage = "";

        Player killed = Bukkit.getPlayer(playerId);
        Player killer = null;
        TeamProfile killedProfile = HCF.getInstance().getProfileHandler().getProfile(playerId);
        TeamProfile killerProfile = null;
        int killedKills = killedProfile.getStat(ProfileStatTypes.KILLS);
        String playerName = killed.getName();

        if (killerId != null){
            killer = Bukkit.getPlayer(killerId);
            killerProfile = HCF.getInstance().getProfileHandler().getProfile(killerId);
        }

        switch (death) {
            case FALL: {
                deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + "&e fell from a high place.";
                break;
            }

            case POISON: {
                deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + "&e was poisoned.";
                break;
            }

            case VOID: {
                deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + "&e has fallen through the void.";
                break;
            }

            case ENTITY_EXPLOSION: {
                deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + "&e has exploded.";
                break;
            }

            case BLOCK_EXPLOSION: {
                deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + "&e was exploded by a TNT minecart.";
                break;
            }

            case LAVA:
            case FIRE:
            case FIRE_TICK: {
                deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + "&e was burnt to a crisp.";
                break;
            }

            case ENTITY_ATTACK: {
                if (killerId == null || killerProfile == null || killer == null){
                    deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + "&e was slain by a mob.";
                    break;
                }

                String killerName = killer.getName();
                int killerStats = killerProfile.getStat(ProfileStatTypes.KILLS);

                ItemStack weapon = killer.getItemInHand();

                if (weapon == null || weapon.getType() == Material.AIR){
                    deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + " &ewas slain by " + "&c" + killerName + "&4[" + killerStats + "]" + " &eusing " + "&c" + "their fists.";
                    break;
                }

                if (!weapon.hasItemMeta() || !weapon.getItemMeta().hasDisplayName()) {
                    String weaponName = WordUtils.capitalize(weapon.getType().toString().toLowerCase().replace("_", " "));
                    deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + "&e was slain by " + "&c" + killerName + "&4[" + killerStats + "]" + "&e using " + "&c" + weaponName;
                    break;
                }

                deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + "&e was slain by " + "&c" + killerName + "&4[" + killerStats + "]" + "&e using " + "&c" + weapon.getItemMeta().getDisplayName();
                break;
            }

            case PROJECTILE: {
                if (killerId == null || killerProfile == null || killer == null){
                    deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + "&e was shot by a mob.";
                    break;
                }

                double distance = killed.getLocation().distance(killer.getLocation());
                ItemStack weapon = killer.getItemInHand();
                String killerName = killer.getName();
                int killerStats = killerProfile.getStat(ProfileStatTypes.KILLS);

                if (!weapon.hasItemMeta() || !weapon.getItemMeta().hasDisplayName()) {
                    deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + "&e was shot by " + "&c" + killerName + "&4[" + killerStats + "]" + "&e from " + ChatColor.GOLD + Math.round(distance) + " blocks.";
                    break;
                }

                deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + "&e was shot by " + "&c" + killerName + "&4[" + killerStats + "]" + "&e from " + ChatColor.GOLD + Math.round(distance) + " blocks" + CC.GRAY + " using " + weapon.getItemMeta().getDisplayName();
                break;
            }

            case WITHER: {
                deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + " &ewithered away.";
                break;
            }

            case SUFFOCATION: {
                deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + " &esuffocated.";
                break;
            }

            case FALLING_BLOCK: {
                deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + " &ewas crushed by a falling block.";
                break;
            }

            default: {
                deathMessage = "&c" + playerName + "&4[" + killedKills + "]" + "&e died.";
                break;
            }
        }

        return CC.translate(deathMessage);
    }

    public double getDTRLoss(Location location) {
        if (HCF.getInstance().getConfiguration().isKitmap()) {
            return 0D;
        }

        if (location.getWorld().getEnvironment() == World.Environment.THE_END
                || location.getWorld().getEnvironment() == World.Environment.NETHER){
            return 0.5D;
        }

        Team team = HCF.getInstance().getTeamHandler().getTeamByLocation(location);

        if (team instanceof SystemTeam) {
            SystemTeam systemTeam = (SystemTeam) team;
            if (systemTeam.isGame()) {
                return 0.5D;
            }
        }

        return 1D;
    }

    public long getDeathbanTime(UUID uuid) {
        if (HCF.getInstance().getConfiguration().isKitmap()) {
            return -1;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.hasPlayedBefore()) {
            if (offlinePlayer.isOp()) {
                return -1;
            }
        }

        PermissionProfile profile;
        if (Bukkit.isPrimaryThread()) {
            profile = PermissionsPlugin.getInstance().getPermissionAPI().getProfile(uuid);
        } else {
            profile = PermissionsPlugin.getInstance().getPermissionAPI().getProfileOrLoad(uuid);
        }

        long currentTime = -1;

        if (profile != null) {
            Rank rank = profile.getCurrentRank();
            for (String permission : rank.getAllPermissions()) {
                if (permission.equals("hcf.deathban.bypass")) {
                    return -1;
                }
                if (permission.startsWith("teams.deathban.")) {
                    permission = permission.replace("teams.deathban.", "");
                    Long time = TimeUtil.parseTime(permission);

                    if (currentTime == -1) {
                        currentTime = time;
                    } else if (currentTime > time) {
                        currentTime = time;
                    }
                }
            }
        }

        return currentTime == -1 ? this.defaultDeathbanTime : currentTime;
    }
}
