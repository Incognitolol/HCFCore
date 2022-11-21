package rip.alpha.hcf.elevator;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;

@RequiredArgsConstructor
public class ElevatorListener implements Listener {

    private final ElevatorHandler handler;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignCreate(SignChangeEvent event) {
        String[] lines = event.getLines();
        if (lines.length < 2) {
            return;
        }
        if (!lines[0].equalsIgnoreCase("[Elevator]")) {
            return;
        }
        String direction = lines[1];
        if (!(direction.equalsIgnoreCase("Up") || direction.equalsIgnoreCase("Down"))) {
            return;
        }
        event.setLine(0, CC.BLUE + "[Elevator]");
        event.setLine(1, CC.BLACK + StringUtils.capitalize(direction.toLowerCase()));
        event.setLine(2, "");
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
        if (!lines[0].replace(" ", "").equalsIgnoreCase(CC.BLUE + "[Elevator]")) {
            return;
        }
        String direction = CC.strip(lines[1]).replace(" ", "");
        if (!(direction.equalsIgnoreCase("Up") || direction.equalsIgnoreCase("Down"))) {
            return;
        }
        Player player = event.getPlayer();
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (!teamProfile.isNotBlockGlitch()) {
            return;
        }
        Location location = block.getLocation();
        if (player.getLocation().distanceSquared(location) > 16) {
            return;
        }

        if (direction.equalsIgnoreCase("up")) {
            this.handler.useElevatorUp(player, location);
        } else if (direction.equalsIgnoreCase("down")) {
            this.handler.useElevatorDown(player, location);
        }

        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);
    }
}
