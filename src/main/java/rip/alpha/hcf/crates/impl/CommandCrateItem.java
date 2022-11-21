package rip.alpha.hcf.crates.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.mcscrims.libraries.util.gson.GsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.crates.CrateItem;

@Getter
public class CommandCrateItem extends CrateItem {

    private final String command;

    public CommandCrateItem(ItemStack displayItem, int weight, int id, int typeId, String command) {
        super(displayItem, weight, id, typeId);
        this.command = command;
    }

    @Override
    public void giveItem(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.command.replace("{player}", player.getName()));
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("displayItem", GsonUtil.GSON.toJson(super.getDisplayItem()));
        jsonObject.addProperty("weight", this.getWeight());
        jsonObject.addProperty("id", this.getId());
        jsonObject.addProperty("typeId", this.getTypeId());
        jsonObject.addProperty("command", this.command);
        return jsonObject;
    }

    @Override
    public void saveCrateItem(String name, int typeId) {
        FileConfiguration crateConfig = HCF.getInstance().getCrateHandler().getCratesConfig().getConfig();
        String prefix = "crates." + name + ".items." + this.getId();
        crateConfig.set(prefix + ".item", GsonUtil.GSON.toJson(this.getDisplayItem()));
        crateConfig.set(prefix + ".weight", this.getWeight());
        crateConfig.set(prefix + ".id", this.getId());
        crateConfig.set(prefix + ".typeId", typeId);
        crateConfig.set(prefix + ".command", this.command);
        HCF.getInstance().getCrateHandler().getCratesConfig().save();
    }
}
