package rip.alpha.hcf.team;

import lombok.Getter;
import lombok.Setter;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;
import net.mcscrims.libraries.util.gson.GsonUtil;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.HCF;

import java.util.UUID;

@Getter
@Setter
public abstract class Team {

    private final UUID id;

    private String name;
    private String color;
    private ACuboid claim;
    private Location home;

    private boolean save;

    public Team(UUID id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.claim = null;
        this.home = null;
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("id", this.id.toString());
        document.put("name", this.name);
        document.put("color", this.color);

        if (this.claim != null) {
            document.put("claim", GsonUtil.GSON.toJson(this.claim));
            if (this.home != null) {
                document.put("home", GsonUtil.GSON.toJson(this.home));
            }
        }

        return document;
    }

    public void fromDocument(Document document) {
        this.name = document.getString("name");
        this.color = document.getString("color");

        if (document.containsKey("claim")) {
            this.setClaim(GsonUtil.GSON.fromJson(document.getString("claim"), ACuboid.class));
        }

        if (this.claim != null) {
            if (document.containsKey("home")) {
                this.home = GsonUtil.GSON.fromJson(document.getString("home"), Location.class);
            }
        }
    }

    public abstract void sendTeamInfo(CommandSender sender);

    public abstract String getDisplayName(CommandSender sender);

    public void setClaim(ACuboid claim) {
        if (claim == null) {
            this.home = null;
            if (this.claim != null) {
                HCF.getInstance().getClaimGrid().setClaim(this.claim, null);
            }
        } else {
            if (this.claim != null) {
                HCF.getInstance().getClaimGrid().setClaim(this.claim, null);
            }
            HCF.getInstance().getClaimGrid().setClaim(claim, this.id);
        }

        this.claim = claim;
    }
}
