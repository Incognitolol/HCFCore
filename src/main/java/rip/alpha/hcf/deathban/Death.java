package rip.alpha.hcf.deathban;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.mcscrims.libraries.util.cuboid.SafeLocation;

import java.util.Date;
import java.util.UUID;

@Getter
public class Death {
    private final UUID dead;
    private UUID killer;
    private final SafeLocation location;
    private final Date date;
    private final String deathInventory;

    public Death(UUID dead, UUID killer, SafeLocation location, Date date, String deathInventory) {
        this.dead = dead;
        this.killer = killer;
        this.location = location;
        this.date = date;
        this.deathInventory = deathInventory;
    }

    public Death(JsonObject jsonObject) {
        this.dead = UUID.fromString(jsonObject.get("dead").getAsString());
        if (jsonObject.has("killer")) {
            this.killer = UUID.fromString(jsonObject.get("killer").getAsString());
        }
        this.location = SafeLocation.fromJson(jsonObject.getAsJsonObject("location"));
        this.date = new Date(jsonObject.get("date").getAsLong());
        this.deathInventory = jsonObject.get("deathInventory").getAsString();
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("dead", this.dead.toString());
        if (this.killer != null) {
            jsonObject.addProperty("killer", this.killer.toString());
        }
        jsonObject.add("location", this.location.toJson());
        jsonObject.addProperty("date", this.date.getTime());
        jsonObject.addProperty("deathInventory", this.deathInventory);
        return jsonObject;
    }
}
