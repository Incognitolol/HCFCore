package rip.alpha.hcf.kitmap;

import net.mcscrims.libraries.fake.impl.player.FakePlayerInteractEvent;
import net.mcscrims.libraries.util.CC;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.pvpclass.kits.Kit;
import rip.alpha.modsuite.ModSuiteMode;
import rip.alpha.modsuite.ModSuitePlugin;

import java.util.concurrent.TimeUnit;

public class KitmapListener implements Listener {
    @EventHandler
    public void onFakePlayerInteract(FakePlayerInteractEvent event) {
        if (!HCF.getInstance().getConfiguration().isKitmap()) {
            return;
        }

        Player player = event.getPlayer();

        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (profile == null) {
            return;
        }
        if (event.getCommand() == null) {
            return;
        }

        Kit kit = HCF.getInstance().getKitHandler().getKit(event.getCommand());
        if (kit == null) {
            return;
        }
        event.setCommand(null);

        // By the time this has been reached, it has been established that a kit exists & this NPC gives one
        ModSuiteMode mode = ModSuitePlugin.getInstance().getModSuiteAPI().getCurrentMode(player);
        if (mode != ModSuiteMode.NONE) {
            event.setCommand(null);
            player.sendMessage(CC.RED + "You cannot get kits while in staff mode.");
            return;
        }
        ;

        if (!profile.canEquipKit()) {
            player.sendMessage(CC.RED + "Please wait before using this again");
            return;
        }

        kit.giveKit(player);
        profile.setKitCooldown(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10));
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!HCF.getInstance().getConfiguration().isKitmap()) {
            return;
        }
        Chunk chunk = event.getChunk();
        for (Entity entity : chunk.getEntities()) {
            entity.remove();
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) {
            return;
        }
        String[] lines = event.getLines();
        if (lines.length < 2) {
            return;
        }
        if (!lines[0].equalsIgnoreCase("[Kit]")) {
            return;
        }
        Kit kit = HCF.getInstance().getKitHandler().getKit(lines[1]);
        if (kit == null) {
            return;
        }
        event.setLine(0, CC.GOLD + "[Kit]");
        event.setLine(1, CC.BLACK + StringUtils.capitalize(kit.getName()));
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
        if (!lines[0].replace(" ", "").equalsIgnoreCase(CC.GOLD + "[Kit]")) {
            return;
        }
        String kitName = CC.strip(lines[1]).replace(" ", "");
        Kit kit = HCF.getInstance().getKitHandler().getKit(kitName);
        Player player = event.getPlayer();
        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (!teamProfile.isNotBlockGlitch()) {
            return;
        }

        ModSuiteMode mode = ModSuitePlugin.getInstance().getModSuiteAPI().getCurrentMode(player);
        if (mode != ModSuiteMode.NONE) {
            player.sendMessage(CC.RED + "You cannot get kits while in staff mode.");
            return;
        }
        ;

        if (!teamProfile.canEquipKit()) {
            player.sendMessage(CC.RED + "Please wait before using this again");
            return;
        }

        kit.giveKit(player);
        teamProfile.setKitCooldown(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10));
    }
}
