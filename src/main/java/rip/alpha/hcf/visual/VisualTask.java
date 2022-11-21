package rip.alpha.hcf.visual;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketWorldBorderRemove;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.grid.ClaimEntry;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.impl.SystemTeam;
import rip.alpha.hcf.timer.impl.CombatTagTimer;
import rip.alpha.hcf.timer.impl.PvPTimer;

import java.awt.Color;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class VisualTask implements Runnable {

    private final VisualHandler handler;

    @Override
    public void run() {
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                World world = player.getWorld();
                String worldName = world.getName();

                Location location = player.getLocation();
                int x = location.getBlockX();
                int y = location.getBlockY();
                int z = location.getBlockZ();

                boolean handleUnder15Mans = false;
                if (!HCF.getInstance().getConfiguration().isAllowUnder15MansInKoths()) {
                    PlayerTeam playerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
                    if (playerTeam != null && playerTeam.getOnlineMembers().size() < 15) {
                        handleUnder15Mans = true;
                    }
                }

                TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
                boolean handleCombatTagCooldown = teamProfile.hasTimer(CombatTagTimer.class);
                boolean handlePvPCooldown = teamProfile.hasTimer(PvPTimer.class);

                final boolean under15Man = handleUnder15Mans;

                this.handler.clearVisualBlocks(player, entry -> {
                    if (!entry.getWorldName().equalsIgnoreCase(worldName)) {
                        return true;
                    }

                    if (entry.getTeamId() != null) {
                        Team team = HCF.getInstance().getTeamHandler().getTeamById(entry.getTeamId());
                        if (team == null) {
                            return true;
                        }

                        if (entry.isClaimTemp()) {
                            return false;
                        }
                        if (team.getClaim() == null) {
                            return true;
                        }
                    }

                    if (entry.isClaimTemp()) {
                        return false;
                    }
                    if (entry.isPillar()) {
                        return false;
                    }

                    if (entry.getData() == 11 && !(under15Man || handlePvPCooldown)) {
                        return true;
                    } else if (entry.getData() == 14 && !handleCombatTagCooldown) {
                        return true;
                    } else if (entry.getData() == 5 && !handlePvPCooldown) {
                        return true;
                    }

                    return (Math.abs(x - entry.getX()) > (this.handler.getWallBorderHorizontalDiff() + 1)
                            || Math.abs(y - entry.getY()) > this.handler.getWallBorderHeightDiff()
                            || Math.abs(z - entry.getZ()) > (this.handler.getWallBorderHorizontalDiff() + 1));
                });

                Set<LCVisualBorderEntry> lunarWalls = this.handler.getLunarWalls(player.getUniqueId());

                if (lunarWalls != null && !lunarWalls.isEmpty()) {
                    Iterator<LCVisualBorderEntry> iterator = lunarWalls.iterator();

                    while (iterator.hasNext()) {
                        LCVisualBorderEntry entry = iterator.next();
                        Team team = HCF.getInstance().getTeamHandler().getTeamById(entry.getUuid());

                        if (team == null) {
                            this.sendLunarRemoveBorder(player, entry.getUuid());
                            iterator.remove();
                            continue;
                        }

                        if (entry.getColor().equals(Color.BLUE) && !(under15Man || handlePvPCooldown)) {
                            this.sendLunarRemoveBorder(player, entry.getUuid());
                            iterator.remove();
                        } else if (entry.getColor().equals(Color.RED) && !handleCombatTagCooldown) {
                            this.sendLunarRemoveBorder(player, entry.getUuid());
                            iterator.remove();
                        } else if (entry.getColor().equals(Color.GREEN) && !handlePvPCooldown) {
                            this.sendLunarRemoveBorder(player, entry.getUuid());
                            iterator.remove();
                        }
                    }
                }

                Set<ClaimEntry> teams = HCF.getInstance().getClaimGrid().getGridData(location, 8, 8, 8);

                if (lunarWalls != null && !lunarWalls.isEmpty()) {
                    Iterator<LCVisualBorderEntry> iterator = lunarWalls.iterator();
                    if (teams.isEmpty()) {
                        while (iterator.hasNext()) {
                            LCVisualBorderEntry entry = iterator.next();
                            this.sendLunarRemoveBorder(player, entry.getUuid());
                            iterator.remove();
                        }
                    } else {
                        while (iterator.hasNext()) {
                            LCVisualBorderEntry entry = iterator.next();
                            if (entry == null) {
                                continue;
                            }
                            if (entry.getUuid() == null) {
                                continue;
                            }
                            if (this.containsUUID(entry.getUuid(), teams)) {
                                continue;
                            }
                            this.sendLunarRemoveBorder(player, entry.getUuid());
                            iterator.remove();
                        }
                    }
                }

                for (ClaimEntry entry : teams) {
                    if (entry.getTeamId() == null) {
                        continue;
                    }
                    Team team = HCF.getInstance().getTeamHandler().getTeamById(entry.getTeamId());
                    if (team == null) {
                        continue;
                    }
                    if (team.getClaim() == null) {
                        continue;
                    }
                    if (team instanceof SystemTeam) {
                        SystemTeam systemTeam = (SystemTeam) team;
                        if (handleCombatTagCooldown && systemTeam.isSafezone()) {
                            this.handler.showClaimBorders(player, (byte) 14, team.getId(), entry.getCuboid());
                        } else if (systemTeam.isGame() && (handlePvPCooldown || handleUnder15Mans)) {
                            this.handler.showClaimBorders(player, (byte) 11, team.getId(), entry.getCuboid());
                        } else if (systemTeam.isDontAllowPvpTimer() && handlePvPCooldown){
                            this.handler.showClaimBorders(player, (byte) 5, team.getId(), entry.getCuboid());
                        }
                    } else {
                        if (handlePvPCooldown) {
                            this.handler.showClaimBorders(player, (byte) 5, team.getId(), entry.getCuboid());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendLunarRemoveBorder(Player player, UUID uuid) {
        LCPacketWorldBorderRemove remove = new LCPacketWorldBorderRemove(uuid.toString());
        LunarClientAPI.getInstance().sendPacket(player, remove);
    }

    private boolean containsUUID(UUID uuid, Set<ClaimEntry> teams) {
        for (ClaimEntry team : teams) {
            if (team.getTeamId() == null) {
                continue;
            }
            if (team.getTeamId().equals(uuid)) {
                return true;
            }
        }
        return false;
    }
}
