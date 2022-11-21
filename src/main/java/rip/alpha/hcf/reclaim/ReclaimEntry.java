package rip.alpha.hcf.reclaim;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.HCF;

import java.util.Set;

public class ReclaimEntry {

    private final Set<String> commands;

    public ReclaimEntry(Set<String> commands){
        this.commands = commands;
    }

    public void giveToPlayer(String playerName){
        HCF instance = HCF.getInstance();
        Server server = instance.getServer();
        CommandSender sender = server.getConsoleSender();
        for (String command : this.commands) {
            server.dispatchCommand(sender, String.format(command, playerName));
        }
    }
}
