package rip.alpha.hcf.listener;

import net.mcscrims.libraries.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;

public class SignTeleportListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignCreate(SignChangeEvent event) {
        String[] lines = event.getLines();
        if (lines.length < 2) {
            return;
        }
        if (!lines[0].equalsIgnoreCase("[Teleport]")) {
            return;
        }
        String worldName = lines[1];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return;
        }

        String locationString = lines[2];

        event.setLine(0, CC.GOLD + "[Teleport]");
        event.setLine(1, CC.BLACK + worldName.toLowerCase());
        event.setLine(2, CC.BLACK + locationString);
        event.setLine(3, "");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (!(block.getState() instanceof Sign)) {
            return;
        }
        Sign sign = (Sign) block.getState();
        String[] lines = sign.getLines();
        if (!lines[0].replace(" ", "").equalsIgnoreCase(CC.GOLD + "[Teleport]")) {
            return;
        }

        String worldName = lines[1];
        World world = Bukkit.getWorld(ChatColor.stripColor(worldName));
        if (world == null) {
            return;
        }

        String locationString = lines[2];
        String[] locationStringArray = locationString.split(",");
        double x = Double.parseDouble(ChatColor.stripColor(locationStringArray[0]));
        double y = Double.parseDouble(ChatColor.stripColor(locationStringArray[1]));
        double z = Double.parseDouble(ChatColor.stripColor(locationStringArray[2]));

        Location teleportLocation = new Location(world, x, y, z);

        Player player = event.getPlayer();
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (!teamProfile.isNotBlockGlitch()) {
            return;
        }
        Location location = block.getLocation();
        if (player.getLocation().distanceSquared(location) > 16) {
            return;
        }

        player.teleport(teleportLocation);
    }
}
