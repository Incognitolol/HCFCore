package rip.alpha.hcf.end;

import lombok.Getter;
import lombok.Setter;
import net.mcscrims.libraries.Libraries;
import net.mcscrims.libraries.util.gson.GsonUtil;
import net.mcscrims.monitor.util.FileConfig;
import org.bukkit.Location;
import rip.alpha.hcf.HCF;

@Getter
@Setter
public class EndHandler {
    private final FileConfig endConfig;
    private Location endExitOverworld;
    private Location endExit;

    public EndHandler(HCF instance) {
        this.endConfig = new FileConfig(instance, "end.yml");

        Libraries.getInstance().getCommandFramework().registerClass(EndCommands.class);

        try {
            this.endExitOverworld = GsonUtil.GSON.fromJson(this.endConfig.getConfig().getString("end.endExitOverworld.location"), Location.class);
            this.endExit = GsonUtil.GSON.fromJson(this.endConfig.getConfig().getString("end.endPortal.location"), Location.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}