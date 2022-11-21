package rip.alpha.hcf.items;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;

@Getter
@RequiredArgsConstructor
public enum CItemRarity {
    COMMON(CC.GRAY + "Common" + CC.BLACK),
    UNCOMMON(CC.GREEN + "Un-Common" + CC.BLACK),
    RARE(CC.BLUE + "Rare" + CC.BLACK),
    EPIC(CC.DARK_PURPLE + "Epic" + CC.BLACK),
    LEGENDARY(CC.GOLD + "Legendary" + CC.BLACK);

    private final String displayName;
}
