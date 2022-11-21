package rip.alpha.hcf.pvpclass;

import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TaskUtil;
import net.mcscrims.libraries.util.listeners.ClassUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.pvpclass.impl.MinerClass;
import rip.alpha.hcf.pvpclass.kits.impl.ArcherKit;
import rip.alpha.hcf.pvpclass.kits.impl.BardKit;
import rip.alpha.hcf.pvpclass.kits.impl.DiamondKit;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.impl.SystemTeam;
import rip.foxtrot.spigot.fSpigot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PvPClassHandler {

    private final Map<Class<? extends PvPClass>, PvPClass> classMap;

    public PvPClassHandler(HCF instance) {
        this.classMap = new HashMap<>();

        try {
            for (Class<?> clazz : ClassUtils.getClassesInPackage(instance, "rip.alpha.hcf.pvpclass.impl")) {
                if (clazz.getSuperclass().equals(PvPClass.class)) {
                    PvPClass pvPClass = (PvPClass) clazz.newInstance();

                    if (HCF.getInstance().getConfiguration().isDisableKits()) {
                        if (pvPClass instanceof MinerClass) {
                            classMap.put(pvPClass.getClass(), pvPClass);
                        }
                    } else {
                        classMap.put(pvPClass.getClass(), pvPClass);
                    }


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (HCF.getInstance().getConfiguration().isKitmap()) { //only setup the kits when its kitmap coz its only used then
            new DiamondKit();
            new BardKit();
            new ArcherKit();
        }

        HCF.getInstance().getScheduledExecutorService().scheduleAtFixedRate(new PvPClassTask(this), 100L, 100L, TimeUnit.MILLISECONDS);

        fSpigot.INSTANCE.addMovementHandler(new PvPClassMovementHandler());

        PluginManager pluginManager = HCF.getInstance().getServer().getPluginManager();
        pluginManager.registerEvents(new PvPClassListener(this), instance);
    }

    public PvPClass getPvPClass(Class<? extends PvPClass> clazz) {
        if (clazz == null) {
            return null;
        }
        return this.classMap.get(clazz);
    }

    public Class<? extends PvPClass> getEquipPlayerClass(Player player) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (profile == null) {
            return null;
        }
        return profile.getEquipPvPClass();
    }

    public Set<PvPClass> getPvPClasses() {
        return new HashSet<>(this.classMap.values());
    }

    public void equipClass(Player player, TeamProfile profile, PvPClass pvPClass) {
        if (profile.getEquipTime() == -1) {
            long equipTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.getEquipWarmup(player, profile));
            profile.setEquipTime(equipTime);
        }

        if (!profile.readyToEquip()) {
            return;
        }
        pvPClass.onEquip(player);
        pvPClass.applyEffects(player);
        profile.setEquipPvPClass(pvPClass.getClass());
        profile.setEquipTime(-1);
        player.sendMessage(CC.translate("&eClass&7: &6") + pvPClass.getName() + CC.YELLOW + " has been equipped!");
    }

    public void unequipClass(Player player, TeamProfile profile, PvPClass pvPClass) {
        profile.setEquipTime(-1);
        pvPClass.handleRemove(player.getUniqueId());
        pvPClass.onUnEquip(player);
        TaskUtil.runSync(() -> pvPClass.removeEffects(player), HCF.getInstance());
        profile.setEquipPvPClass(null);
        player.sendMessage(CC.translate("&eClass&7: &6") + pvPClass.getName() + CC.YELLOW + " has been un-equipped!");
    }

    private int getEquipWarmup(Player player, TeamProfile profile) {
        if (player.getUniqueId().toString().equalsIgnoreCase("df927ed1-c660-41f3-b8ca-b5c04252f872")
                || player.getUniqueId().toString().equalsIgnoreCase("ee83c945-56f8-4259-963a-274e873c0b26")) {
            return 1;
        }

        Team team = profile.getLastClaimTeam();
        if (team instanceof SystemTeam) {
            SystemTeam systemTeam = (SystemTeam) team;
            if (systemTeam.isSafezone()) {
                return 5;
            }
        }

        return 20;
    }
}
