package rip.alpha.hcf.listener;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.event.LCPlayerRegisterEvent;
import com.lunarclient.bukkitapi.nethandler.shared.LCPacketWaypointRemove;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.FastNoiseLite;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.impl.SystemTeam;
import rip.alpha.hcf.timer.impl.CombatTagTimer;
import rip.alpha.hcf.timer.impl.PvPTimer;

import java.awt.Color;

public class PortalListener implements Listener {

    public static final BlockFace[] FACES = new BlockFace[]{
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    @EventHandler
    public void onEndPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            return;
        }

        World world = event.getTo().getWorld();
        Player player = event.getPlayer();

        if (world.getName().equalsIgnoreCase("world")) {
            Location location = HCF.getInstance().getEndHandler().getEndExitOverworld();
            if (location == null) {
                event.setCancelled(true);
                player.sendMessage(CC.RED + "There is not end exit, please message a staff member to fix this");
                return;
            }

            event.setTo(location);
        } else if (world.getName().equalsIgnoreCase("world_the_end")) {
            TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
            if (teamProfile == null) {
                return;
            }
            if (teamProfile.getTimer(PvPTimer.class) != null) {
                player.sendMessage(CC.RED + "You may not enter end with pvp-timer.");
                event.setCancelled(true);
                return;
            }

            if (teamProfile.getTimer(CombatTagTimer.class) != null) {
                player.sendMessage(CC.RED + "You may not enter end with combat tag.");
                event.setCancelled(true);
                return;
            }

            event.setTo(world.getSpawnLocation());
        }
    }

    @EventHandler
    public void onLCPlayerRegister(LCPlayerRegisterEvent event) {
        Player player = event.getPlayer();
        Location location = HCF.getInstance().getEndHandler().getEndExit();
        if (location == null) {
            return;
        }

        World world = location.getWorld();
        LCWaypoint waypoint = new LCWaypoint(CC.YELLOW + "End Exit", location, Color.YELLOW.getRGB(), false, true);
        LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove(CC.YELLOW + "End Exit", world.getUID().toString());
        LunarClientAPI.getInstance().sendPacket(player, removePacket);
        LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        for (BlockFace face : FACES) {
            if (event.getBlock().getRelative(face).getType() == Material.PORTAL) {
                event.getPlayer().sendMessage(CC.RED + "You cannot place blocks this close to the portal.");
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onBlockForm(EntityChangeBlockEvent event) {
        for (BlockFace face : FACES) {
            if (event.getBlock().getRelative(face).getType() == Material.PORTAL) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            return;
        }

        Player player = event.getPlayer();

        Location to = event.getTo();
        Location from = event.getFrom();

        World world = to.getWorld();
        World fromWorld = from.getWorld();
        TravelAgent agent = event.getPortalTravelAgent();


        if (fromWorld.getEnvironment() == World.Environment.NETHER) {
            Team team = HCF.getInstance().getTeamHandler().getTeamByLocation(from);
            if (team instanceof SystemTeam) {
                SystemTeam systemTeam = (SystemTeam) team;
                if (systemTeam.isSafezone()) {
                    event.setTo(world.getSpawnLocation());
                    return;
                }
            }

            if (!this.isSafePortal(player.getLocation(), to)) {
                Location created = this.findOrCreate(world, agent, from);
                if (created == null) {
                    player.sendMessage(CC.RED + "Could not find a safe portal location for you.");
                    event.setCancelled(true);
                    return;
                }

                event.setTo(created);
            }
        } else if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
            Team toTeam = HCF.getInstance().getTeamHandler().getTeamByLocation(to);

            if (toTeam instanceof SystemTeam) {

                if (toTeam.getName().contains("North")) {
                    to.add(32, 0, 100);
                } else if (toTeam.getName().contains("South")) {
                    to.subtract(32, 0, 100);
                } else if (toTeam.getName().contains("East")) {
                    to.add(100, 0, 32);
                } else if (toTeam.getName().contains("West")) {
                    to.subtract(100, 0, 32);
                }

                event.setTo(to);
            }
        }
    }

    private Location findOrCreate(World world, TravelAgent travelAgent, Location from) {
        FastNoiseLite fastNoiseLite = new FastNoiseLite((int) world.getSeed());
        fastNoiseLite.SetFrequency(0.1F);

        boolean isNether = world.getEnvironment() == World.Environment.NETHER;
        travelAgent.setCreationRadius(3); //not too far coz we trust ourselves :fingerscrossed:

        int x = from.getBlockX();
        int z = from.getBlockZ();

        int warzoneRadius = HCF.getInstance().getBorderHandler().getWarzoneRadius(isNether);
        boolean shouldNeg = fastNoiseLite.GetNoise(x + 10, z + 10) <= 0;
        int locX = (int) ((Math.abs(fastNoiseLite.GetNoise(x, z)) * 500) + warzoneRadius);
        int locZ = (int) ((Math.abs(fastNoiseLite.GetNoise(z, x)) * 500) + warzoneRadius);

        if (shouldNeg) {
            locX = -(locX);
            locZ = -(locZ);
        }

        int highestY = world.getHighestBlockYAt(locX, locZ);
        Location location = new Location(world, locX, highestY, locZ);
        if (!this.isSafePortal(from, location)) {
            return null;
        }
        return travelAgent.findOrCreate(location);
    }

    private boolean isSafePortal(Location from, Location to) {
        Team toTeam = HCF.getInstance().getTeamHandler().getTeamByLocation(to);
        Team fromTeam = HCF.getInstance().getTeamHandler().getTeamByLocation(from);

        if (fromTeam instanceof SystemTeam) {
            return toTeam instanceof SystemTeam;
        } else {
            if (toTeam instanceof SystemTeam) {
                return false;
            }
            if (toTeam instanceof PlayerTeam) {
                return false;
            }
            if (!HCF.getInstance().getBorderHandler().inBorder(to)) {
                return false;
            }
            return !HCF.getInstance().getBorderHandler().inWarzone(to);
        }
    }
}
