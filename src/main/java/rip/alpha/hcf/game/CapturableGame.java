package rip.alpha.hcf.game;

import lombok.Getter;
import lombok.Setter;
import net.mcscrims.libraries.util.cuboid.Cuboid;
import net.mcscrims.libraries.util.gson.GsonUtil;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.impl.SystemTeam;
import rip.alpha.hcf.timer.impl.PvPTimer;
import rip.alpha.modsuite.ModSuiteMode;
import rip.alpha.modsuite.ModSuitePlugin;

import java.util.UUID;

@Getter
@Setter
public abstract class CapturableGame extends Game {

    private UUID owningTeam;
    private Cuboid captureCuboid = null;
    private UUID capturingUUID = null;

    public CapturableGame(UUID uuid, UUID owningTeam) {
        super(uuid);
        this.owningTeam = owningTeam;
    }

    public void resetCapturingPlayer() {
        this.capturingUUID = null;
    }

    public void setCapturingPlayer(Player player) {
        this.capturingUUID = player.getUniqueId();
    }

    public Player getCapturingPlayer() {
        if (this.capturingUUID == null) {
            return null;
        }
        return Bukkit.getPlayer(this.capturingUUID);
    }

    public SystemTeam getOwningTeam() {
        return HCF.getInstance().getTeamHandler().getSystemTeamById(this.owningTeam);
    }

    @Override
    public void reset() {
        this.resetCapturingPlayer();
    }

    @Override
    public void tick() {
        if (!this.isActive()) {
            return;
        }

        if (this.captureCuboid == null || this.getOwningTeam() == null || this.getOwningTeam().getClaim() == null) {
            this.end(false);
            return;
        }

        if (this.capturingUUID != null) {
            Player player = this.getCapturingPlayer();
            if (!this.isValidCapture(player)) {
                this.reset();
            }
        }

        if (this.capturingUUID == null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (this.isValidCapture(player)) {
                    this.setCapturingPlayer(player);
                    return;
                }
            }
        }
    }

    public boolean isValidCapture(Player player) {
        if (player == null) {
            return false;
        }
        if (!player.isValid()) {
            return false;
        }
        if (player.isDead()) {
            return false;
        }
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return false;
        }

        ModSuiteMode mode = ModSuitePlugin.getInstance().getModSuiteAPI().getCurrentMode(player);
        if (mode != ModSuiteMode.NONE) {
            return false;
        }

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);

        if (teamProfile != null) {
            if (teamProfile.hasTimer(PvPTimer.class)) {
                return false;
            }
        }

        PlayerTeam playerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
        if (playerTeam == null) {
            return false;
        }
        return this.getOwningTeam().getClaim().contains(player.getLocation()) && this.getCaptureCuboid().contains(player.getLocation());
    }

    @Override
    public Document toDocument() {
        Document document = super.toDocument();

        if (owningTeam != null) {
            document.put("owningTeamId", this.owningTeam.toString());
        }
        if (captureCuboid != null) {
            document.put("captureCuboid", GsonUtil.GSON.toJson(this.captureCuboid));
        }

        return document;
    }

    @Override
    public void fromDocument(Document document) {
        if (document.containsKey("owningTeamId")) {
            this.owningTeam = UUID.fromString(document.getString("owningTeamId"));
        }
        if (document.containsKey("captureCuboid")) {
            this.captureCuboid = GsonUtil.GSON.fromJson(document.getString("captureCuboid"), Cuboid.class);
        }
    }
}
