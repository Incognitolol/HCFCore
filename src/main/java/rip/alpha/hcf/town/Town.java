package rip.alpha.hcf.town;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Town {
    private UUID townId;
    private UUID owningTeamId;
    private String refillableContents;

    public Town(UUID townId, UUID owningTeamId){
        this.townId = townId;
        this.owningTeamId = owningTeamId;
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("townId", this.townId.toString());
        document.put("owningTeamId", this.owningTeamId.toString());
        document.put("refillableContents", this.refillableContents);
        return document;
    }

    public void fromDocument(Document document){
        this.townId = UUID.fromString(document.getString("townId"));
        this.owningTeamId = UUID.fromString(document.getString("owningTeamId"));
        this.refillableContents = document.getString("refillableContents");
    }

    public void refillChests(){

    }

}
