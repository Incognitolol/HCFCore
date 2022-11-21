package rip.alpha.hcf.pvpclass.kits;

import rip.alpha.hcf.HCF;
import rip.alpha.hcf.pvpclass.kits.impl.ArcherKit;
import rip.alpha.hcf.pvpclass.kits.impl.BardKit;
import rip.alpha.hcf.pvpclass.kits.impl.DiamondKit;

import java.util.HashMap;
import java.util.Map;

public class KitHandler {

    private final Map<String, Kit> kitMap;

    public KitHandler() {
        this.kitMap = new HashMap<>();

        if (HCF.getInstance().getConfiguration().isKitmap()) {
            this.registerKit(new ArcherKit());
            this.registerKit(new BardKit());
            this.registerKit(new DiamondKit());
        }
    }

    public Kit getKit(String name) {
        return this.kitMap.get(name.toLowerCase());
    }

    private void registerKit(Kit kit) {
        this.kitMap.put(kit.getName().toLowerCase(), kit);
    }
}
