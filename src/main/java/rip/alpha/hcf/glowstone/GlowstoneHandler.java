package rip.alpha.hcf.glowstone;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.mcscrims.libraries.Libraries;
import net.mcscrims.libraries.mongo.EasyMongoCollection;
import net.mcscrims.libraries.util.TaskUtil;
import net.mcscrims.libraries.util.cuboid.SafeBlock;
import net.mcscrims.libraries.util.cuboid.SafeLocation;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.glowstone.command.GlowstoneCommand;
import rip.alpha.hcf.glowstone.task.GlowstoneResetTask;
import rip.alpha.hcf.glowstone.task.GlowstoneRestoreTask;
import rip.alpha.hcf.team.TeamHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
public class GlowstoneHandler {

    private final EasyMongoCollection glowstoneCollection;
    private final Map<SafeLocation, SafeBlock> blockMap;
    private UUID id;

    public GlowstoneHandler(HCF instance, TeamHandler teamHandler) {
        this.blockMap = Maps.newHashMap();
        this.glowstoneCollection = instance.getMongoHelper().fetchMongoCollection("glowstone");
        TaskUtil.runAsync(() -> this.glowstoneCollection.fetchAllDocuments().forEach(this::loadFromDocument), HCF.getInstance());
        Libraries.getInstance().getCommandFramework().registerClass(GlowstoneCommand.class);
        instance.getServer().getPluginManager().registerEvents(new GlowstoneListener(teamHandler), instance);
        HCF.getInstance().getScheduledExecutorService().scheduleAtFixedRate(new GlowstoneResetTask(), 30, 30, TimeUnit.MINUTES);
    }

    public void scanBlocks() {
        this.blockMap.clear();

        ACuboid cuboid = HCF.getInstance().getTeamHandler().getSystemTeamByName("Glowstone").getClaim();
        if (cuboid == null) {
            return;
        }

        String cuboidWorldName = cuboid.getWorldName();
        World cuboidWorld = Bukkit.getWorld(cuboidWorldName);

        for (int x = cuboid.getMinX(); x < cuboid.getMaxX(); x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = cuboid.getMinZ(); z < cuboid.getMaxZ(); z++) {
                    Block worldBlock = new Location(cuboidWorld, x, y, z).getBlock();

                    if (worldBlock.getType() == Material.GLOWSTONE) {
                        this.blockMap.put(new SafeLocation(x, y, z, cuboidWorldName), new SafeBlock(worldBlock.getTypeId(), worldBlock.getData(), x, y, z, cuboidWorldName));
                    }
                }
            }
        }

        if (!this.blockMap.isEmpty()) {
            if (this.id == null) {
                this.id = UUID.randomUUID();
            }

            this.saveCache();
        }
    }

    public void resetBlocks() {
        GlowstoneRestoreTask.start(this.blockMap);
    }

    public void saveCache() {
        Document document = new Document();
        document.put("id", this.id.toString());

        if (!this.blockMap.isEmpty()) {
            Document blockDocument = new Document();
            for (Map.Entry<SafeLocation, SafeBlock> entry : this.blockMap.entrySet()) {
                blockDocument.put(entry.getKey().toJson().toString(), entry.getValue().toJson().toString());
            }

            document.put("blockMap", blockDocument);
        }

        this.glowstoneCollection.insertAsync("id", this.id.toString(), document);
    }

    public void loadFromDocument(Document document) {
        if (document.isEmpty()) {
            return;
        }

        this.id = UUID.fromString(document.getString("id"));

        Document blockMap = (Document) document.get("blockMap");
        blockMap.keySet().forEach(key -> {
            JsonObject keyObject = HCF.PARSER.parse(key).getAsJsonObject();
            JsonObject valueObject = HCF.PARSER.parse((String) blockMap.get(key)).getAsJsonObject();

            SafeLocation safeLocation = SafeLocation.fromJson(keyObject);
            SafeBlock safeBlock = SafeBlock.fromJson(valueObject);

            this.blockMap.put(safeLocation, safeBlock);
        });
    }

}
