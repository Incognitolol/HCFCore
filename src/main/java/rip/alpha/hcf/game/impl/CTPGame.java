package rip.alpha.hcf.game.impl;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.shared.LCPacketWaypointRemove;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class CTPGame extends CapturableGame {

    private boolean paused = false, active = false;

    private long startTimeMillis = TimeUnit.MINUTES.toMillis(10);
    private long currentTimeMillis = this.startTimeMillis;

    public final Color waypointColor = new Color(186, 85, 211);
    public final String waypointNameColor = CC.LIGHT_PURPLE;

    private final List<CTPPointEntry> pointEntries = new CopyOnWriteArrayList<>();

    public CTPGame(UUID uuid, UUID owningTeam) {
        super(uuid, owningTeam);
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
    public void start() {
        this.active = true;
        this.reset();

        Cuboid claimCuboid = this.getCaptureCuboid();
        if (claimCuboid != null) {
            LCWaypoint waypoint = new LCWaypoint(CC.PINK + this.getName(), claimCuboid.getCenter(), waypointColor.getRGB(), false, true);
            LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove(CC.PINK + this.getName(), claimCuboid.getWorld().getUID().toString());

            for (Player player : Bukkit.getOnlinePlayers()) {
                LunarClientAPI.getInstance().sendPacket(player, removePacket);
                LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
            }
        }


        this.broadcastMessage(this.getColor() + this.getName() + " &ecan now be contested!");
    }

    @Override
    public void tick() {
        super.tick();

        if (this.paused) {
            if (this.hasOtherTeam()) {
                return;
            }
            this.paused = false;
        }

        if (this.hasOtherTeam()) {
            this.paused = true;
            return;
        }

        if (this.getCapturingUUID() == null) {
            return;
        }

        this.currentTimeMillis--;

        if (this.currentTimeMillis <= 0) {
            PlayerTeam playerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(this.getCapturingUUID());
            int points = this.incrementPointEntry(playerTeam.getId());

            if (this.isCompleted()) {
                this.end(false);
                return;
            }

            this.broadcastMessage(playerTeam.getName() + " &ehas captured a point at " + this.getColor() + this.getName() + " &e(" + points + ")");
            this.reset();
        }
    }

    @Override
    public void end(boolean forced) {
        this.active = false;
        this.pointEntries.clear();

        Player player = this.getCapturingPlayer();

        Cuboid claimCuboid = this.getCaptureCuboid();
        if (claimCuboid != null) {
            LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove(CC.PINK + this.getName(), claimCuboid.getWorld().getUID().toString());
            for (Player online : Bukkit.getOnlinePlayers()) {
                LunarClientAPI.getInstance().sendPacket(online, removePacket);
            }
        }

        if (!forced) {
            PlayerTeam playerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
            String teamName = playerTeam.getName();
            this.broadcastMessage(this.getColor() + this.getName() + " &6has been conquered by &e" + teamName + ".");

            Crate crate = HCF.getInstance().getCrateHandler().getCrateByName("CTP");
            if (crate != null) {
                ItemStack itemStack = crate.getKey().clone();
                itemStack.setAmount(3);
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                    player.sendMessage(CC.RED + "You did not have enough inventory space to collect your keys, they have been dropped on the floor.");
                    return;
                }

                player.getInventory().addItem(itemStack);
                player.sendMessage(CC.GOLD + "You have received 3x " + CC.YELLOW + "CTP" + CC.GOLD + " keys!");
            }
        } else {
            this.broadcastMessage(this.getColor() + this.getName() + " can no longer be contested.");
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.currentTimeMillis = startTimeMillis;
        this.paused = false;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public List<String> getScoreboardLines() {
        List<String> list = new ArrayList<>();

        String time = TimeUtil.millisToTimer(this.currentTimeMillis);
        if (this.paused) {
            time = CC.RED + "Contested";
        }
        list.add(this.getColor() + this.getName() + ": " + CC.RESET + time);


        for (int i = 0; i < 4; i++) {
            CTPPointEntry pointEntry = this.getPoints(i);
            if (pointEntry != null) {
                list.add(CC.YELLOW + pointEntry.getName() + ": " + CC.GRAY + "[" + pointEntry.getPoints() + "/4]");
            }
        }

        return list;
    }

    @Override
    public boolean isCompleted() {
        CTPPointEntry highestPoint = this.getHighestPoints();
        if (highestPoint == null) {
            return false;
        }
        return highestPoint.getPoints() >= 4;
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

    private void sortPointEntries() {
        this.pointEntries.sort(Comparator.comparingInt(value -> -value.getPoints()));
    }

    private CTPPointEntry getByTeamId(UUID teamId) {
        for (CTPPointEntry pointEntry : this.pointEntries) {
            if (pointEntry.teamId.equals(teamId)) {
                return pointEntry;
            }
        }
        return null;
    }

    public void removePointEntry(UUID teamId) {
        this.pointEntries.removeIf(ctpPointEntry -> ctpPointEntry.teamId.equals(teamId));
    }

    private int incrementPointEntry(UUID teamId) {
        CTPPointEntry pointEntry = this.getByTeamId(teamId);
        if (pointEntry == null) {
            pointEntry = new CTPPointEntry(teamId);
            this.pointEntries.add(pointEntry);
        }
        pointEntry.incrementPoint();
        this.sortPointEntries();
        return pointEntry.getPoints();
    }

    public CTPPointEntry getHighestPoints() {
        return this.getPoints(0);
    }

    public CTPPointEntry getPoints(int index) {
        if (this.pointEntries.isEmpty()) {
            return null;
        }
        if (index >= this.pointEntries.size()) {
            return null;
        }
        return this.pointEntries.get(index);
    }

    private boolean hasOtherTeam() {
        if (this.getCapturingUUID() == null) {
            return false;
        }
        Player player = this.getCapturingPlayer();
        if (player == null) {
            return false;
        }
        PlayerTeam playerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
        if (playerTeam == null) {
            return false;
        }

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!this.isValidCapture(target)) {
                continue;
            }
            if (playerTeam.getMember(target.getUniqueId()) != null) {
                continue;
            }
            PlayerTeam targetTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(target);
            if (targetTeam == null) {
                continue;
            }
            if (!playerTeam.getId().equals(targetTeam.getId())) {
                return true;
            }
        }

        return false;
    }

    private void broadcastMessage(String message) {
        message = CC.translate(CC.CAPTURE_THE_POINT + message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
            if (!teamProfile.getSetting(Setting.CTP_MESSAGES)) {
                continue;
            }
            player.sendMessage(message);
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class CTPPointEntry {
        private final UUID teamId;
        private int points = 0;

        public String getName() {
            return HCF.getInstance().getTeamHandler().getPlayerTeamById(this.teamId).getName();
        }

        public void incrementPoint() {
            this.points++;
        }
    }
}
