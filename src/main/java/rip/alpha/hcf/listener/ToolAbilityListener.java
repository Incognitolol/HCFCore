package rip.alpha.hcf.listener;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.mcscrims.libraries.util.TaskUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.*;

public class ToolAbilityListener implements Listener {
    private final Int2ObjectArrayMap<Set<BlockFace>> faces = new Int2ObjectArrayMap<>();

    static {

    }

    public ToolAbilityListener(){
        faces.put(1, ImmutableSet.of(BlockFace.NORTH_WEST, BlockFace.NORTH,
                BlockFace.NORTH_EAST,
                BlockFace.WEST,
                BlockFace.EAST,
                BlockFace.SOUTH_WEST,
                BlockFace.SOUTH, BlockFace.SOUTH_EAST));
        faces.put(2, ImmutableSet.of(BlockFace.UP_WEST, BlockFace.UP,
                BlockFace.UP_EAST,
                BlockFace.WEST,
                BlockFace.EAST,
                BlockFace.DOWN_WEST,
                BlockFace.DOWN, BlockFace.DOWN_EAST));

        faces.put(3, ImmutableSet.of(BlockFace.UP_NORTH, BlockFace.UP,
                BlockFace.UP_SOUTH,
                BlockFace.NORTH,
                BlockFace.SOUTH,
                BlockFace.DOWN_NORTH,
                BlockFace.DOWN, BlockFace.DOWN_SOUTH));
    }

    @EventHandler
    public void getBlockFace(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getItemInHand();
        BlockFace blockFace = event.getBlockFace();
        if (itemInHand.getType().equals(Material.GOLD_PICKAXE)) {
            TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player.getUniqueId());

            if (blockFace == BlockFace.UP || blockFace == BlockFace.DOWN) {
                profile.setBlockFaceType(1);
            }else if (blockFace == BlockFace.NORTH || blockFace == BlockFace.SOUTH) {
                profile.setBlockFaceType(2);
            }else if (blockFace == BlockFace.WEST || blockFace == BlockFace.EAST) {
                profile.setBlockFaceType(3);
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlock();

        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand.getType() == Material.GOLD_PICKAXE && HCF.getInstance().getCrateHandler().isKothItem(itemInHand)) {
            if (block.getType() == Material.STONE) {
                this.breakBlock(block,player);
            }
        }

        if (itemInHand.getType() == Material.DIAMOND_SPADE && HCF.getInstance().getCrateHandler().isKothItem(itemInHand)) {
            this.breakBlock(block, player);
        }
    }

    private void breakBlock(Block block, Player player){
        TaskUtil.runAsync(() -> {
            TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
            Set<BlockFace> faces = this.faces.get(teamProfile.getBlockFaceType());
            for (BlockFace blockFace : faces) {
                Block blockToChange = block.getRelative(blockFace);
                if (blockToChange.getType() != Material.STONE) continue;
                Team team = HCF.getInstance().getTeamHandler().getTeamByLocation(blockToChange.getLocation());
                if (team != null) {
                    if (team instanceof PlayerTeam) {
                        PlayerTeam playerTeam = (PlayerTeam) team;
                        if (playerTeam.getMember(player.getUniqueId()) == null) {
                            continue;
                        }
                    }
                    continue;
                }
                TaskUtil.runSync(blockToChange::breakNaturally, HCF.getInstance());
            }
        }, HCF.getInstance());
    }
}
