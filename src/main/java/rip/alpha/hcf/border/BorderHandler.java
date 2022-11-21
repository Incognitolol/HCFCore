package rip.alpha.hcf.border;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.HCFConfiguration;
import rip.alpha.hcf.border.listener.BorderListener;
import rip.alpha.hcf.border.listener.WarzoneListener;

@Getter
@Setter
public class BorderHandler {

    private final int claimRadius;
    private final int worldBorder;

    private final int overWorldBuildRadius;
    private final int netherBuildRadius;

    private final int overWorldWarzoneRadius;
    private final int netherWarzoneRadius;

    public BorderHandler(HCF instance) {
        HCFConfiguration configuration = instance.getConfiguration();
        this.overWorldWarzoneRadius = configuration.getOverworldWarzoneRadius();
        this.netherWarzoneRadius = configuration.getNetherWarzoneRadius();
        this.overWorldBuildRadius = configuration.getBuildRadiusOverworld();
        this.netherBuildRadius = configuration.getBuildRadiusNether();
        this.claimRadius = configuration.getClaimRadius();
        this.worldBorder = configuration.getWorldBorder();

        PluginManager pluginManager = instance.getServer().getPluginManager();
        pluginManager.registerEvents(new WarzoneListener(this), instance);
        pluginManager.registerEvents(new BorderListener(this), instance);
    }

    public boolean isBypass(Player player) {
        return player.getGameMode() == GameMode.CREATIVE && player.hasPermission("hcf.bypass");
    }

    public boolean inWarzone(Location location) {
        return this.inWarzone(location.getBlockX(), location.getBlockZ(), location.getWorld().getEnvironment() == World.Environment.NETHER);
    }

    public boolean inBuildRadius(Location location) {
        return this.inBuildRadius(location.getBlockX(), location.getBlockZ(), location.getWorld().getEnvironment() == World.Environment.NETHER);
    }

    public boolean inWarzone(int x, int z, boolean nether) {
        if (nether) {
            return Math.abs(x) < this.netherWarzoneRadius && Math.abs(z) < this.netherWarzoneRadius;
        } else {
            return Math.abs(x) < this.overWorldWarzoneRadius && Math.abs(z) < this.overWorldWarzoneRadius;
        }
    }

    public boolean inBuildRadius(int x, int z, boolean nether) {
        if (nether) {
            return Math.abs(x) >= this.netherBuildRadius || Math.abs(z) >= this.netherBuildRadius;
        } else {
            return Math.abs(x) >= this.overWorldBuildRadius || Math.abs(z) >= this.overWorldBuildRadius;
        }
    }

    public int getWarzoneRadius(boolean nether) {
        if (nether) {
            return this.netherWarzoneRadius;
        } else {
            return this.overWorldWarzoneRadius;
        }
    }

    public boolean inWarzone(int x, int z) {
        return this.inWarzone(x, z, false);
    }


    public boolean inClaimRadius(int x, int z) {
        return Math.abs(x) >= this.claimRadius || Math.abs(z) >= this.claimRadius;
    }

    public boolean inClaimRadius(Location location) {
        return this.inClaimRadius(location.getBlockX(), location.getBlockZ());
    }

    public boolean inBorder(int x, int z) {
        return Math.abs(x) < this.worldBorder && Math.abs(z) < this.worldBorder;
    }

    public boolean inBorder(Location location) {
        return this.inBorder(location.getBlockX(), location.getBlockZ());
    }
}
