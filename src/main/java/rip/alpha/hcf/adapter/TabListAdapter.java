package rip.alpha.hcf.adapter;

import net.mcscrims.libraries.Libraries;
import net.mcscrims.libraries.skin.MojangSkin;
import net.mcscrims.libraries.tablist.TabListLayout;
import net.mcscrims.libraries.tablist.TabListProvider;
import net.mcscrims.libraries.util.BalanceUtil;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.PlayerDirection;
import net.mcscrims.libraries.util.RoundingUtil;
import net.mcscrims.libraries.util.cuboid.Cuboid;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.HCFConfiguration;
import rip.alpha.hcf.enchantment.EnchantmentHandler;
import rip.alpha.hcf.game.CapturableGame;
import rip.alpha.hcf.game.Game;
import rip.alpha.hcf.game.GameHandler;
import rip.alpha.hcf.game.schedule.GameScheduleEntry;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.statistics.ProfileStatTypes;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.impl.SystemTeam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TabListAdapter implements TabListProvider {

    @Override
    public String getHeader(Player player) {
        return CC.B_GOLD + "Alpha Network";
    }

    @Override
    public String getFooter(Player player) {
        return CC.RED + "store.alpha.rip";
    }

    @Override
    public TabListLayout getLayout(Player player, boolean v1_8) {
        return this.createDefaultLayout(player, v1_8);
    }

    @Override
    public int getUpdateInterval() {
        return 250;
    }

    private TabListLayout createDefaultLayout(Player player, boolean v1_8) {
        TabListLayout tabListLayout = new TabListLayout(v1_8);

        if (!v1_8) {
            tabListLayout.put(1, 0, this.getHeader(player));
        }
        int startRow = v1_8 ? 0 : 2;

        TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
        PlayerTeam playerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
        String homeLine = "&eNone";

        if (playerTeam != null) {
            Location home = playerTeam.getHome();
            if (playerTeam.getClaim() != null && home != null) {
                homeLine = "&eX: " + home.getBlockX() + " Z:" + home.getBlockZ();
            }
        }

        tabListLayout.put(0, startRow, "&6Faction Home");
        tabListLayout.put(0, startRow + 1, homeLine);

        String teamOnlineLine = "&eYou do not";
        String teamDTRLine = "&ehave a faction";
        String teamBalanceLine = "&e/f create <name>";

        if (playerTeam != null) {
            teamOnlineLine = "&eOnline: " + playerTeam.getOnlineMembers().size();
            teamDTRLine = "&eDTR: " + playerTeam.getDTRSymbol() + playerTeam.getDTRColor() + RoundingUtil.round(playerTeam.getDtr(), 2);
            teamBalanceLine = "&eBalance: $" + BalanceUtil.formatBalance(playerTeam.getBalance());
        }

        tabListLayout.put(0, startRow + 3, "&6Faction Info");
        tabListLayout.put(0, startRow + 4, teamOnlineLine);
        tabListLayout.put(0, startRow + 5, teamDTRLine);
        tabListLayout.put(0, startRow + 6, teamBalanceLine);

        tabListLayout.put(0, startRow + 8, "&6Player Info");
        tabListLayout.put(0, startRow + 9, "&eKills: " + teamProfile.getStat(ProfileStatTypes.KILLS));
        tabListLayout.put(0, startRow + 10, "&eDeaths: " + teamProfile.getStat(ProfileStatTypes.DEATHS));
        tabListLayout.put(0, startRow + 11, "&eBalance: $" + BalanceUtil.formatBalance(teamProfile.getBalance()));

        Location location = player.getLocation();
        String claimLine = HCF.getInstance().getBorderHandler().inWarzone(location) ? "&cWarzone" : "&aWilderness";

        Team team = teamProfile.getLastClaimTeam();
        if (team != null) {
            String color = CC.YELLOW;
            if (team instanceof SystemTeam) {
                SystemTeam systemTeam = (SystemTeam) team;
                color = systemTeam.getColor();
            }

            claimLine = color + team.getName();
        }

        String direction = PlayerDirection.getCardinalDirection(player);
        String locationLine = "&e" + location.getBlockX() + ", " + location.getBlockZ() + " [" + direction + "]";

        tabListLayout.put(0, startRow + 13, "&6Location");
        tabListLayout.put(0, startRow + 14, claimLine);
        tabListLayout.put(0, startRow + 15, locationLine);

        if (playerTeam != null) {
            List<PlayerTeam.TeamMember> sortedMembers = playerTeam.getOnlineMembers()
                    .stream()
                    .sorted(Comparator.comparingInt(value -> -value.getRole()))
                    .collect(Collectors.toList());

            tabListLayout.put(1, startRow, CC.GREEN + playerTeam.getName());

            for (int i = 0; i < 20; i++) {
                int index = i + startRow + 1;
                if (index >= 20) {
                    break;
                }
                String name = "";
                MojangSkin skin = null;

                if (!(i >= sortedMembers.size())) {
                    PlayerTeam.TeamMember member = sortedMembers.get(i);
                    name = CC.DARK_GREEN + member.getName() + member.getPrefix();
                    skin = Libraries.getInstance().getMojangSkinHandler().getMojangSkinFromPlayer(member.toPlayer());
                }

                tabListLayout.put(1, index, name, skin);
            }
        }

        EnchantmentHandler enchantmentHandler = HCF.getInstance().getEnchantmentHandler();
        int protection = enchantmentHandler.getMaxProtection();
        int sharpness = enchantmentHandler.getMaxSharpness();
        tabListLayout.put(2, startRow, "&6Map Kit");
        tabListLayout.put(2, startRow + 1, "&eProt " + protection + " Sharp " + sharpness);


        HCFConfiguration hcfConfiguration = HCF.getInstance().getConfiguration();
        int endPortalX = hcfConfiguration.getEndPortalX();
        int endPortalZ = hcfConfiguration.getEndPortalZ();

        tabListLayout.put(2, startRow + 3, "&6Border");
        tabListLayout.put(2, startRow + 4, "&e" + hcfConfiguration.getWorldBorder());

        tabListLayout.put(2, startRow + 6, "&6End Portals");
        tabListLayout.put(2, startRow + 7, "&eX: " + endPortalX + ", Z: " + endPortalZ);

        GameHandler gameHandler = HCF.getInstance().getGameHandler();
        boolean kothActive = false;

        for (Game game : HCF.getInstance().getGameHandler().getGames()) {
            if (game.isActive() && !game.isCompleted()) {
                kothActive = true;
                break;
            }
        }

        if (kothActive) {
            tabListLayout.put(2, startRow + 9, "&6Active Events");
            List<Game> games = new ArrayList<>(HCF.getInstance().getGameHandler().getGames());
            games.removeIf(game -> game.getName() == null);
            games.removeIf(game -> !game.isActive());

            games.removeIf(game -> {
                if (game instanceof CapturableGame) {
                    CapturableGame capturableGame = (CapturableGame) game;
                    return capturableGame.getCaptureCuboid() == null;
                }
                return false;
            });

            games.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()));
            int i = startRow + 10;
            for (Game game : games) {
                if (i > 20) {
                    break;
                }
                String entry = game.getColor() + game.getName();

                if (game instanceof CapturableGame) {
                    CapturableGame capturableGame = (CapturableGame) game;
                    Cuboid cuboid = capturableGame.getCaptureCuboid();
                    Location center = cuboid.getCenter();
                    entry += " &e(" + center.getBlockX() + ", " + center.getBlockZ() + ")";
                }

                tabListLayout.put(2, i, entry);
                i++;
            }
        } else {
            tabListLayout.put(2, startRow + 9, "&6Upcoming Event");
            GameScheduleEntry gameScheduleEntry = gameHandler.getGameScheduleHandler().getNextScheduledGame();
            if (gameScheduleEntry != null) {
                tabListLayout.put(2, startRow + 10, "&eEvent: " + gameScheduleEntry.getColorName());
                tabListLayout.put(2, startRow + 11, CC.YELLOW + CC.strip(gameScheduleEntry.formatDateTime()));
            } else {
                tabListLayout.put(2, startRow + 10, "&eNone");
            }
        }

        if (v1_8) {
            //TODO add faction list or something, or just top players IDK
        }

        return tabListLayout;
    }
}
