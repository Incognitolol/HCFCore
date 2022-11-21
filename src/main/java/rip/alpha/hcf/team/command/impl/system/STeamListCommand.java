package rip.alpha.hcf.team.command.impl.system;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.PageUtil;
import net.mcscrims.libraries.util.SimpleText;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.SystemTeam;

import java.util.ArrayList;
import java.util.List;

public class STeamListCommand {
    @Command(names = {"steam list", "steam steam", "steam list", "steam list"}, async = true)
    public static void steamListCommand(CommandSender sender, @Param(name = "page", defaultValue = "1") int page) {
        int pageIndex = page - 1;

        if (page < 0) {
            sender.sendMessage(CC.RED + "That is an invalid page.");
            return;
        }

        TeamHandler teamHandler = HCF.getInstance().getTeamHandler();
        List<SystemTeam> list = new ArrayList<>(teamHandler.getSystemTeams());
        list.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()));
        int maxPages = PageUtil.amountOfPages(list, 10);

        if (pageIndex > maxPages) {
            sender.sendMessage(CC.RED + "That is an invalid page.");
            return;
        }

        maxPages += 1;
        list = PageUtil.createPage(list, 10, pageIndex);

        List<SimpleText> message = new ArrayList<>();
        message.add(new SimpleText("&7&m---------------------------------------"));
        message.add(new SimpleText("&6System Team List &7(Page " + page + "/" + maxPages + ")"));
        for (int i = 0; i < Math.min(10, list.size()); i++) {
            int index = i + 1;
            SystemTeam team = list.get(i);
            message.add(new SimpleText("&7" + index + "." + " " + team.getDisplayName(sender)).click("/team who " + team.getName()).hover("&7Click to view this teams information."));
        }
        message.add(new SimpleText("&7You are currently on page &r" + page + "/" + maxPages + "&7."));
        message.add(new SimpleText("&7To view other pages, use &e/t list <page#>"));
        message.add(new SimpleText("&7&m---------------------------------------"));
        message.forEach(simpleText -> simpleText.send(sender));
    }
}
