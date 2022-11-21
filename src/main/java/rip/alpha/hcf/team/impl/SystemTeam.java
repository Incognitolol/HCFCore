package rip.alpha.hcf.team.impl;

import lombok.Getter;
import lombok.Setter;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.SimpleText;
import net.mcscrims.libraries.util.cuboid.Cuboid;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.game.CapturableGame;
import rip.alpha.hcf.game.Game;
import rip.alpha.hcf.team.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SystemTeam extends Team {

    private boolean safezone = false, enderpearl = true, dontAllowPvpTimer = false, canHome = true;

    private UUID linkedGameId = null, linkedTownId = null;

    public SystemTeam(UUID id, String name, String color) {
        super(id, name, color);
    }

    public boolean isGame() {
        return this.linkedGameId != null;
    }

    public boolean isTown() {
        return this.linkedTownId != null;
    }

    public Game getGame() {
        if (this.linkedGameId == null) {
            return null;
        }
        return HCF.getInstance().getGameHandler().getGameById(this.linkedGameId);
    }

    @Override
    public void sendTeamInfo(CommandSender sender) {
        List<SimpleText> messages = new ArrayList<>();
        messages.add(new SimpleText("&7&m--------------------------"));
        sender.sendMessage(this.getDisplayName(sender));

        Integer x = null, z = null;

        if (this.isGame()) {
            Game game = this.getGame();
            if (game instanceof CapturableGame) {
                CapturableGame capturableGame = (CapturableGame) game;
                Cuboid capturingCuboid = capturableGame.getCaptureCuboid();
                if (capturingCuboid != null) {
                    Location center = capturingCuboid.getCenter();
                    x = center.getBlockX();
                    z = center.getBlockZ();
                }
            }
        }

        if (x == null) {
            ACuboid claimCuboid = this.getClaim();
            if (claimCuboid != null && this.getHome() != null) {
                Location location = this.getHome();
                x = location.getBlockX();
                z = location.getBlockZ();
            }
        }

        if (x != null) {
            SimpleText simpleText = new SimpleText("&6Location:&r x" + x + ", z" + z);
            if (sender.hasPermission("basic.teleportposition")) {
                simpleText.hover("&6Click to teleport to that location");
                simpleText.click("/tppos " + x + " 100 " + z);
            }
            messages.add(simpleText);
        }

        messages.add(new SimpleText("&7&m--------------------------"));
        messages.forEach(text -> text.send(sender));
    }

    @Override
    public String getDisplayName(CommandSender sender) {
        return CC.translate(this.getColor() + this.getName());
    }

    @Override
    public String getColor() {
        if (this.safezone) {
            return CC.GREEN;
        }
        return super.getColor();
    }

    @Override
    public void fromDocument(Document document) {
        super.fromDocument(document);

        this.safezone = document.get("safezone", this.safezone);
        this.enderpearl = document.get("enderpearl", this.enderpearl);
        this.dontAllowPvpTimer = document.get("pvpTimer", this.dontAllowPvpTimer);
        if (document.containsKey("linkedGameId")) {
            this.linkedGameId = UUID.fromString(document.getString("linkedGameId"));
        }
    }

    @Override
    public Document toDocument() {
        Document document = super.toDocument();

        document.put("safezone", this.safezone);
        document.put("enderpearl", this.enderpearl);
        document.put("pvpTimer", this.dontAllowPvpTimer);
        if (this.linkedGameId != null) {
            document.put("linkedGameId", this.linkedGameId.toString());
        }

        return document;
    }
}
