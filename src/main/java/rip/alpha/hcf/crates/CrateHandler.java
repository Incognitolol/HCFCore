package rip.alpha.hcf.crates;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import net.mcscrims.command.CommandFramework;
import net.mcscrims.libraries.Libraries;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.cuboid.SafeLocation;
import net.mcscrims.libraries.util.gson.GsonUtil;
import net.mcscrims.monitor.util.FileConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.crates.command.CrateCommand;
import rip.alpha.hcf.crates.impl.CommandCrateItem;
import rip.alpha.hcf.crates.impl.ItemstackCrateItem;
import rip.alpha.hcf.crates.impl.KOTHCrateItem;
import rip.alpha.hcf.crates.param.CrateParam;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Getter
public class CrateHandler {

    private final Map<String, Crate> crateMap;
    private final Int2ObjectMap<Class<? extends CrateItem>> idToCrateItemClazz;

    private final FileConfig cratesConfig;

    private final Random random = new Random();

    public CrateHandler(HCF instance) {
        this.cratesConfig = new FileConfig(instance, "crates.yml");

        this.crateMap = Maps.newHashMap();
        this.idToCrateItemClazz = new Int2ObjectOpenHashMap<>();

        this.idToCrateItemClazz.put(0, CommandCrateItem.class);
        this.idToCrateItemClazz.put(1, ItemstackCrateItem.class);
        this.idToCrateItemClazz.put(2, KOTHCrateItem.class);

        PluginManager pluginManager = instance.getServer().getPluginManager();
        pluginManager.registerEvents(new CrateListener(this), instance);

        CommandFramework commandFramework = Libraries.getInstance().getCommandFramework();
        commandFramework.registerParameter(new CrateParam(this), Crate.class);
        commandFramework.registerClass(CrateCommand.class);

        this.loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = this.cratesConfig.getConfig();

        if (config == null) {
            return;
        }
        if (!config.contains("crates")) {
            return;
        }
        if (config.getConfigurationSection("crates").getKeys(false).isEmpty()) {
            return;
        }

        ConfigurationSection rootSection = config.getConfigurationSection("crates");
        for (String name : rootSection.getKeys(false)) {
            ConfigurationSection currentSection = rootSection.getConfigurationSection(name);
            UUID uuid = UUID.fromString(currentSection.getString("id"));
            Crate crate = new Crate(uuid, name);

            ItemStack keyItem = GsonUtil.GSON.fromJson(currentSection.getString("key"), ItemStack.class);
            if (keyItem != null) {
                crate.setKey(keyItem);
            }

            String locationKey = currentSection.getString("location");
            if (locationKey != null) {
                JsonObject locationObject = HCF.PARSER.parse(locationKey).getAsJsonObject();
                if (locationObject != null) {
                    crate.setSafeLocation(SafeLocation.fromJson(locationObject));
                }
            }

            int delay = currentSection.getInt("delay", 0);
            crate.setOpenDelay(delay);

            ConfigurationSection itemsSection = currentSection.getConfigurationSection("items");
            if (itemsSection != null) {
                for (String key : itemsSection.getKeys(false)) {
                    try {
                        ConfigurationSection section = itemsSection.getConfigurationSection(key);
                        int typeId = section.getInt("typeId");
                        Class<? extends CrateItem> crateItemClazz = this.getCrateItemClassById(typeId);
                        ItemStack displayItem = (GsonUtil.GSON.fromJson(section.getString("item"), ItemStack.class));
                        int weight = section.getInt("weight");
                        int id = section.getInt("id");

                        CrateItem crateItem = null;
                        if (crateItemClazz.equals(CommandCrateItem.class)) {
                            String command = config.getString("command");
                            if (command != null) {
                                crateItem = crateItemClazz
                                        .getConstructor(ItemStack.class, int.class, int.class, int.class, String.class)
                                        .newInstance(displayItem, weight, id, typeId, command);
                            }
                        } else if (crateItemClazz.equals(KOTHCrateItem.class) || crateItemClazz.equals(ItemstackCrateItem.class)) {
                            crateItem = crateItemClazz.getConstructor(ItemStack.class, int.class, int.class, int.class).newInstance(displayItem, weight, id, typeId);
                        }

                        if (crateItem != null) {
                            crate.getItems().put(id, crateItem);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            this.cacheCrate(crate);
        }
    }

    public void createCrate(Crate crate) {
        FileConfiguration cratesConfig = this.cratesConfig.getConfig();
        String prefix = "crates." + crate.getName();
        ConfigurationSection section = cratesConfig.createSection(prefix);
        section.set("id", crate.getUuid().toString());
        this.cratesConfig.save();
        this.cacheCrate(crate);
    }

    public void cacheCrate(Crate crate) {
        this.crateMap.put(crate.getName().toLowerCase(), crate);
    }

    public void uncacheCrate(Crate crate) {
        this.crateMap.remove(crate.getName().toLowerCase());
    }

    public Crate getCrateByName(String name) {
        return this.crateMap.get(name.toLowerCase());
    }

    public Crate getCrateByLocation(SafeLocation location) {
        for (Crate crate : this.getCrateMap().values()) {
            SafeLocation crateLocation = crate.getSafeLocation();
            if (crateLocation == null) {
                continue;
            }
            if (crateLocation.equals(location)) {
                return crate;
            }
        }

        return null;
    }

    public boolean isKothItem(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (!itemStack.hasItemMeta()) {
            return false;
        }
        if (!itemStack.getItemMeta().hasLore()) {
            return false;
        }

        for (String lore : itemStack.getItemMeta().getLore()) {
            if (lore.equals(CC.GRAY + "Koth Item.")) {
                return true;
            }
        }

        return false;
    }

    public Class<? extends CrateItem> getCrateItemClassById(int id) {
        return this.idToCrateItemClazz.get(id);
    }
}
