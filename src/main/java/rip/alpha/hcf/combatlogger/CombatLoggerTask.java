package rip.alpha.hcf.combatlogger;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.TaskUtil;
import rip.alpha.hcf.HCF;

@RequiredArgsConstructor
public class CombatLoggerTask implements Runnable {

    private final CombatLoggerHandler handler;

    @Override
    public void run() {
        try {
            this.handler.getLoggerMap().forEach((uuid, combatLoggerVillager) -> {
                if (combatLoggerVillager.isExpired()) {
                    TaskUtil.runSync(() -> combatLoggerVillager.getBukkitEntity().remove(), HCF.getInstance());
                    this.handler.getLoggerMap().remove(uuid);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
