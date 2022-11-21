package rip.alpha.hcf.combatlogger;

import lombok.Getter;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.PlayerUtil;
import net.mcscrims.libraries.util.TaskUtil;
import net.mcscrims.libraries.util.cuboid.SafeLocation;
import net.mcscrims.libraries.util.uuid.UUIDFetcher;
import net.mcscrims.libraries.villager.BaseVillager;
import net.minecraft.server.v1_7_R4.DamageSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.deathban.Death;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.statistics.ProfileStatTypes;
import rip.alpha.hcf.team.impl.PlayerTeam;
import rip.alpha.hcf.timer.impl.CombatTagTimer;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class CombatLoggerVillager extends BaseVillager {

    private final UUID playerId;
    private final String playerName;
    private final long liveTime;

    private UUID killerId = null;
    private boolean dead = false;

    public CombatLoggerVillager(Player player, World world, Location location) {
        super(((CraftWorld) world).getHandle(), false);
        this.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.setCustomName(CC.translate("&c[CombatLogger] &r" + player.getName()));
        this.setCustomNameVisible(true);
        this.getBukkitEntity().setMetadata("CombatLogger", new FixedMetadataValue(HCF.getInstance(), true));

        Villager villager = (Villager) this.getBukkitEntity();
        villager.setFireTicks(player.getFireTicks());
        villager.setFallDistance(player.getFallDistance());
        villager.setMaxHealth(player.getMaxHealth());
        villager.setHealth(player.getHealth());
        villager.setProfession(Villager.Profession.BLACKSMITH);
        villager.setCanPickupItems(false);

        this.liveTime = System.currentTimeMillis() +
                TimeUnit.SECONDS.toMillis(HCF.getInstance().getConfiguration().getCombatLoggerTime());

        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
    }

    public void spawn(World world) {
        CraftWorld craftWorld = (CraftWorld) world;
        net.minecraft.server.v1_7_R4.World nmsWorld = craftWorld.getHandle();
        this.spawnIn(nmsWorld);
        nmsWorld.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    public boolean isExpired() {
        return this.liveTime - System.currentTimeMillis() <= 0;
    }

    @Override
    public void die() {
        Location location = this.getBukkitEntity().getLocation();
        List<ItemStack> itemStacks = PlayerUtil.killOfflinePlayer(this.playerId, this.playerName);

        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null) {
                continue;
            }
            if (itemStack.getTypeId() == 0) {
                continue;
            }
            location.getWorld().dropItemNaturally(location, itemStack);
        }

        TaskUtil.runAsync(() -> {
            TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfileOrLoad(this.playerId);
            TeamProfile killerProfile = null;
            if (this.killerId != null) {
                killerProfile = HCF.getInstance().getProfileHandler().getProfile(this.killerId);
            }
            HCF.getInstance().getDeathbanHandler().handleDeath(teamProfile, killerProfile, location);
//            teamProfile.addDeath(new Death(playerId, this.killerId, new SafeLocation(location), new Date(), teamProfile.createDeathInventory()));

            if (this.playerId != null && this.killerId != null) {
                if (killerProfile != null) {
                    Bukkit.broadcastMessage(CC.translate("&7(Combat Logger) &c" +
                            UUIDFetcher.getName(this.playerId) + "&4[" + teamProfile.getStat(ProfileStatTypes.KILLS) + "]"
                            + "&e was slain by &c" + UUIDFetcher.getName(this.killerId) + "&4[" + killerProfile.getStat(ProfileStatTypes.KILLS) + "]&e."));
                }
            }
        }, HCF.getInstance());

        HCF.getInstance().getCombatLoggerHandler().removeVillager(this.playerId);

        super.die();
        this.dead = true;
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (Bukkit.getPlayer(this.playerId) != null) { //if player is online, make the combat logger un damageable
            return false;
        }

        this.killerId = null;

        if (damagesource.getEntity() != null) {
            Entity entity = damagesource.getEntity().getBukkitEntity();
            Player player = null;
            if (entity instanceof Player) {
                player = (Player) entity;
            } else if (entity instanceof Projectile && ((Projectile) entity).getShooter() instanceof Player) {
                player = (Player) ((Projectile) entity).getShooter();
            }

            if (player != null) {
                this.killerId = player.getUniqueId();
                PlayerTeam playerTeam = HCF.getInstance().getTeamHandler().getPlayerTeamByPlayer(player);

                if (playerTeam != null && playerTeam.getMember(this.playerId) != null) {
                    return false;
                }

                TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(this.playerId);
                if (teamProfile != null) {
                    teamProfile.addTimer(new CombatTagTimer());
                }
                TeamProfile damagerProfile = HCF.getInstance().getProfileHandler().getProfile(player);
                if (damagerProfile != null) {
                    damagerProfile.addTimer(new CombatTagTimer());
                }
            }
        }

        return super.damageEntity(damagesource, f);
    }
}
