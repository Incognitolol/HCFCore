package rip.alpha.hcf.team.command.impl.player;

import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.PageUtil;
import net.mcscrims.libraries.util.SimpleText;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamHelpCommand {

    private static final List<String> HELP_INFO = Arrays.asList(
            CC.GRAY + "Usage: /t create <name>",
            CC.GRAY + "Usage: /t invite <player>",
            CC.GRAY + "Usage: /t uninvite <player>",
            CC.GRAY + "Usage: /t join <team>",
            CC.GRAY + "Usage: /t claim",
            CC.GRAY + "Usage: /t stuck",
            CC.GRAY + "Usage: /t map",
            CC.GRAY + "Usage: /t unclaim",
            CC.GRAY + "Usage: /t leave",
            CC.GRAY + "Usage: /t disband",
            CC.GRAY + "Usage: /t rally",
            CC.GRAY + "Usage: /t kick <player>",
            CC.GRAY + "Usage: /t demote <player>",
            CC.GRAY + "Usage: /t promote <player>",
            CC.GRAY + "Usage: /t coleader <player>",
            CC.GRAY + "Usage: /t captain <player>",
            CC.GRAY + "Usage: /t leader <player>",
            CC.GRAY + "Usage: /t sethome",
            CC.GRAY + "Usage: /t home",
            CC.GRAY + "Usage: /t withdraw <amount>",
            CC.GRAY + "Usage: /t deposit <amount>",
            CC.GRAY + "Usage: /t invites",
            CC.GRAY + "Usage: /t list",
            CC.GRAY + "Usage: /t announcement <announcement>",
            CC.GRAY + "Usage: /t removeannouncement",
            CC.GRAY + "Usage: /t focus <team>",
            CC.GRAY + "Usage: /t unfocus <team>",
            CC.GRAY + "Usage: /t rename <name>",
            CC.GRAY + "Usage: /t chat <type>",
            CC.GRAY + "Usage: /t info <team>");

    @Command(names = {"f", "t", "faction", "team", "f help", "t help", "team help", "faction help"}, async = true)
    public static void teamHelp(Player player, @Param(name = "page", defaultValue = "1") int page) {
        int pageIndex = page - 1;

        if (page < 0) {
            player.sendMessage(CC.RED + "That is an invalid page.");
            return;
        }

        List<String> infoList = new ArrayList<>(HELP_INFO);

        int maxPages = PageUtil.amountOfPages(infoList, 10);

        if (pageIndex > maxPages) {
            player.sendMessage(CC.RED + "That is an invalid page.");
            return;
        }

        maxPages += 1;
        infoList = PageUtil.createPage(infoList, 10, pageIndex);

        List<SimpleText> message = new ArrayList<>();
        message.add(new SimpleText("&7&m---------------------------------------"));
        message.add(new SimpleText("&6Team Help &7(Page " + page + "/" + maxPages + ")"));
        for (int i = 0; i < Math.min(10, infoList.size()); i++) {
            String usage = infoList.get(i);
            message.add(new SimpleText(usage));
        }

        message.add(new SimpleText("&7You are currently on page &r" + page + "/" + maxPages + "&7."));
        message.add(new SimpleText("&7To view other pages, use &e/t list <page#>"));
        message.add(new SimpleText("&7&m---------------------------------------"));
        message.forEach(simpleText -> simpleText.send(player));
    }
}
