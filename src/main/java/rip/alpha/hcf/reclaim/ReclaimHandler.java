package rip.alpha.hcf.reclaim;

import net.mcscrims.libraries.Libraries;
import net.mcscrims.libraries.util.configuration.FileConfig;
import rip.alpha.hcf.HCF;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReclaimHandler {

    private Map<String, ReclaimEntry> reclaimEntryMap;

    public ReclaimHandler(HCF instance){
        this.reclaimEntryMap = new HashMap<>();

        Libraries.getInstance().getCommandFramework().registerClass(ReclaimCommand.class);

        FileConfig fileConfig = new FileConfig(instance, "reclaim");
        Set<String> keys = fileConfig.getConfiguration().getKeys(false);
        for (String key : keys){
            Set<String> commands = new HashSet<>(fileConfig.getStringList(key));
            ReclaimEntry reclaimEntry = new ReclaimEntry(commands);
            this.reclaimEntryMap.put(key.toLowerCase(), reclaimEntry);
        }
    }

    public ReclaimEntry getReclaimEntry(String rank){
        return this.reclaimEntryMap.get(rank.toLowerCase());
    }
}
