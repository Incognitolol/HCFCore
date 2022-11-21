package rip.alpha.hcf.profile.statistics;

import lombok.Getter;
import org.bukkit.Material;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum ProfileStatTypes {
    KILLS("Kills: "),
    DEATHS("Deaths: "),
    DIAMOND_ORE("Diamond Ores: ", Material.DIAMOND_ORE),
    EMERALD_ORE("Emerald Ores: ", Material.EMERALD_ORE),
    REDSTONE_ORE("Redstone Ores: ", Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE),
    LAPIS_ORE("Lapis Ores: ", Material.LAPIS_ORE),
    GOLD_ORE("Gold Ores: ", Material.GOLD_ORE),
    IRON_ORE("Iron Ores: ", Material.IRON_ORE),
    COAL_ORE("Coal Ores: ", Material.COAL_ORE),
    QUARTZ_ORE("Quartz Ores: ", Material.QUARTZ_ORE);

    private final String formattedName;
    private final Material[] materials;

    ProfileStatTypes(String formattedName, Material... materials) {
        this.formattedName = formattedName;
        this.materials = materials;
    }

    private static final Map<String, ProfileStatTypes> NAME_TO_VALUE = new HashMap<>();
    private static final Map<Material, ProfileStatTypes> MATERIAL_TO_VALUE = new HashMap<>();

    static {
        for (ProfileStatTypes value : ProfileStatTypes.values()) {
            NAME_TO_VALUE.put(value.name().toLowerCase(), value);
            if (value.getMaterials() != null) {
                for (Material material : value.getMaterials()) {
                    MATERIAL_TO_VALUE.put(material, value);
                }
            }
        }
    }

    public static ProfileStatTypes getByName(String name) {
        return NAME_TO_VALUE.getOrDefault(name.toLowerCase(), null);
    }

    public static ProfileStatTypes getByMaterial(Material material) {
        return MATERIAL_TO_VALUE.get(material);
    }

    public static Collection<ProfileStatTypes> getValues() {
        return Collections.unmodifiableCollection(NAME_TO_VALUE.values());
    }
}
