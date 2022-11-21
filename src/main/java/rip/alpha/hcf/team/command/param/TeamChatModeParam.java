package rip.alpha.hcf.team.command.param;

import net.mcscrims.command.param.IParameter;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamChatModeParam implements IParameter<PlayerTeam.TeamChatMode> {

    private final Set<String> tabComplete = new HashSet<>();

    public TeamChatModeParam() {
        for (PlayerTeam.TeamChatMode value : PlayerTeam.TeamChatMode.values()) {
            tabComplete.addAll(Arrays.asList(value.getCommandPrefix()));
        }
    }

    @Override
    public PlayerTeam.TeamChatMode transform(CommandSender commandSender, String string) {
        PlayerTeam.TeamChatMode chatMode = PlayerTeam.TeamChatMode.getByCommandPrefix(string);
        if (chatMode == null) {
            return PlayerTeam.TeamChatMode.PUBLIC;
        }
        return PlayerTeam.TeamChatMode.getByCommandPrefix(string);
    }

    @Override
    public List<String> getTabComplete(CommandSender commandSender) {
        return new ArrayList<>(this.tabComplete);
    }
}
