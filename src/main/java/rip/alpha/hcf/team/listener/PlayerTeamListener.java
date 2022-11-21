package rip.alpha.hcf.team.listener;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.event.LCPlayerRegisterEvent;
import com.lunarclient.bukkitapi.nethandler.shared.LCPacketWaypointRemove;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import net.mcscrims.basic.Basic;
import net.mcscrims.basic.profile.BasicProfile;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.DamagerUtils;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.settings.Setting;
import rip.alpha.hcf.pvpclass.impl.ArcherClass;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.event.player.TeamFocusEvent;
import rip.alpha.hcf.team.event.player.TeamFocusRemoveEvent;
import rip.alpha.hcf.team.event.player.TeamInviteExpireEvent;
import rip.alpha.hcf.team.event.player.TeamJoinEvent;
import rip.alpha.hcf.team.event.player.TeamPreLeaveEvent;
import rip.alpha.hcf.team.event.player.TeamRallyExpireEvent;
import rip.alpha.hcf.team.event.player.TeamRallySetEvent;
import rip.alpha.hcf.team.event.player.TeamSetHomeEvent;
import rip.alpha.hcf.team.event.shared.TeamDisbandEvent;
import rip.alpha.hcf.team.event.shared.TeamRemoveClaimEvent;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.timer.impl.PvPTimer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerTeamListener implements Listener {

    private final TeamHandler teamHandler;
    private final Set<BlockFace> blockFaces;

    public PlayerTeamListener(TeamHandler teamHandler) {
        this.teamHandler = teamHandler;
        this.blockFaces = new HashSet<>();
        this.blockFaces.add(BlockFace.EAST);
        this.blockFaces.add(BlockFace.WEST);
        this.blockFaces.add(BlockFace.SOUTH);
        this.blockFaces.add(BlockFace.NORTH);
        this.blockFaces.add(BlockFace.SELF);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Player damager = DamagerUtils.getDamager(event);
            if (damager == null) {
                return;
            }
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                return;
            }
            PlayerTeam damagedTeam = this.teamHandler.getPlayerTeamByPlayer(player);
            PlayerTeam damagerTeam = this.teamHandler.getPlayerTeamByPlayer(damager);
            if (!(damagedTeam != null && damagerTeam != null && damagerTeam.getId().equals(damagedTeam.getId()))) {
                return;
            }
            TeamProfile shooterProfile = HCF.getInstance().getProfileHandler().getProfile(damager);
            if (shooterProfile.isActiveClass(ArcherClass.class)) {
                ItemStack itemStack = damager.getItemInHand();
                if (itemStack.getType() == Material.BOW && HCF.getInstance().getCrateHandler().isKothItem(itemStack)) {
                    if (HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(damager).getMember(player.getUniqueId()) != null && player.getHealth() < 20) {
                        player.setHealth(player.getHealth() + 1);
                        damager.sendMessage(CC.GRAY + "You have healed your teammate!");
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            event.setCancelled(true);
            damager.sendMessage(CC.GRAY + "You may not damage your teammates.");
        }
    }

    private final String overworldId = Bukkit.getWorlds().get(0).getUID().toString();

    @EventHandler
    public void onLCPlayerRegister(LCPlayerRegisterEvent event) {
        Player player = event.getPlayer();

        this.sendSpawnPacket(player, "", "world");
        this.sendSpawnPacket(player, "Nether ", "world_nether");

        PlayerTeam playerTeam = this.teamHandler.getPlayerTeamByPlayer(player);
        if (playerTeam == null) {
            return;
        }
        this.handleAddLCPackets(player, playerTeam);
    }

    @EventHandler
    public void onTeamJoin(TeamJoinEvent event) {
        Player player = event.getPlayer();
        PlayerTeam playerTeam = (PlayerTeam) event.getTeam();
        this.handleAddLCPackets(player, playerTeam);
    }

    @EventHandler
    public void onTeamPreLeave(TeamPreLeaveEvent event) {
        Player player = event.getPlayer();
        PlayerTeam playerTeam = (PlayerTeam) event.getTeam();
        this.handleRemoveLCPackets(player, playerTeam);
    }

    @EventHandler
    public void onTeamDisband(TeamDisbandEvent event) {
        if (!(event.getTeam() instanceof PlayerTeam)) {
            return;
        }

        PlayerTeam playerTeam = (PlayerTeam) event.getTeam();

        for (PlayerTeam.TeamMember member : playerTeam.getOnlineMembers()) {
            Player onlineMember = member.toPlayer();
            this.handleRemoveLCPackets(onlineMember, playerTeam);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Team team = HCF.getInstance().getTeamHandler().getTeamByLocation(player.getLocation());
        if (team instanceof PlayerTeam) {
            PlayerTeam playerTeam = (PlayerTeam) team;
            ACuboid cuboid = playerTeam.getClaim();
            if (cuboid != null) {
                TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player.getUniqueId());
                if (teamProfile.hasTimer(PvPTimer.class)) {
                    if (cuboid.contains(player.getLocation())) {
                        HCF.getInstance().getTimerHandler().teleportToSafeLocation(player, team);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTeamRallySet(TeamRallySetEvent event) {
        PlayerTeam playerTeam = (PlayerTeam) event.getTeam();
        LCWaypoint waypoint = new LCWaypoint("Rally", playerTeam.getRally(), Color.ORANGE.getRGB(), false, true);
        LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove("Rally", playerTeam.getPreviousRallyWorldId());
        for (PlayerTeam.TeamMember member : playerTeam.getOnlineMembers()) {
            if (member == null) {
                continue;
            }
            Player player = member.toPlayer();
            if (player == null) {
                continue;
            }
            LunarClientAPI.getInstance().sendPacket(player, removePacket);
            LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
        }
    }

    @EventHandler
    public void onTeamRallyExpire(TeamRallyExpireEvent event) {
        PlayerTeam playerTeam = (PlayerTeam) event.getTeam();
        LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove("Rally", playerTeam.getPreviousRallyWorldId());
        for (PlayerTeam.TeamMember member : playerTeam.getOnlineMembers()) {
            if (member == null) {
                continue;
            }
            Player player = member.toPlayer();
            if (player == null) {
                continue;
            }
            LunarClientAPI.getInstance().sendPacket(player, removePacket);
        }
    }

    @EventHandler
    public void onTeamFocusEvent(TeamFocusEvent event) {
        PlayerTeam playerTeam = (PlayerTeam) event.getTeam();
        PlayerTeam targetTeam = event.getTargetTeam();

        if (targetTeam.getClaim() == null || targetTeam.getClaim() != null && targetTeam.getHome() == null) {
            return;
        }

        LCWaypoint focusTeamHomeWaypoint = new LCWaypoint("Focused Team", event.getTargetTeam().getHome(), Color.MAGENTA.getRGB(), true, true);

        LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove("Focused Team", overworldId);

        for (PlayerTeam.TeamMember member : playerTeam.getOnlineMembers()) {
            Player player = member.toPlayer();
            LunarClientAPI.getInstance().sendPacket(player, removePacket);
            LunarClientAPI.getInstance().sendWaypoint(player, focusTeamHomeWaypoint);
        }
    }

    @EventHandler
    public void onTeamFocusRemove(TeamFocusRemoveEvent event) {
        PlayerTeam playerTeam = (PlayerTeam) event.getTeam();
        LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove("Focused Team", overworldId);
        for (PlayerTeam.TeamMember member : playerTeam.getOnlineMembers()) {
            Player player = member.toPlayer();
            LunarClientAPI.getInstance().sendPacket(player, removePacket);
        }
    }

    @EventHandler
    public void onTeamSetHome(TeamSetHomeEvent event) {
        PlayerTeam playerTeam = (PlayerTeam) event.getTeam();
        LCWaypoint waypoint = new LCWaypoint(CC.DARK_GREEN + "Home", playerTeam.getHome(), Color.GREEN.getRGB(), false, true);
        LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove(CC.DARK_GREEN + "Home", overworldId);
        for (PlayerTeam.TeamMember member : playerTeam.getOnlineMembers()) {
            Player player = member.toPlayer();
            LunarClientAPI.getInstance().sendPacket(player, removePacket);
            LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
        }
    }

    @EventHandler
    public void onTeamRemoveClaim(TeamRemoveClaimEvent event) {
        Team team = event.getTeam();
        if (!(team instanceof PlayerTeam)) {
            return;
        }
        PlayerTeam playerTeam = (PlayerTeam) event.getTeam();
        LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove(CC.DARK_GREEN + "Home", overworldId);
        for (PlayerTeam.TeamMember member : playerTeam.getOnlineMembers()) {
            Player player = member.toPlayer();
            LunarClientAPI.getInstance().sendPacket(player, removePacket);
        }
    }

    @EventHandler
    public void onTeamInviteExpire(TeamInviteExpireEvent event) {
        PlayerTeam.TeamInviteEntry entry = event.getTeamInviteEntry();
        UUID uuid = entry.getUuid();
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        player.sendMessage(CC.RED + "Your team invite to " + event.getTeam().getName() + " has expired.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerTeam playerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
        PlayerTeam.TeamChatMode chatMode = PlayerTeam.TeamChatMode.PUBLIC;
        String message = event.getMessage();

        if (playerTeam != null) {
            PlayerTeam.TeamMember teamMember = playerTeam.getMember(uuid);
            if (teamMember != null) {
                chatMode = teamMember.getChatMode();

                char c = message.charAt(0);
                PlayerTeam.TeamChatMode prefixMode = PlayerTeam.TeamChatMode.getByChatPrefix(c);
                if (prefixMode != null) {
                    chatMode = prefixMode;
                    message = message.substring(1);
                }
            }
        }

        if (message.length() <= 0) {
            message = event.getMessage();
            chatMode = PlayerTeam.TeamChatMode.PUBLIC;
        }

        switch (chatMode) {
            case FACTION: {
                playerTeam.handleTeamChat(player, message);
                break;
            }

            case CAPTAIN: {
                playerTeam.handleCaptainChat(player, message);
                break;
            }

            case PUBLIC:
            default: {
                if (!event.isCancelled()) {
                    for (Player target : event.getRecipients()) {
                        TeamProfile targetProfile = HCF.getInstance().getProfileHandler().getProfile(target);
                        if (!targetProfile.getSetting(Setting.PUBLIC_CHATS)) {
                            continue;
                        }
                        String factionMessage = "";

                        if (targetProfile.getSetting(Setting.FACTION_PREFIX_CHAT)) {
                            factionMessage = playerTeam == null ? CC.translate("&6[&e-&6]") : CC.translate("&6[" + playerTeam.getDisplayName(target) + "&6]");
                        }

                        BasicProfile basicProfile = Basic.getInstance().getBasicProfileManager().getProfile(target);
                        if (basicProfile != null) {
                            if (basicProfile.getIgnoredPlayers().contains(player.getUniqueId())) {
                                continue;
                            }
                        }

                        target.sendMessage(factionMessage + String.format(event.getFormat(), player.getName(), message));
                    }
                }
            }
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        if (lines.length <= 1) {
            return;
        }
        if (!lines[0].equalsIgnoreCase("[Subclaim]")) {
            return;
        }

        Player player = event.getPlayer();
        PlayerTeam playerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
        Block block = event.getBlock();

        if (playerTeam == null) {
            block.breakNaturally();
            player.sendMessage(ChatColor.RED + "You are not on a team!");
            return;
        }

        ACuboid claim = playerTeam.getClaim();

        if (claim == null || !claim.contains(block)) {
            block.breakNaturally();
            player.sendMessage(ChatColor.RED + "This is not in your teams claim");
            return;
        }

        Sign sign = (Sign) block.getState();
        BlockFace attachedFace = ((org.bukkit.material.Sign) sign.getData()).getAttachedFace();
        Block relBlock = event.getBlock().getRelative(attachedFace);
        Material material = relBlock.getType();

        if (!(material.equals(Material.CHEST)) && !(material.equals(Material.TRAPPED_CHEST))) {
            block.breakNaturally();
            player.sendMessage(CC.RED + "Subclaims only work on chests.");
            return;
        }

        if (this.hasSubclaimSign(block)) {
            block.breakNaturally();
            player.sendMessage(ChatColor.RED + "This chest is already subclaimed!");
            return;
        }

        PlayerTeam.TeamMember teamMember = playerTeam.getMember(player.getUniqueId());

        if (!teamMember.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_CAPTAIN)) {
            block.breakNaturally();
            player.sendMessage(CC.RED + "You need to be a team captain to create a subclaim");
            return;
        }

        List<String> whitelisted = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line == null || line.isEmpty()) {
                continue;
            }
            int role = PlayerTeam.TeamMember.getRoleIdByName(line);

            if (role == -1) {
                UUID uuid = UUIDFetcher.getCachedUUID(line);
                if (uuid == null) {
                    continue;
                }
                String name = UUIDFetcher.getCachedName(uuid);
                if (name == null) {
                    continue;
                }
                whitelisted.add(name);
            } else {
                whitelisted.add(PlayerTeam.TeamMember.getRoleNameById(role));
            }
        }

        if (whitelisted.isEmpty()) {
            String name = player.getName();
            whitelisted.add(name);
            player.sendMessage(CC.GREEN + "You didnt add a player so we added your ourself!");
        }

        event.setLine(0, CC.BLUE + "[Subclaim]");
        for (int i = 0; i < Math.min(whitelisted.size(), 3); i++) {
            String line = whitelisted.get(i);
            line = line.substring(0, Math.min(15, line.length()));
            event.setLine(i + 1, line);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (HCF.getInstance().getBorderHandler().isBypass(player)) {
            return;
        }
        Block block = event.getBlock();
        Team team = HCF.getInstance().getTeamHandler().getTeamByLocation(block.getLocation());
        if (!(team instanceof PlayerTeam)) {
            return;
        }
        PlayerTeam playerTeam = (PlayerTeam) team;
        if (playerTeam.isRaidable()) {
            return;
        }
        PlayerTeam.TeamMember member = playerTeam.getMember(player.getUniqueId());
        if (member == null) {
            return;
        }

        if (block.getState() instanceof Chest) {
            if (!this.hasSubclaimSign(block)) {
                return;
            }
            if (!member.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_CAPTAIN)) {
                player.sendMessage(CC.RED + "You have to be a team captain or above to break a subclaim block.");
                event.setCancelled(true);
            }
        } else if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (!(sign.getLine(0).equalsIgnoreCase(CC.BLUE + "[Subclaim]"))) {
                return;
            }
            if (!member.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_CAPTAIN)) {
                player.sendMessage(CC.RED + "You have to be a team captain or above to break a subclaim block.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (HCF.getInstance().getBorderHandler().isBypass(player)) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!(event.getClickedBlock().getState() instanceof Chest)) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Team team = HCF.getInstance().getTeamHandler().getTeamByLocation(block.getLocation());
        if (!(team instanceof PlayerTeam)) {
            return;
        }
        PlayerTeam playerTeam = (PlayerTeam) team;
        if (playerTeam.isRaidable()) {
            return;
        }

        Sign sign = this.getSubclaimSign(block);
        if (sign == null) {
            return;
        }

        PlayerTeam.TeamMember member = playerTeam.getMember(player.getUniqueId());
        if (member == null) {
            return;
        }
        if (member.isHigherOrEqual(PlayerTeam.TeamMember.TEAM_CAPTAIN)) {
            return;
        }
        if (!this.isAllowedToUseSubclaim(sign, member)) {
            player.sendMessage(CC.RED + "You do not have access to this subclaim.");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getSource().getType() != InventoryType.CHEST || event.getDestination().getType() != InventoryType.HOPPER) {
            return;
        }

        InventoryHolder inventoryHolder = event.getSource().getHolder();
        Block block = null;

        if (inventoryHolder instanceof DoubleChest) {
            block = ((DoubleChest) inventoryHolder).getLocation().getBlock();
        } else if (inventoryHolder instanceof BlockState) {
            block = ((BlockState) inventoryHolder).getBlock();
        }

        if (block != null && this.hasSubclaimSign(block)) {
            event.setCancelled(true);
        }
    }

    private boolean isAllowedToUseSubclaim(Sign sign, PlayerTeam.TeamMember member) {
        for (int i = 1; i < 4; i++) {
            String line = sign.getLine(i);
            int role = PlayerTeam.TeamMember.getRoleIdByName(line);
            if (role == -1) {
                if (member.getName().equalsIgnoreCase(line)) {
                    return true;
                }
            } else {
                if (member.isHigherOrEqual(role)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Sign getSubclaimSign(Block block) {
        for (BlockFace blockFace : this.blockFaces) {
            Block relBlock = block.getRelative(blockFace);
            if (relBlock.getType() == block.getType()) {
                for (BlockFace relFace : this.blockFaces) {
                    Block signBlock = relBlock.getRelative(relFace);
                    BlockState state = signBlock.getState();
                    if (!(state instanceof Sign)) {
                        continue;
                    }
                    Sign sign = (Sign) state;
                    BlockFace attachedFace = ((org.bukkit.material.Sign) sign.getData()).getAttachedFace();
                    if (!signBlock.getRelative(attachedFace).equals(relBlock)) {
                        continue; //check if its the same block u know
                    }
                    if (sign.getLine(0).equalsIgnoreCase(CC.BLUE + "[Subclaim]")) {
                        return sign;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasSubclaimSign(Block block) {
        return this.getSubclaimSign(block) != null;
    }

    private void sendSpawnPacket(Player player, String prefix, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return;
        }
        LCWaypoint waypoint = new LCWaypoint(CC.GREEN + prefix + "Spawn", world.getSpawnLocation(), Color.GREEN.getRGB(), false, true);
        LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove(CC.GREEN + prefix + "Spawn", world.getUID().toString());
        LunarClientAPI.getInstance().sendPacket(player, removePacket);
        LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
    }

    private void handleAddLCPackets(Player player, PlayerTeam playerTeam) {
        if (playerTeam.getClaim() != null && playerTeam.getHome() != null) {
            LCWaypoint waypoint = new LCWaypoint(CC.DARK_GREEN + "Home", playerTeam.getHome(), Color.GREEN.getRGB(), false, true);
            LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove(CC.DARK_GREEN + "Home", overworldId);
            LunarClientAPI.getInstance().sendPacket(player, removePacket);
            LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
        }

        if (playerTeam.getRally() != null) {
            LCWaypoint waypoint = new LCWaypoint("Rally", playerTeam.getRally(), Color.ORANGE.getRGB(), false, true);
            LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove("Rally", playerTeam.getPreviousRallyWorldId());
            LunarClientAPI.getInstance().sendPacket(player, removePacket);
            LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
        }

        if (playerTeam.getFocusedTeamId() != null) {
            PlayerTeam targetTeam = this.teamHandler.getPlayerTeamById(playerTeam.getFocusedTeamId());
            if (targetTeam.getHome() != null) {
                LCWaypoint focusTeamHomeWaypoint = new LCWaypoint("Focused Team", targetTeam.getHome(), Color.MAGENTA.getRGB(), true, true);

                LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove("Focused Team", overworldId);

                for (PlayerTeam.TeamMember member : playerTeam.getOnlineMembers()) {
                    Player onlineMember = member.toPlayer();
                    LunarClientAPI.getInstance().sendPacket(onlineMember, removePacket);
                    LunarClientAPI.getInstance().sendWaypoint(onlineMember, focusTeamHomeWaypoint);
                }
            }
        }
    }

    private void handleRemoveLCPackets(Player player, PlayerTeam playerTeam) {
        if (playerTeam.getClaim() != null && playerTeam.getHome() != null) {
            LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove(CC.DARK_GREEN + "Home", overworldId);
            LunarClientAPI.getInstance().sendPacket(player, removePacket);
        }

        if (playerTeam.getRally() != null) {
            LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove("Rally", playerTeam.getPreviousRallyWorldId());
            LunarClientAPI.getInstance().sendPacket(player, removePacket);
        }

        if (playerTeam.getFocusedTeamId() != null) {
            LCPacketWaypointRemove removePacket = new LCPacketWaypointRemove("Focused Team", overworldId);
            for (PlayerTeam.TeamMember member : playerTeam.getOnlineMembers()) {
                Player onlineMember = member.toPlayer();
                LunarClientAPI.getInstance().sendPacket(onlineMember, removePacket);
            }
        }
    }
}
