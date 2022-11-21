package rip.alpha.hcf.crates;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.gson.GsonUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;

@Data
public abstract class CrateItem {

    private ItemStack displayItem;
    private int weight;
    private int id;
    private int typeId;

    public CrateItem(ItemStack displayItem, int weight, int id, int typeId) {
        this.displayItem = displayItem;
        this.weight = weight;
        this.id = id;
        this.typeId = typeId;
    }

    public void giveItem(Player player) {
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), this.getDisplayItem());
            player.sendMessage(CC.RED + "Your inventory is full, your item has been dropped on the floor.");
            return;
        }

        player.getInventory().addItem(this.getDisplayItem());
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("displayItem", GsonUtil.GSON.toJson(this.displayItem));
        jsonObject.addProperty("weight", this.weight);
        jsonObject.addProperty("id", this.id);
        jsonObject.addProperty("typeId", this.typeId);
        return jsonObject;
    }

    public void saveCrateItem(String name, int typeId) {
        FileConfiguration crateConfig = HCF.getInstance().getCrateHandler().getCratesConfig().getConfig();
        String prefix = "crates." + name + ".items." + id;
        crateConfig.set(prefix + ".item", GsonUtil.GSON.toJson(displayItem));
        crateConfig.set(prefix + ".weight", weight);
        crateConfig.set(prefix + ".id", id);
        crateConfig.set(prefix + ".typeId", typeId);
        HCF.getInstance().getCrateHandler().getCratesConfig().save();
    }
}
