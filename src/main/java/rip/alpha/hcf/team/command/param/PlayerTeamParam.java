package rip.alpha.hcf.team.command.param;

import lombok.RequiredArgsConstructor;
import net.mcscrims.command.param.IParameter;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.command.TeamCommandConstants;
import rip.alpha.hcf.team.impl.PlayerTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class PlayerTeamParam implements IParameter<PlayerTeam> {

    private final TeamHandler teamHandler;

    @Override
    public PlayerTeam transform(CommandSender sender, String name) {
        if (sender instanceof Player && name.equalsIgnoreCase("self")) {
            Player player = (Player) sender;
            PlayerTeam team = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
            if (team == null) {
                sender.sendMessage(CC.RED + "You are not in a team");
            }
            return team;
        }

        PlayerTeam playerTeam = null;

        if (name.length() <= 16 && name.length() >= 3 && !TeamCommandConstants.ALPHANUMERIC_PATTERN.matcher(name).find()) {
            playerTeam = this.teamHandler.getPlayerTeamByName(name);
        }

        if (playerTeam == null) {
            UUID uuid = UUIDFetcher.getCachedUUID(name);
            if (uuid != null) {
                PlayerTeam team = this.teamHandler.getPlayerTeamByPlayer(uuid);
                if (team != null) {
                    return team;
                }
            }

            sender.sendMessage(CC.RED + "There was no team found with that name");
            return null;
        }

        return playerTeam;
    }

    @Override
    public List<String> getTabComplete(CommandSender commandSender) {
        List<String> tabList = new ArrayList<>();
        this.teamHandler.getPlayerTeams().forEach(playerTeam -> tabList.add(playerTeam.getName()));
        Bukkit.getOnlinePlayers().forEach(player -> tabList.add(player.getName()));
        return tabList;
    }
}
