package rip.alpha.hcf.crowbar;

import lombok.AllArgsConstructor;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.ItemBuilder;
import net.mcscrims.libraries.util.items.LoreUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@AllArgsConstructor
public class CrowbarItem {

    private int spawnerUses;
    private int endFrameUses;

    public boolean hasSpawnerUse() {
        return this.spawnerUses > 0;
    }

    public boolean hasEndFrameUse() {
        return this.endFrameUses > 0;
    }

    public void decrementSpawnerUse() {
        this.spawnerUses--;
    }

    public void decrementEndFrameUse() {
        this.endFrameUses--;
    }

    public ItemStack toItemStack() {
        return new ItemBuilder(Material.GOLD_HOE)
                .name(CC.GOLD + "Crowbar")
                .lore(CC.translateLines(Arrays.asList("&6Spawner Uses: &f" + this.spawnerUses, "&6End Frame Uses: &f" + this.endFrameUses, "", "&eRight click to use crowbar")))
                .build();
    }

    private static final String SPLIT = ": " + CC.WHITE;

    public static CrowbarItem fromItemStack(ItemStack itemStack) {
        String line = LoreUtil.getFirstLoreLine(itemStack);
        if (line == null) {
            return null;
        }
        String[] split = line.split(SPLIT);
        if (split.length <= 1) {
            return null;
        }
        int spawnerUses = Integer.parseInt(CC.strip(split[1]));
        line = LoreUtil.getLoreLine(itemStack, 1);
        if (line == null) {
            return null;
        }
        split = line.split(SPLIT);
        int endFrameUses = Integer.parseInt(CC.strip(split[1]));
        return new CrowbarItem(spawnerUses, endFrameUses);
    }
}
