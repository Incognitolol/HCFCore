package rip.alpha.hcf.crowbar;

import net.mcscrims.libraries.Libraries;
import org.bukkit.plugin.PluginManager;
import rip.alpha.hcf.HCF;

public class CrowbarHandler {

    private final int defaultSpawnerUses = 1;
    private final int defaultEndFrameUses = 6;

    public CrowbarHandler(HCF instance) {
        PluginManager pluginManager = instance.getServer().getPluginManager();
        pluginManager.registerEvents(new CrowbarListener(), instance);

        Libraries.getInstance().getCommandFramework().registerClass(CrowbarCommand.class);
    }

    public CrowbarItem createDefaultCrowbarItem() {
        return new CrowbarItem(this.defaultSpawnerUses, this.defaultEndFrameUses);
    }
}
