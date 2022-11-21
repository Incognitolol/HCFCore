package rip.alpha.hcf.team.command.param;

import lombok.RequiredArgsConstructor;
import net.mcscrims.command.param.IParameter;
import net.mcscrims.libraries.util.CC;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.SystemTeam;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SystemTeamParam implements IParameter<SystemTeam> {

    private final TeamHandler teamHandler;

    @Override
    public SystemTeam transform(CommandSender commandSender, String s) {
        SystemTeam team = this.teamHandler.getSystemTeamByName(s.replace("-", " "));

        if (team == null) {
            commandSender.sendMessage(CC.RED + "That system team doesn't exist");
            return null;
        }

        return team;
    }

    @Override
    public List<String> getTabComplete(CommandSender commandSender) {
        List<String> tabList = new ArrayList<>();
        this.teamHandler.getSystemTeams().forEach(team -> tabList.add(team.getName().replace(" ", "-")));
        return tabList;
    }
}
