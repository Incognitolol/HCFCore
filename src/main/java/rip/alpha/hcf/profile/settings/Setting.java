package rip.alpha.hcf.profile.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Setting {

    LIGHTING(
            true,
            CC.YELLOW + "Death Lighting",
            Material.FLINT_AND_STEEL,
            "Toggle the death lighting effect when other players die",
            "Show death lighting",
            "Hide death lighting"
    ),
    FOUND_DIAMONDS(
            true,
            CC.AQUA + "Found Diamonds",
            Material.DIAMOND,
            "Toggle the found diamond message in chat",
            "Found diamond message shown in chat",
            "Found diamond message hidden from chat"
    ),
    KOTH_MESSAGES(
            true,
            CC.BLUE + "KoTH messages",
            Material.GOLD_HELMET,
            "Toggle the KoTH messages in chat",
            "KoTH messages shown in chat",
            "KoTH messages hidden from chat"
    ),
    CTP_MESSAGES(
            true,
            CC.D_PURPLE + "CTP messages",
            Material.WOOL,
            "Toggle the CTP messages in chat",
            "CTP messages shown in chat",
            "CTP messages hidden from chat"
    ),
    SCOREBOARD(
            true,
            CC.YELLOW + "Scoreboard",
            Material.ENCHANTED_BOOK,
            "Toggle whether you would like to see your scoreboard or not.",
            "Scoreboard can be seen",
            "Scoreboard is hidden"
    ),
    EXTRA_NAME_TAGS(
            false,
            CC.DARK_RED + "Extra Tags",
            Material.SKULL_ITEM,
            "Toggle extra tags on names",
            "Extra tags can be seen on names",
            "Extra tags are hidden from names"
    ),
    DEATH_MESSAGES(
            true,
            CC.DARK_RED + "Death messages",
            Material.DIAMOND_SWORD,
            "Toggle death messages in chat",
            "Death messages can be seen in chat",
            "Death messages are hidden from chat"
    ),
    LUNAR_TIMERS(
            true,
            CC.DARK_RED + "Lunar timers",
            Material.WATCH,
            "Toggle lunar timers",
            "Lunar timers can be seen.",
            "Lunar timers are hidden."
    ),
    LUNAR_BORDERS(
            true,
            CC.DARK_RED + "Lunar borders",
            Material.STAINED_GLASS,
            "Toggle lunar borders",
            "Lunar borders can be seen.",
            "Lunar borders are hidden."
    ),
    SCOREBOARD_LINES(
            false,
            CC.GREEN + "Scoreboard Lines",
            Material.BOOK,
            "Toggle scoreboard line",
            "Scoreboard can be seen with lines.",
            "Scoreboard lines are hidden."
    ),
    PUBLIC_CHATS(
            true,
            CC.GREEN + "Public chat",
            Material.PAPER,
            "Toggle public chat",
            "Public chat can be seen.",
            "Public chat is hidden."
    ),
    COBBLE(
            true,
            CC.D_GRAY + "Cobblestone",
            Material.COBBLESTONE,
            "Toggle cobblestone pickup",
            "Cobblestone can be picked up.",
            "Cobblestone is not picked up."
    ),
    MOB_DROPS(
            true,
            CC.D_PURPLE + "Mob Drops",
            Material.ROTTEN_FLESH,
            "Toggle mob drops pickup",
            "Mob drops can be picked up.",
            "Mob drops is not picked up."
    ),
    FACTION_PREFIX_CHAT(
            true,
            CC.D_PURPLE + "Faction prefix in chat",
            Material.EMPTY_MAP,
            "Toggle the ability to see faction prefixes in chat",
            "Faction prefixes can be seen.",
            "Faction prefixes are hidden."
    );

    private final Boolean defaultValue;
    private final String displayName;
    private final Material material;
    private final String description;
    private final String enabledMessage;
    private final String disabledMessage;

    private static final Map<String, Setting> NAME_TO_SETTING = new HashMap<>();
    private static final List<Setting> SORTED_LIST;

    static {
        for (Setting value : Setting.values()) {
            NAME_TO_SETTING.put(value.name().toLowerCase(), value);
        }

        SORTED_LIST = getSettings().stream().sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.name(), o2.name())).collect(Collectors.toList());
    }

    public static Setting getSettingByName(String name) {
        return NAME_TO_SETTING.get(name.toLowerCase());
    }

    public static Set<Setting> getSettings() {
        return new HashSet<>(NAME_TO_SETTING.values());
    }

    public static List<Setting> getSortedSettings() {
        return SORTED_LIST;
    }

}
