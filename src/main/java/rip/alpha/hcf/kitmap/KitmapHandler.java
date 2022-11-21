package rip.alpha.hcf.kitmap;

import rip.alpha.hcf.HCF;

public class KitmapHandler {

    public KitmapHandler(HCF instance) {
        instance.getServer().getPluginManager().registerEvents(new KitmapListener(), instance);
    }
}
