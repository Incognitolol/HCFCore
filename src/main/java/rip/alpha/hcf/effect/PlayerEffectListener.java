package rip.alpha.hcf.effect;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.UUID;

@RequiredArgsConstructor
public class PlayerEffectListener implements Listener {

    private final PlayerEffectHandler playerEffectHandler;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        this.playerEffectHandler.getRestoreEntries().put(uuid, new HashSet<>());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        for (PlayerEffectRestoreEntry entry : this.playerEffectHandler.getRestoreEntries(uuid)) {
            this.playerEffectHandler.restorePotionEffect(player, entry);
        }
        this.playerEffectHandler.getRestoreEntries().remove(uuid);
    }
}
