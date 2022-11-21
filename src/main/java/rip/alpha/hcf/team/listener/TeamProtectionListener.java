package rip.alpha.hcf.team.listener;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.impl.SystemTeam;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class TeamProtectionListener implements Listener {

    private final TeamHandler teamHandler;

    private final Set<Material> interactBlacklist;
    private final Set<Material> dispenserBlacklist;

    public TeamProtectionListener(TeamHandler teamHandler) {
        this.teamHandler = teamHandler;

        this.interactBlacklist = new HashSet<>();
        this.dispenserBlacklist = new HashSet<>();

        this.interactBlacklist.add(Material.TRAPPED_CHEST);
        this.interactBlacklist.add(Material.ANVIL);
        this.interactBlacklist.add(Material.BEACON);
        this.interactBlacklist.add(Material.ENDER_CHEST);
        this.interactBlacklist.add(Material.FENCE_GATE);
        this.interactBlacklist.add(Material.LEVER);
        this.interactBlacklist.add(Material.CHEST);
        this.interactBlacklist.add(Material.DISPENSER);
        this.interactBlacklist.add(Material.HOPPER);
        this.interactBlacklist.add(Material.ENCHANTMENT_TABLE);
        this.interactBlacklist.add(Material.WORKBENCH);
        this.interactBlacklist.add(Material.FURNACE);
        this.interactBlacklist.add(Material.BURNING_FURNACE);
        this.interactBlacklist.add(Material.PAINTING);
        this.interactBlacklist.add(Material.ITEM_FRAME);
        this.interactBlacklist.add(Material.BREWING_STAND);
        this.interactBlacklist.add(Material.DROPPER);
        this.interactBlacklist.add(Material.SOIL);

        for (Material material : Material.values()) {
            if (material.name().contains("_DOOR")) {
                this.interactBlacklist.add(material);
            }
            if (material.name().contains("DIODE")) {
                this.interactBlacklist.add(material);
            }
            if (material.name().contains("_COMPARATOR")) {
                this.interactBlacklist.add(material);
            }
        }

        this.dispenserBlacklist.add(Material.BUCKET);
        this.dispenserBlacklist.add(Material.WATER_BUCKET);
        this.dispenserBlacklist.add(Material.LAVA_BUCKET);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }
        Team team = this.teamHandler.getTeamByLocation(event.getEntity().getLocation());
        if (!(team instanceof SystemTeam)) {
            return;
        }
        if (!((SystemTeam) team).isSafezone()) {
            return;
        }
        event.getEntity().remove();
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (this.isBypass(player)) {
            return;
        }
        Block block = event.getBlock();
        PlayerTeam playerTeam = this.teamHandler.getPlayerTeamByPlayer(player);
        Team blockTeam = this.teamHandler.getTeamByLocation(block.getLocation());
        if (blockTeam == null) {
            return;
        }

        if (blockTeam instanceof PlayerTeam) {
            PlayerTeam playerBlockTeam = (PlayerTeam) blockTeam;
            if (playerBlockTeam.isRaidable()) {
                return;
            }
            if (playerTeam != null && playerBlockTeam.getId().equals(playerTeam.getId())) {
                return;
            }
        } else {
            if (blockTeam.getName().equalsIgnoreCase("Glowstone") && block.getType() == Material.GLOWSTONE) {
                return;
            }
        }

        String teamName = blockTeam.getDisplayName(player);
        teamName = teamName + CC.getSorApostrophe(teamName) + CC.YELLOW;
        player.sendMessage(CC.translate("&eYou cannot build in " + teamName + " territory!"));

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (this.isBypass(player)) {
            return;
        }
        Block block = event.getBlock();
        PlayerTeam playerTeam = this.teamHandler.getPlayerTeamByPlayer(player);
        Team blockTeam = this.teamHandler.getTeamByLocation(block.getLocation());
        if (blockTeam == null) {
            return;
        }

        if (blockTeam instanceof PlayerTeam) {
            PlayerTeam playerBlockTeam = (PlayerTeam) blockTeam;
            if (playerBlockTeam.isRaidable()) {
                return;
            }
            if (playerTeam != null && playerBlockTeam.getId().equals(playerTeam.getId())) {
                return;
            }
        }

        String teamName = blockTeam.getDisplayName(player);
        teamName = teamName + CC.getSorApostrophe(teamName) + CC.YELLOW;
        player.sendMessage(CC.translate("&eYou cannot build in " + teamName + " territory!"));

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (this.isBypass(player)) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (!this.interactBlacklist.contains(block.getType())) {
            return;
        }

        if (block.getType() == Material.SOIL) {
            if (event.getAction() != Action.PHYSICAL) {
                return;
            }
        }

        PlayerTeam playerTeam = this.teamHandler.getPlayerTeamByPlayer(player);
        Team blockTeam = this.teamHandler.getTeamByLocation(block.getLocation());
        if (blockTeam == null) {
            return;
        }

        if (blockTeam instanceof PlayerTeam) {
            PlayerTeam playerBlockTeam = (PlayerTeam) blockTeam;
            if (playerBlockTeam.isRaidable()) {
                return;
            }
            if (playerTeam != null && playerBlockTeam.getId().equals(playerTeam.getId())) {
                return;
            }
        }

        if (block.getType() != Material.SOIL) {
            String teamName = blockTeam.getDisplayName(player);
            teamName = teamName + CC.getSorApostrophe(teamName) + CC.YELLOW;
            player.sendMessage(CC.translate("&eYou cannot build in " + teamName + " territory!"));
        }

        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        Entity entity = event.getRemover();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;
        if (this.isBypass(player)) {
            return;
        }
        EntityType type = event.getEntity().getType();
        if (type != EntityType.ITEM_FRAME && type != EntityType.PAINTING) {
            return;
        }

        PlayerTeam playerTeam = this.teamHandler.getPlayerTeamByPlayer(player);
        Team entityTeam = this.teamHandler.getTeamByLocation(entity.getLocation());
        if (entityTeam == null) {
            return;
        }

        if (entityTeam instanceof PlayerTeam) {
            PlayerTeam playerBlockTeam = (PlayerTeam) entityTeam;
            if (playerBlockTeam.isRaidable()) {
                return;
            }
            if (playerTeam != null && playerBlockTeam.getId().equals(playerTeam.getId())) {
                return;
            }
        }

        String teamName = entityTeam.getDisplayName(player);
        teamName = teamName + CC.getSorApostrophe(teamName) + CC.YELLOW;
        player.sendMessage(CC.translate("&eYou cannot build in " + teamName + " territory!"));

        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (this.isBypass(player)) {
            return;
        }
        Block block = event.getBlock();
        if (block == null || block.getType() == Material.AIR) {
            return;
        }
        EntityType type = event.getEntity().getType();
        if (type != EntityType.ITEM_FRAME && type != EntityType.PAINTING) {
            return;
        }

        PlayerTeam playerTeam = this.teamHandler.getPlayerTeamByPlayer(player);
        Team blockTeam = this.teamHandler.getTeamByLocation(block.getLocation());
        if (blockTeam == null) {
            return;
        }

        if (blockTeam instanceof PlayerTeam) {
            PlayerTeam playerBlockTeam = (PlayerTeam) blockTeam;
            if (playerBlockTeam.isRaidable()) {
                return;
            }
            if (playerTeam != null && playerBlockTeam.getId().equals(playerTeam.getId())) {
                return;
            }
        }

        String teamName = blockTeam.getDisplayName(player);
        teamName = teamName + CC.getSorApostrophe(teamName) + CC.YELLOW;
        player.sendMessage(CC.translate("&eYou cannot build in " + teamName + " territory!"));

        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ItemFrame && event.getDamager() instanceof Player) {
            ItemFrame itemFrame = (ItemFrame) event.getEntity();
            Player player = (Player) event.getDamager();

            if (this.isBypass(player)) {
                return;
            }

            PlayerTeam playerTeam = this.teamHandler.getPlayerTeamByPlayer(player);
            Team blockTeam = this.teamHandler.getTeamByLocation(itemFrame.getLocation());
            if (blockTeam == null) {
                return;
            }

            if (blockTeam instanceof PlayerTeam) {
                PlayerTeam playerBlockTeam = (PlayerTeam) blockTeam;
                if (playerBlockTeam.isRaidable()) {
                    return;
                }
                if (playerTeam != null && playerBlockTeam.getId().equals(playerTeam.getId())) {
                    return;
                }
            }

            String teamName = blockTeam.getDisplayName(player);
            teamName = teamName + CC.getSorApostrophe(teamName) + CC.YELLOW;
            player.sendMessage(CC.translate("&eYou cannot build in " + teamName + " territory!"));

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (this.isBypass(player)) {
            return;
        }
        Entity entity = event.getRightClicked();
        if (!(entity instanceof ItemFrame)) {
            return;
        }

        PlayerTeam playerTeam = this.teamHandler.getPlayerTeamByPlayer(player);
        Team blockTeam = this.teamHandler.getTeamByLocation(entity.getLocation());
        if (blockTeam == null) {
            return;
        }

        if (blockTeam instanceof PlayerTeam) {
            PlayerTeam playerBlockTeam = (PlayerTeam) blockTeam;
            if (playerBlockTeam.isRaidable()) {
                return;
            }
            if (playerTeam != null && playerBlockTeam.getId().equals(playerTeam.getId())) {
                return;
            }
        }

        String teamName = blockTeam.getDisplayName(player);
        teamName = teamName + CC.getSorApostrophe(teamName) + CC.YELLOW;
        player.sendMessage(CC.translate("&eYou cannot build in " + teamName + " territory!"));

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() != BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) {
            return;
        }
        if (event.getPlayer() != null) {
            Player player = event.getPlayer();
            if (this.isBypass(player)) {
                return;
            }
            Block block = event.getBlock();
            if (block == null || block.getType() == Material.AIR) {
                return;
            }
            PlayerTeam playerTeam = this.teamHandler.getPlayerTeamByPlayer(player);
            Team blockTeam = this.teamHandler.getTeamByLocation(block.getLocation());
            if (blockTeam == null) {
                return;
            }

            if (blockTeam instanceof PlayerTeam) {
                PlayerTeam playerBlockTeam = (PlayerTeam) blockTeam;
                if (playerBlockTeam.isRaidable()) {
                    return;
                }
                if (playerTeam != null && playerBlockTeam.getId().equals(playerTeam.getId())) {
                    return;
                }
            }

            String teamName = blockTeam.getDisplayName(player);
            teamName = teamName + CC.getSorApostrophe(teamName) + CC.YELLOW;
            player.sendMessage(CC.translate("&eYou cannot build in " + teamName + " territory!"));

            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Block pistonBlock = event.getBlock();
        Location pistonLocation = pistonBlock.getLocation();
        Team fromTeam = this.teamHandler.getTeamByLocation(pistonLocation);
        for (Block block : event.getBlocks()) {
            block = block.getRelative(event.getDirection());
            if (block == null) {
                continue;
            }
            Team toTeam = this.teamHandler.getTeamByLocation(block.getLocation());
            if (toTeam == null) {
                continue;
            }
            if (fromTeam != null && fromTeam.getId().equals(toTeam.getId())) {
                continue;
            }
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        Block pistonBlock = event.getBlock();
        Block fromBlock = event.getRetractLocation().getBlock().getRelative(event.getDirection());
        if (fromBlock == null || fromBlock.getType() == Material.AIR) {
            return;
        }
        Team fromTeam = this.teamHandler.getTeamByLocation(fromBlock.getLocation());
        Team toTeam = this.teamHandler.getTeamByLocation(pistonBlock.getLocation());
        if (fromTeam == null) {
            return;
        }
        if (toTeam != null && fromTeam.getId().equals(toTeam.getId())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onLiquidDispense(BlockDispenseEvent event) {
        ItemStack itemStack = event.getItem();
        if (itemStack == null) {
            return;
        }
        if (!this.dispenserBlacklist.contains(itemStack.getType())) {
            return;
        }
        Block fromBlock = event.getBlock();
        Location fromLocation = fromBlock.getLocation();
        Location toLocation = event.getVelocity().toLocation(fromLocation.getWorld());
        Team fromTeam = this.teamHandler.getTeamByLocation(fromLocation);
        Team toTeam = this.teamHandler.getTeamByLocation(toLocation);
        if (toTeam == null) {
            return;
        }
        if (fromTeam != null && fromTeam.getId().equals(toTeam.getId())) {
            return;
        }
        event.setCancelled(true);
    }

//    @EventHandler
//    public void onBlockFrom(BlockFromToEvent event){
//        Block fromBlock = event.getBlock();
//        Block toBlock = event.getToBlock();
//        if (fromBlock.getType() == Material.WATER) return;
//
//        Location fromLocation = fromBlock.getLocation();
//        Location toLocation = toBlock.getLocation();
//
//        if (fromLocation.getWorld().getEnvironment() != World.Environment.NORMAL) return;
//
//        Team fromTeam = this.teamHandler.getTeamByLocation(fromLocation);
//        Team toTeam = this.teamHandler.getTeamByLocation(toLocation);
//        if (toTeam == null) return;
//        if (fromTeam != null && fromTeam.getId().equals(toTeam.getId())) return;
//        event.setCancelled(true);
//    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        this.handleBucketEvent(event);
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        this.handleBucketEvent(event);
    }

    private void handleBucketEvent(PlayerBucketEvent event) {
        Player player = event.getPlayer();
        if (this.isBypass(player)) {
            return;
        }
        Block block = event.getBlockClicked();
        if (block == null) {
            return;
        }

        PlayerTeam playerTeam = this.teamHandler.getPlayerTeamByPlayer(player);
        Team blockTeam = this.teamHandler.getTeamByLocation(block.getLocation());
        if (blockTeam == null) {
            return;
        }

        if (blockTeam instanceof PlayerTeam) {
            PlayerTeam playerBlockTeam = (PlayerTeam) blockTeam;
            if (playerBlockTeam.isRaidable()) {
                return;
            }
            if (playerTeam != null && playerBlockTeam.getId().equals(playerTeam.getId())) {
                return;
            }
        }

        event.setCancelled(true);
    }

    private boolean isBypass(Player player) {
        return HCF.getInstance().getBorderHandler().isBypass(player);
    }
}
