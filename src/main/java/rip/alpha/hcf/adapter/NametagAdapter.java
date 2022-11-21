package rip.alpha.hcf.adapter;

import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.nametag.NametagProvider;
import net.mcscrims.libraries.nametag.NametagVisibility;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.RoundingUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.game.impl.KoTHGame;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.TeamProfileHandler;
import rip.alpha.hcf.profile.settings.Setting;
import rip.alpha.hcf.team.TeamHandler;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.modsuite.ModSuiteMode;
import rip.alpha.modsuite.ModSuitePlugin;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

@RequiredArgsConstructor
public class NametagAdapter implements NametagProvider {

    private final TeamHandler teamHandler;
    private final TeamProfileHandler teamProfileHandler;

    @Override
    public String getPrefix(Player target, Player viewer) {
        if (target.getUniqueId().equals(viewer.getUniqueId())) {
            return CC.DARK_GREEN; //just default it this way
        }

        TeamProfile targetProfile = this.teamProfileHandler.getProfile(target);

        PlayerTeam targetTeam = this.teamHandler.getPlayerTeamByPlayer(target);
        PlayerTeam viewerTeam = this.teamHandler.getPlayerTeamByPlayer(viewer);

        if (targetTeam != null && viewerTeam != null &&
                targetTeam.getId().equals(viewerTeam.getId())) {
            return CC.DARK_GREEN;
        }

        if (targetProfile.isArcherTagged()) {
            return CC.RED;
        }

        if (viewerTeam != null) {
            if (viewerTeam.isFocused(target.getUniqueId())) {
                return CC.PINK;
            }
        }

        return CC.YELLOW;
    }

    @Override
    public String getSuffix(Player target, Player viewer) {
        ModSuiteMode modSuiteMode = ModSuitePlugin.getInstance().getModSuiteAPI().getCurrentMode(target);
        if (modSuiteMode != ModSuiteMode.NONE) {
            return "*[MM]".replace("*", ModSuitePlugin.getInstance().getModSuiteAPI().isVanished(target) ? CC.GREEN : CC.RED);
        }

        TeamProfile viewerProfile = this.teamProfileHandler.getProfile(viewer);
        TeamProfile profile = this.teamProfileHandler.getProfile(target);
        String suffix = "";

        PlayerTeam viewerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(viewer);
        if (viewerProfile.getSetting(Setting.EXTRA_NAME_TAGS)) {
            if (profile.isArcherTagged()) {
                suffix += CC.translate(" &c[AT]");
            }

            if (viewerTeam != null) {
                if (viewerTeam.isFocused(target.getUniqueId())) {
                    suffix += CC.translate("&d[F]");
                }
            }
        }

        PlayerTeam targetTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(target);
        if (targetTeam != null && viewerTeam != null) {
            if (viewerTeam.getId().equals(targetTeam.getId())) {
                for (KoTHGame game : HCF.getInstance().getGameHandler().getGamesByType(KoTHGame.class)) {
                    if (game.isActive() && game.getCapturingUUID() != null) {
                        Player player = game.getCapturingPlayer();
                        if (player == null) {
                            continue;
                        }
                        if (!player.getUniqueId().equals(target.getUniqueId())) {
                            continue;
                        }
                        suffix += CC.translate("&6[C]");
                        break;
                    }
                }
            }
        }

        return suffix;
    }

    @Override
    public String getTeamName(Player target, Player viewer) {
        return target.getName();
    }

    @Override
    public boolean isFriendly(Player target, Player viewer) {
        if (target.getUniqueId().equals(viewer.getUniqueId())) {
            return true;
        }
        PlayerTeam targetTeam = this.teamHandler.getPlayerTeamByPlayer(target);
        PlayerTeam viewerTeam = this.teamHandler.getPlayerTeamByPlayer(viewer);
        if (targetTeam == null || viewerTeam == null) {
            return false;
        }
        return targetTeam.getId().equals(viewerTeam.getId());
    }

    @Override
    public NametagVisibility getNametagVisibility(Player target, Player viewer) {
        if (target.getUniqueId().equals(viewer.getUniqueId())) {
            return NametagVisibility.ALWAYS;
        }
        try {
            PotionEffect effect = HCF.getInstance().getPlayerEffectHandler().getActivePotionEffect(target, PotionEffectType.INVISIBILITY);
            if (effect == null) {
                return NametagVisibility.ALWAYS;
            }

            PlayerTeam targetTeam = this.teamHandler.getPlayerTeamByPlayer(target);
            PlayerTeam viewerTeam = this.teamHandler.getPlayerTeamByPlayer(viewer);
            if (targetTeam != null && viewerTeam != null && targetTeam.getId().equals(viewerTeam.getId())) {
                return NametagVisibility.ALWAYS;
            }

            return NametagVisibility.NEVER;
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
            return NametagVisibility.ALWAYS;
        }
    }

    @Override
    public List<String> getExtraTags(Player target, Player viewer) {
        if (target.getUniqueId().equals(viewer.getUniqueId())) {
            return null;
        }
        if (target.getGameMode() == GameMode.CREATIVE) {
            return null;
        }
        ModSuiteMode modSuiteMode = ModSuitePlugin.getInstance().getModSuiteAPI().getCurrentMode(viewer);
        if (modSuiteMode == ModSuiteMode.NONE) {
            return null;
        }
        PlayerTeam playerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(target);
        if (playerTeam == null) {
            return null;
        }
        List<String> strings = new ArrayList<>();
        strings.add("&e[&6" + playerTeam.getName() + "&e]");
        strings.add("&eDTR: " + playerTeam.getDTRSymbol() + playerTeam.getDTRColor() + RoundingUtil.round(playerTeam.getDtr(), 2));
        return strings;
    }

    @Override
    public int getUpdateInterval() {
        return 500;
    }
}
