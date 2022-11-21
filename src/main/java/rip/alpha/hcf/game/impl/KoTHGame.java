package rip.alpha.hcf.game.impl;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.shared.LCPacketWaypointRemove;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import lombok.Getter;
import lombok.Setter;
import net.mcscrims.basic.Basic;
import net.mcscrims.basic.profile.BasicProfile;
import net.mcscrims.libraries.util.BalanceUtil;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TimeUtil;
import net.mcscrims.libraries.util.cuboid.Cuboid;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.crates.Crate;
import rip.alpha.hcf.game.CapturableGame;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.settings.Setting;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class KoTHGame extends CapturableGame {

    public final Color waypointColor = new Color(51, 119, 255);
    public final String waypointNameColor = CC.AQUA;

    private long startTimeMillis = TimeUnit.MINUTES.toMillis(15);
    private long currentTimeMillis = this.startTimeMillis;
    private boolean active = false;

    public KoTHGame(UUID uuid, UUID owningTeam) {
        super(uuid, owningTeam);
    }

    @Override
    public void start() {
        this.reset();
        this.active = true;

        Cuboid claimCuboid = this.getCaptureCuboid();
        if (claimCuboid != null) {
            LCWaypoint waypoint = new LCWaypoint(waypointNameColor + this.getName(), claimCuboid.getCenter(), this.waypointColor.getRGB(), false, true);
            LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove(waypointNameColor + this.getName(), claimCuboid.getWorld().getUID().toString());

            for (Player player : Bukkit.getOnlinePlayers()) {
                LunarClientAPI.getInstance().sendPacket(player, removePacket);
                LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
            }
        }

        this.broadcastMessage(this.getColor() + this.getName() + " &6can can now be contested.");
    }

    @Override
    public void resetCapturingPlayer() {
        if (this.isActive()) {
            Player player = this.getCapturingPlayer();
            if (player != null) {
                if ((this.startTimeMillis - currentTimeMillis) > TimeUnit.MINUTES.toMillis(1)) {
                    this.broadcastMessage("&6Control of " + this.getColor() + this.getName() + " &6lost. &9(" + this.formatRemainingTime() + ")");
                }
            }
        }

        super.resetCapturingPlayer();
    }

    @Override
    public void end(boolean forced) {
        Player player = this.getCapturingPlayer();
        this.active = false;
        this.reset();

        Cuboid claimCuboid = this.getCaptureCuboid();
        if (claimCuboid != null) {
            LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove(CC.AQUA + this.getName(), claimCuboid.getWorld().getUID().toString());
            for (Player online : Bukkit.getOnlinePlayers()) {
                LunarClientAPI.getInstance().sendPacket(online, removePacket);
            }
        }

        if (!forced && player != null) {
            boolean isPalace = this.getName().equalsIgnoreCase("Palace");
            BasicProfile basicProfile = Basic.getInstance().getBasicAPI().getProfile(player.getUniqueId());
            basicProfile.addXp(isPalace ? 750 : 200);

            PlayerTeam playerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
            String teamName = playerTeam.getName();
            this.broadcastMessage(this.getColor() + this.getName() + " &6has been captured by &e" + teamName + ".");

            playerTeam.setKothsCapped(playerTeam.getKothsCapped() + 1);

            int balanceReceived = isPalace ? 60000 : 20000;
            playerTeam.setBalance(playerTeam.getBalance() + balanceReceived);
            playerTeam.addPoints(isPalace ? 100 : 25);
            playerTeam.broadcast("", "&6Your team has been rewarded &e$" + BalanceUtil.formatBalance(balanceReceived), PlayerTeam.TeamMember.TEAM_MEMBER);
            playerTeam.setSave(true);

            String kothType = isPalace ? "Palace" : "KoTH";
            Crate crate = HCF.getInstance().getCrateHandler().getCrateByName(kothType);
            if (crate != null) {
                ItemStack itemStack = crate.getKey().clone();
                itemStack.setAmount(3);
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                    player.sendMessage(CC.RED + "You did not have enough inventory space to collect your keys, they have been dropped on the floor.");
                    return;
                }

                player.getInventory().addItem(itemStack);
                player.sendMessage(CC.GOLD + "You have received 3x " + CC.YELLOW + kothType + CC.GOLD + " keys!");
            }

        } else {
            this.broadcastMessage(this.getColor() + this.getName() + " can no longer be contested.");
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.currentTimeMillis = this.startTimeMillis;
    }

    @Override
    public void tick() {
        if (!this.active) {
            return;
        }

        super.tick();

        Player player = this.getCapturingPlayer();
        if (player != null) {
            this.currentTimeMillis--;

            if (this.isCompleted()) {
                this.end(false);
                return;
            }

            int modular = this.currentTimeMillis > 60000 ? 60000 : (this.currentTimeMillis > 10000) ? 10000 : 1000;
            if (this.currentTimeMillis % modular == 0L) {
                this.broadcastMessage(this.getColor() + this.getName() + " &6is trying to be controlled. \n - Time left: &9" + this.formatRemainingTime());
            }
        }
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public List<String> getScoreboardLines() {
        List<String> lines = new ArrayList<>();
        lines.add(this.getColor() + this.getName() + ": &r" + this.formatRemainingTime());
        return lines;
    }

    @Override
    public boolean isCompleted() {
        return this.currentTimeMillis <= 0;
    }

    @Override
    public String getName() {
        return this.getOwningTeam().getName();
    }

    @Override
    public String getColor() {
        return this.getOwningTeam().getColor();
    }

    @Override
    public Document toDocument() {
        Document document = super.toDocument();
        document.put("startTime", this.startTimeMillis);
        return document;
    }

    @Override
    public void fromDocument(Document document) {
        super.fromDocument(document);
        this.startTimeMillis = document.get("startTime", this.startTimeMillis);
    }

    public String formatRemainingTime() {
        return TimeUtil.millisToTimer(this.currentTimeMillis);
    }

    private void broadcastMessage(String message) {
        message = CC.translate(CC.KOTH + message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
            if (profile == null) {
                continue;
            }
            if (!profile.getSetting(Setting.KOTH_MESSAGES)) {
                continue;
            }
            player.sendMessage(message);
        }
    }
}
