package rip.alpha.hcf.adapter;

import net.mcscrims.libraries.board.BoardProvider;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.RoundingUtil;
import net.mcscrims.libraries.util.TimeUtil;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.game.Game;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.TeamProfileHandler;
import rip.alpha.hcf.profile.settings.Setting;
import rip.alpha.hcf.profile.statistics.ProfileStatTypes;
import rip.alpha.hcf.pvpclass.PvPClass;
import rip.alpha.hcf.pvpclass.impl.ArcherClass;
import rip.alpha.hcf.pvpclass.impl.BardClass;
import rip.alpha.hcf.timer.impl.*;
import rip.alpha.modsuite.ModSuiteMode;
import rip.alpha.modsuite.ModSuitePlugin;
import rip.alpha.modsuite.profile.ModSuiteProfile;

import java.util.ArrayList;
import java.util.List;

public class BoardAdapter implements BoardProvider {

    private final TeamProfileHandler teamProfileHandler;
    private final String title;

    public BoardAdapter(TeamProfileHandler teamProfileHandler) {
        this.teamProfileHandler = teamProfileHandler;

        String currentTitle = HCF.getInstance().getConfiguration().getScoreboardTitle();
        currentTitle.substring(0, Math.min(32, currentTitle.length()));
        currentTitle = CC.translate(currentTitle);

        this.title = currentTitle;
    }

    @Override
    public String getTitle(Player player) {
        return this.title;
    }

    @Override
    public List<String> getLines(Player player) {
        List<String> lines = new ArrayList<>();
        TeamProfile profile = this.teamProfileHandler.getProfile(player);

        if (!profile.getSetting(Setting.SCOREBOARD)) {
            return lines;
        }
        boolean scoreboardLines = profile.getSetting(Setting.SCOREBOARD_LINES);

        if (scoreboardLines) {
            lines.add("&8&m-----------------");
        }

        if (HCF.getInstance().getConfiguration().isKitmap() && HCF.getInstance().getConfiguration().isKda()) {
            lines.add("&6Kills: &f" + profile.getStat(ProfileStatTypes.KILLS));
            lines.add("&6Deaths: &f" + profile.getStat(ProfileStatTypes.DEATHS));
        }

        if (profile.getEquipPvPClass() == null) {
            if (profile.getEquipTime() != -1 && !profile.readyToEquip()) {
                lines.add("&3Class Warmup: &r" + TimeUtil.formatTime(profile.remainingEquipTime()));
            }
        } else {
            PvPClass pvPClass = HCF.getInstance().getPvPClassHandler().getPvPClass(profile.getEquipPvPClass());
            if (pvPClass instanceof BardClass) {
                BardClass bardClass = (BardClass) pvPClass;
                double energy = bardClass.getEnergy(player);
                lines.add("&9Energy: &r" + RoundingUtil.round(energy, 2));
            } else if (pvPClass instanceof ArcherClass) {
                long millis = pvPClass.getRemainingCooldown(player.getUniqueId());
                if (millis > 0) {
                    lines.add("&5Ability Cooldown: &r" + TimeUtil.formatTime(millis));
                }
            }
        }

        HomeTimer homeTimer = profile.getTimer(HomeTimer.class);
        if (homeTimer != null) {
            lines.add("&9Home: &r" + homeTimer.getRemaining());
        }

        GoppleTimer goppleTimer = profile.getTimer(GoppleTimer.class);
        if (goppleTimer != null) {
            lines.add("&6Gopple: &r" + goppleTimer.formatRemaining());
        }

        CrappleTimer crappleTimer = profile.getTimer(CrappleTimer.class);
        if (crappleTimer != null) {
            lines.add("&eApple: &r" + crappleTimer.formatRemaining());
        }

        ArcherTagTimer archerTagTimer = profile.getTimer(ArcherTagTimer.class);
        if (archerTagTimer != null) {
            lines.add("&4Archer Tag: &r" + archerTagTimer.formatRemaining());
        }

        CombatTagTimer combatTagTimer = profile.getTimer(CombatTagTimer.class);
        if (combatTagTimer != null) {
            lines.add("&cCombat Tag: &r" + combatTagTimer.formatRemaining());
        }

        EnderpearlTimer enderpearlTimer = profile.getTimer(EnderpearlTimer.class);
        if (enderpearlTimer != null) {
            lines.add("&3Enderpearl: &r" + enderpearlTimer.formatRemaining());
        }

        LogoutTimer logoutTimer = profile.getTimer(LogoutTimer.class);
        if (logoutTimer != null) {
            lines.add("&4Logout: &r" + logoutTimer.formatRemaining());
        }

        StuckTimer stuckTimer = profile.getTimer(StuckTimer.class);
        if (stuckTimer != null) {
            lines.add("&eStuck: &r" + stuckTimer.formatRemaining());
        }

        PvPTimer pvPTimer = profile.getTimer(PvPTimer.class);
        if (pvPTimer != null) {
            lines.add("&aProtection: &r" + pvPTimer.formatRemaining());
        }

        for (Game game : HCF.getInstance().getGameHandler().getGames()) {
            if (!game.isActive()) {
                continue;
            }
            List<String> gameLines = game.getScoreboardLines();
            if (gameLines == null) {
                continue;
            }
            lines.addAll(gameLines);
        }

        ModSuiteProfile modSuiteProfile = ModSuitePlugin.getInstance().getModSuiteAPI().getProfile(player);
        if (modSuiteProfile != null) {
            ModSuiteMode modSuiteMode = modSuiteProfile.getModSuiteMode();
            if (modSuiteMode != ModSuiteMode.NONE) {
                if (lines.size() > 1) {
                    lines.add(" ");
                }
                lines.add("&6Vanished: " + (modSuiteProfile.isVanished() ? "&aEnabled" : "&cDisabled"));
                if (modSuiteMode == ModSuiteMode.ADMIN) {
                    lines.add("&6Build: " + (modSuiteProfile.isBuild() ? "&aEnabled" : "&cDisabled"));
                }
            }
        }

        lines.add(" ");
        lines.add("&calpha.rip");
        if (scoreboardLines) {
            lines.add("&8&m-----------------");
        }

        if (lines.size() <= (!scoreboardLines ? 2 : 4)) {
            return null;
        }
        return lines;
    }

    @Override
    public int getUpdateInterval() {
        return 100;
    }
}
