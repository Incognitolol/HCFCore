package rip.alpha.hcf;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.mcscrims.configuration.Configuration;

import java.io.File;

@Getter
@Setter
public class HCFConfiguration implements Configuration {

    @SerializedName(value = "teams.db")
    private String databaseName = "teams";
    @SerializedName(value = "teams.size")
    private int teamSize = 30;

    @SerializedName(value = "teams.bardClassLimit")
    private int bardClassLimit = 10;
    @SerializedName(value = "teams.archerClassLimit")
    private int archerClassLimit = 10;

    @SerializedName(value = "teams.inviteMax")
    private int maxInvites = 35;
    @SerializedName(value = "teams.enableMaxInvites")
    private boolean useMaxInvites = false;

    @SerializedName(value = "border.warzone.overworld")
    private int overworldWarzoneRadius = 1000;
    @SerializedName(value = "border.warzone.nether")
    private int netherWarzoneRadius = 300;

    @SerializedName(value = "border.buildRadiusOverworld")
    private int buildRadiusOverworld = 800;
    @SerializedName(value = "border.buildRadiusNether")
    private int buildRadiusNether = 300;

    @SerializedName(value = "border.claim")
    private int claimRadius = 1000;
    @SerializedName(value = "border.worldBorder")
    private int worldBorder = 3000;

    @SerializedName(value = "timers.combat")
    private int combatTagTimer = 30;
    @SerializedName(value = "timers.enderpearl")
    private int enderpearlTimer = 16;
    @SerializedName(value = "timers.logout")
    private int logoutTimer = 30;
    @SerializedName(value = "timers.stuck")
    private int stuckTimer = 5;
    @SerializedName(value = "timers.gopple")
    private int goppleTimer = 60 * 60 * 8; //8 hours
    @SerializedName(value = "timers.crapple")
    private int crappleTimer = 10;

    @SerializedName(value = "crappleEnabled")
    private boolean crappleEnabled = true;
    @SerializedName(value = "goppleEnabled")
    private boolean goppleEnabled = true;

    @SerializedName(value = "bard.radius")
    private int bardRadius = 35;
    @SerializedName(value = "bard.swordNerf")
    private int bardSwordNerf = 50;
    @SerializedName(value = "bard.maxEnergy")
    private double bardMaxEnergy = 60D;

    @SerializedName(value = "archer.tagTime")
    private int archerTagTime = 10;
    @SerializedName(value = "archer.tagPercentage")
    private int archerTagPercentage = 10;

    @SerializedName(value = "mobstack.limit")
    private int mobStackLimit = 150;
    @SerializedName(value = "mobstack.radius")
    private int mobStackRadius = 35;

    @SerializedName(value = "deathban")
    private String deathbanTime = "1h5m5s";
    @SerializedName(value = "kitmap")
    private boolean kitmap = false;
    @SerializedName(value = "combatLogger.time")
    private int combatLoggerTime = 30;

    @SerializedName(value = "allowUnder15MansInKoths")
    private boolean allowUnder15MansInKoths = true;

    @SerializedName(value = "mapKit.protection")
    private int mapKitProtection = 1;
    @SerializedName(value = "mapKit.sharpness")
    private int mapKitSharpness = 1;
    @SerializedName(value = "mapKit.power")
    private int mapKitPower = 3;

    @SerializedName(value = "disableKits")
    private boolean disableKits = false;

    @SerializedName(value = "scoreboard.title")
    private String scoreboardTitle = "&6HCF";
    @SerializedName(value = "scoreboard.kda")
    private boolean kda = false;

    @SerializedName(value = "portals.x")
    private int endPortalX = 1000;
    @SerializedName(value = "portals.z")
    private int endPortalZ = 1000;

    @SerializedName(value = "crafting.gapples")
    private boolean craftGapples = true;
    @SerializedName(value = "crafting.crapples")
    private boolean craftCrapples = true;

    @SerializedName(value = "stackExcludedMobs")
    private String[] stackExcludedMobs =
            {
                    "ENDERMAN", "HORSE", "VILLAGER"
            };

    @SerializedName(value = "blacklistedTeamNames")
    private String[] blacklistedTeamNames =
            {
                    "nigger", "niggers", "n1gger", "n1ggers", "niggerz", "n1ggerz", "n1gg3r", "n1gg3rs", "n1gg3rz", "nigg3r", "nigg3rs", "nigg3rz",
                    "niggerr", "niggerss", "niggerzz", "n1ggerss", "n1ggger", "niggger", "nigggerss", "nigggerzz", "n1gggers", "niiger", "faggot",
                    "fagg0t", "faggots", "fagg0ts", "faggotz", "fagg0tz", "nigggger", "niggggger", "n1gggger", "n1gggerr", "n1gggerrss", "FuckBlacks",
                    "HateNiggers", "ihateniggers", "ihateniggerz", "ihateblacks", "ihateblackz", "hateblacks", "hateniggerz", "haten1ggerz",
                    "hateblackppl", "ihateblackppl"
            };

    @SerializedName(value = "dtr")
    private double[] maxDTR =
            {
                    1.01, 2.01, 3.25, 3.75, 4.50, // 0 - 4
                    5.25, 5.50, 5.50, 5.50, 6.80, // 5 - 9
                    6.81, 6.90, 6.91, 7.94, 8.98, // 9 - 14
                    9.81, 9.99, 10.01, 10.5, 11.0 // 15 - 20
            };

    @SerializedName(value = "mapNumber")
    private String mapNumber = "2";

    @Override
    public File getFileLocation() {
        return new File(HCF.getInstance().getDataFolder(), "config.json");
    }
}
