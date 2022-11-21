package rip.alpha.hcf.game.listener;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.ItemBuilder;
import net.mcscrims.libraries.util.cuboid.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.game.CapturableGame;
import rip.alpha.hcf.game.Game;
import rip.alpha.hcf.game.GameHandler;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.impl.SystemTeam;

import java.util.Arrays;
import java.util.UUID;

@RequiredArgsConstructor
public class GameClaimListener implements Listener {

    public static final ItemStack GAME_CLAIM_WAND =
            new ItemBuilder(Material.DIAMOND_HOE)
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

    private final GameHandler gameHandler;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) {
            return;
        }
        ItemStack itemStack = event.getItem();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }
        if (!itemStack.isSimilar(GAME_CLAIM_WAND)) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isOp() && !player.hasPermission("game.claim")) {
            return;
        }
        event.setCancelled(true);
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (teamProfile == null) {
            return;
        }
        UUID gameId = teamProfile.getClaimingForGame();
        if (gameId == null) {
            return;
        }
        Game game = this.gameHandler.getGameById(gameId);
        if (!(game instanceof CapturableGame)) {
            return;
        }
        CapturableGame capturableGame = (CapturableGame) game;

        if (event.getAction() == Action.LEFT_CLICK_AIR && player.isSneaking()) {
            Location[] selectedLocations = teamProfile.getSelectedLocations();
            if (selectedLocations[0] == null || selectedLocations[1] == null) {
                return;
            }
            Cuboid cuboid = new Cuboid(selectedLocations[0], selectedLocations[1]);
            capturableGame.setCaptureCuboid(cuboid);
            teamProfile.setClaimingForGame(null);
            teamProfile.setSelectedLocations(new Location[2]);
            player.sendMessage(CC.GREEN + "You have setup the capzone for that game");
            return;
        }

        if (!event.hasBlock()) {
            return;
        }

        Block block = event.getClickedBlock();
        Location location = block.getLocation();
        SystemTeam systemTeam = capturableGame.getOwningTeam();

        if (systemTeam == null || systemTeam.getClaim() == null || !systemTeam.getClaim().contains(location)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            teamProfile.getSelectedLocations()[0] = location;
            player.sendMessage(CC.GREEN + "First location set");
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            teamProfile.getSelectedLocations()[1] = location;
            player.sendMessage(CC.GREEN + "Second location set");
            return;
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop() == null) {
            return;
        }
        if (event.getItemDrop().getItemStack() == null) {
            return;
        }
        if (!event.getItemDrop().getItemStack().isSimilar(GAME_CLAIM_WAND)) {
            return;
        }
        event.getItemDrop().remove();
    }

}
