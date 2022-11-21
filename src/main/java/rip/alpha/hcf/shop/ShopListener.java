package rip.alpha.hcf.shop;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.fake.impl.player.FakePlayerInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class ShopListener implements Listener {

    private final ShopHandler shopHandler;

    @EventHandler
    public void onFakePlayerInteract(FakePlayerInteractEvent event) {
        Player player = event.getPlayer();
        String command = event.getCommand();
        if (command != null) {
            if (command.equalsIgnoreCase("sell")) {
                this.shopHandler.openSellMenu(player);
                event.setCommand(null);
            } else if (command.equalsIgnoreCase("buy")) {
                this.shopHandler.openBuyMenu(player);
                event.setCommand(null);
            } else if (command.equalsIgnoreCase("spawner")) {
                this.shopHandler.openSpawnerMenu(player);
                event.setCommand(null);
            }
        }
    }

}
