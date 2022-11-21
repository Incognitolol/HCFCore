package rip.alpha.hcf.visual;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.io.netty.util.internal.ConcurrentSet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class VisualListener implements Listener {

    private final VisualHandler handler;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.handler.getVisualBlocks().put(event.getPlayer().getUniqueId(), new ConcurrentSet<>());
        this.handler.getLunarWallMap().put(event.getPlayer().getUniqueId(), new ConcurrentSet<>());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.handler.getVisualBlocks().remove(event.getPlayer().getUniqueId());
        this.handler.getLunarWallMap().remove(event.getPlayer().getUniqueId());
    }
}
