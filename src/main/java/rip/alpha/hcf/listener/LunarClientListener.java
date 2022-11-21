package rip.alpha.hcf.listener;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.event.LCPlayerRegisterEvent;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketServerRule;
import com.lunarclient.bukkitapi.nethandler.client.obj.ServerRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import rip.alpha.hcf.HCF;

import java.util.HashSet;
import java.util.Set;

public class LunarClientListener implements Listener {

    private final Set<ServerRule> enabledServerRules;

    public LunarClientListener() {
        this.enabledServerRules = new HashSet<>();
        this.enabledServerRules.add(ServerRule.LEGACY_COMBAT);
        if (!HCF.getInstance().getConfiguration().isKitmap()) {
            this.enabledServerRules.add(ServerRule.COMPETITIVE_GAME);
        }
    }

    @EventHandler
    public void onLCPlayerRegister(LCPlayerRegisterEvent event) {
        Player player = event.getPlayer();
        for (ServerRule rule : this.enabledServerRules) {
            LCPacketServerRule serverRulePacket = new LCPacketServerRule(rule, true);
            LunarClientAPI.getInstance().sendPacket(player, serverRulePacket);
        }
    }
}
