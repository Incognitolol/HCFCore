package rip.alpha.hcf.items.impl;

import net.mcscrims.basic.Basic;
import net.mcscrims.basic.profile.BasicProfile;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.TaskUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.items.CItem;
import rip.alpha.hcf.items.CItemRarity;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.pvpclass.impl.BardClass;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.team.impl.SystemTeam;

import java.util.List;

public class HealerWandItem extends CItem {

    private final int bardRadius = HCF.getInstance().getConfiguration().getBardRadius();

    public HealerWandItem(ItemStack itemStack, List<String> lore, CItemRarity rarity, boolean remove) {
        super(itemStack, lore, rarity, remove);
    }

    @Override
    public void onClick(Player player) {
        PlayerTeam team = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);
        TaskUtil.runAsync(() -> {
            TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player.getUniqueId());

            if (!profile.isActiveClass(BardClass.class)) {
                player.sendMessage(CC.RED + "You must be in bard to use this wand!");
                return;
            }

            BardClass bardClass = (BardClass) HCF.getInstance().getPvPClassHandler().getPvPClass(profile.getEquipPvPClass());;
            if (bardClass.getEnergy(player) < 60) {
                player.sendMessage(CC.RED + "You do not have enough energy for this effect!");
                player.sendMessage(CC.RED + "You need at least 60 energy to use this effect.");
                return;
            }

            bardClass.getBardEnergyMap().put(player.getUniqueId(), 0D);
            int i = 0;

            for (Entity entity : player.getNearbyEntities(this.bardRadius, this.bardRadius, this.bardRadius)) {
                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    if (target.getUniqueId().equals(player.getUniqueId())) {
                        continue;
                    }

                    if (team != null && team.getMember(target.getUniqueId()) == null) {
                        continue;
                    }

                    TeamProfile targetProfile = HCF.getInstance().getProfileHandler().getProfile(target.getUniqueId());
                    if (targetProfile != null) {
                        Team lastTeam = targetProfile.getLastClaimTeam();
                        if (lastTeam instanceof SystemTeam) {
                            SystemTeam systemTeam = (SystemTeam) lastTeam;
                            if (systemTeam.isSafezone()) {
                                continue;
                            }
                        }
                    }

                    if (target.getHealth() <= 16){
                        target.setHealth(target.getHealth() + 4);
                    } else {
                        target.setHealth(20);
                    }

                    i++;
                }
            }

            if (i > 0) {
                BasicProfile basicProfile = Basic.getInstance().getBasicAPI().getProfile(player.getUniqueId());
                basicProfile.addXp(i);
                player.sendMessage(CC.GREEN + "You have healed " + i + " other player(s)");
            } else {
                player.sendMessage(CC.RED + "There was nobody nearby to apply the effect to.");
            }

        }, HCF.getInstance());
    }
}
