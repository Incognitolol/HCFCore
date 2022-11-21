package rip.alpha.hcf.mobstack;

import lombok.Getter;
import net.mcscrims.libraries.Libraries;
import net.mcscrims.libraries.util.CC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.PluginManager;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.HCFConfiguration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class MobstackHandler {

    private final int mobStackRadius;
    private final int maxStack;
    private final Set<Short> excludedMobs;
    private final String startFormat;

    public MobstackHandler(HCF instance) {
        HCFConfiguration configuration = instance.getConfiguration();
        this.maxStack = configuration.getMobStackLimit();
        this.mobStackRadius = configuration.getMobStackRadius();
        this.excludedMobs =
                Arrays.stream(configuration.getStackExcludedMobs())
                        .map(mobName -> EntityType.valueOf(mobName).getTypeId()).collect(Collectors.toSet());

        this.startFormat = CC.YELLOW + "x";

        PluginManager pluginManager = instance.getServer().getPluginManager();
        pluginManager.registerEvents(new MobstackListener(this), instance);

        Libraries.getInstance().getCommandFramework().registerClass(MobstackCommand.class);
    }

    public boolean isMobStackEntity(LivingEntity livingEntity) {
        return livingEntity.isCustomNameVisible() &&
                livingEntity.getCustomName() != null && livingEntity.getCustomName().contains(this.startFormat);
    }

    public int getMobStackCount(LivingEntity creature) {
        if (!this.isMobStackEntity(creature)) {
            return 0;
        }
        return Integer.parseInt(creature.getCustomName().replace(this.startFormat, ""));
    }

    public <T extends LivingEntity> T getAvailableStackEntity(T entity) {
        for (T otherEntity : this.getNearbyEntitiesByTypeAroundEntity(entity)) {
            if (this.isMobStackEntity(otherEntity)) {
                int currentMobStack = this.getMobStackCount(otherEntity);
                if (currentMobStack >= this.maxStack) {
                    continue;
                }
                return otherEntity;
            }
        }
        return null;
    }

    public <T extends LivingEntity> Set<T> getNearbyEntitiesByTypeAroundEntity(T entity) {
        Set<T> set = new HashSet<>();
        final short typeID = entity.getType().getTypeId();
        if (excludedMobs.contains(typeID)) {
            return set;
        }

        for (Entity otherEntity : entity.getNearbyEntities(this.mobStackRadius, this.mobStackRadius, this.mobStackRadius)) {
            final short otherTypeID = otherEntity.getType().getTypeId();
            if (otherTypeID != typeID) {
                continue;
            }
            set.add((T) otherEntity);
        }

        return set;
    }
}
