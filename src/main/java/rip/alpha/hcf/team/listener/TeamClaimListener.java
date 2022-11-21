package rip.alpha.hcf.team.listener;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.ItemBuilder;
import net.mcscrims.libraries.util.TaskUtil;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboidException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.timer.impl.PvPTimer;

import java.util.Arrays;
import java.util.UUID;

@RequiredArgsConstructor
public class TeamClaimListener implements Listener {

    public static final ItemStack CLAIM_WAND =
            new ItemBuilder(Material.GOLD_HOE)
                    .name(CC.GOLD + "Claiming Wand " + CC.GRAY + "(Hover over this item to view info)")
                    .lore(CC.translateLines(Arrays.asList(
                            "&eRight/Left Click Block",
                            "&f- Select claim's corners",
                            " ",
                            "&eRight click air",
                            "&f- Cancel current claim",
                            " ",
                            "&eShift left click air",
                            "&f- Proceed with selected claim")))
                    .build();

    private final TeamHandler teamHandler;

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop() == null) {
            return;
        }
        if (event.getItemDrop().getItemStack() == null) {
            return;
        }
        if (!event.getItemDrop().getItemStack().isSimilar(CLAIM_WAND)) {
            return;
        }
        event.getItemDrop().remove();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem()) {
            ItemStack itemStack = event.getItem();

            if (!itemStack.isSimilar(CLAIM_WAND)) {
                return;
            }

            event.setCancelled(true);

            Player player = event.getPlayer();
            TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);

            if (profile.getClaimingFor() == null) {
                player.sendMessage(CC.RED + "You are currently not claiming for anything, use /team claim");
                return;
            }

            UUID teamId = profile.getClaimingFor();
            Team team = teamHandler.getTeamById(teamId);

            if (team == null) {
                this.teamHandler.clearClaiming(player, profile);
                player.sendMessage(CC.RED + "You are currently not claiming for anything, use /team claim");
                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                this.teamHandler.clearClaiming(player, profile);
                player.sendMessage(CC.RED + "You have cleared your claiming selection");
                return;
            }

            if (event.getAction() == Action.LEFT_CLICK_AIR) {
                if (player.isSneaking()) {
                    Location[] locations = profile.getSelectedLocations();

                    if (locations[0] == null || locations[1] == null) {
                        player.sendMessage(CC.RED + "You haven't selected your corners yet");
                        return;
                    }

                    try {
                        ACuboid cuboid = new ACuboid(locations[0], locations[1]);
                        TaskUtil.runAsync(() -> {
                            String message = teamHandler.createClaim(team, cuboid);
                            boolean failed = message.startsWith(CC.RED);
                            player.sendMessage(message);
                            if (!failed) {
                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    if (online.willBeOnline()) {
                                        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(online.getUniqueId());
                                        if (teamProfile.hasTimer(PvPTimer.class)) {
                                            PlayerTeam playerTeam = this.teamHandler.getPlayerTeamById(teamId);
                                            if (cuboid.contains(online.getLocation())) {
                                                HCF.getInstance().getTimerHandler().teleportToSafeLocation(online, playerTeam);
                                            }
                                        }
                                    }
                                }

                                teamHandler.clearClaiming(player, profile);
                            }
                        }, HCF.getInstance());
                    } catch (ACuboidException e) {
                        player.sendMessage(CC.RED + "Your corners are not in the same world.");
                        teamHandler.clearClaiming(player, profile);
                    }
                }
            }

            if (!event.hasBlock()) {
                return;
            }

            Block block = event.getClickedBlock();

            if (team instanceof PlayerTeam) {
                if (!block.getLocation().getWorld().getName().equals("world")) {
                    player.sendMessage(CC.RED + "You cannot claim outside of the overworld");
                    return;
                }

                if (!HCF.getInstance().getBorderHandler().inClaimRadius(block.getLocation())) {
                    player.sendMessage(CC.RED + "You have to be " + HCF.getInstance().getBorderHandler().getClaimRadius() + " blocks out to begin claiming");
                    return;
                }
            }

            if (!profile.canAttemptClaim()) {
                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                this.clearClaimEntry(player, profile, 0);
                profile.getSelectedLocations()[0] = event.getClickedBlock().getLocation();
                player.sendMessage(CC.GREEN + "You have made your first selection");
                HCF.getInstance().getVisualHandler().showPillar(player, Material.EMERALD_BLOCK, teamId, profile.getSelectedLocations()[0], true);
                this.checkCornersAndSendMessage(player, profile, team);
                profile.setLastClaimAttempt(System.currentTimeMillis() + 500);
                return;
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                this.clearClaimEntry(player, profile, 1);
                profile.getSelectedLocations()[1] = event.getClickedBlock().getLocation();
                player.sendMessage(CC.GREEN + "You have made your second selection");
                HCF.getInstance().getVisualHandler().showPillar(player, Material.EMERALD_BLOCK, teamId, profile.getSelectedLocations()[1], true);
                this.checkCornersAndSendMessage(player, profile, team);
                profile.setLastClaimAttempt(System.currentTimeMillis() + 500);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        this.teamHandler.clearClaiming(player, profile);
    }

    private void clearClaimEntry(Player player, TeamProfile profile, int index) {
        Location location = profile.getSelectedLocations()[index];
        if (location != null) {
            HCF.getInstance().getVisualHandler().clearVisualBlocks(player, entry -> {
                if (entry.getWorldName().equals(location.getWorld().getName())
                        && entry.getX() == location.getBlockX() && entry.getZ() == location.getBlockZ()) {
                    return entry.isClaimTemp();
                }
                return false;
            });
        }
    }

    private void checkCornersAndSendMessage(Player player, TeamProfile profile, Team team) {
        if (profile.getSelectedLocations()[0] != null && profile.getSelectedLocations()[1] != null) {
            player.sendMessage(CC.GREEN + "You now have two selected locations, shift right click the air to create your claim");

            if (team instanceof PlayerTeam) {
                Location firstLocation = profile.getSelectedLocations()[0];
                Location secondLocation = profile.getSelectedLocations()[1];

                int x = Math.abs(firstLocation.getBlockX() - secondLocation.getBlockX()) + 1; //because the block itself isnt accounted for
                int z = Math.abs(firstLocation.getBlockZ() - secondLocation.getBlockZ()) + 1; //because the block itself isnt accounted for

                int price = this.teamHandler.calculatePrice(x, z);
                player.sendMessage(CC.GREEN + "The current price for this claim is $" + price);
            }
        }
    }

    @EventHandler
    public void onWorldLoadEvent(WorldLoadEvent event) {
        HCF.getInstance().getClaimGrid().registerWorld(event.getWorld());
    }
}
