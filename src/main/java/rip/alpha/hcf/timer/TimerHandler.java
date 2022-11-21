package rip.alpha.hcf.timer;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.cuboid.SafeLocation;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;
import net.mcscrims.libraries.util.listeners.ClassUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.team.Team;
import rip.alpha.hcf.timer.impl.HomeTimer;
import rip.alpha.hcf.timer.impl.LogoutTimer;
import rip.alpha.hcf.timer.impl.StuckTimer;
import rip.alpha.hcf.timer.type.SecondsTimer;
import rip.alpha.hcf.timer.type.TimestampTimer;
import rip.foxtrot.spigot.fSpigot;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TimerHandler {

    private final Random random = new Random();

    private final Int2ObjectMap<Class<? extends Timer>> idToTimer;
    private final Object2IntMap<Class<? extends Timer>> timerClazzToId;

    public TimerHandler(HCF instance) {
        this.idToTimer = new Int2ObjectOpenHashMap<>();
        this.timerClazzToId = new Object2IntOpenHashMap<>();

        try {
            for (Class<?> clazz : ClassUtils.getClassesInPackage(instance, "rip.alpha.hcf.timer.impl")) {
                if (clazz.getSuperclass().equals(SecondsTimer.class)) {
                    SecondsTimer timer = (SecondsTimer) clazz.getConstructor(int.class).newInstance(0);
                    this.idToTimer.put(timer.getId(), timer.getClass());
                    this.timerClazzToId.put(timer.getClass(), timer.getId());
                } else if (clazz.getSuperclass().equals(TimestampTimer.class)) {
                    TimestampTimer timer = (TimestampTimer) clazz.getConstructor(long.class).newInstance(0L);
                    this.idToTimer.put(timer.getId(), timer.getClass());
                    this.timerClazzToId.put(timer.getClass(), timer.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        fSpigot.INSTANCE.addMovementHandler(new TimerMovementHandler(this));

        PluginManager pluginManager = instance.getServer().getPluginManager();
        pluginManager.registerEvents(new TimerListener(this, HCF.getInstance().getProfileHandler()), instance);

        HCF.getInstance().getScheduledExecutorService().scheduleAtFixedRate(new TimerTask(instance.getProfileHandler()), 1, 1, TimeUnit.SECONDS);
    }

    public Class<? extends Timer> getClassById(int i) {
        return this.idToTimer.get(i);
    }

    public int getIdByClass(Class<? extends Timer> clazz) {
        return this.timerClazzToId.get(clazz);
    }

    public <T extends Timer> void cancelTimer(TeamProfile teamProfile, Class<T> clazz) {
        T t = teamProfile.getTimer(clazz);
        if (t == null) {
            return;
        }
        this.cancelTimer(teamProfile, t);
    }

    public void cancelTimer(TeamProfile profile, Timer timer) {
        profile.removeTimer(timer);
        timer.onRemove(profile);
    }

    public Timer parseTimer(JsonObject object) {
        try {
            int id = object.get("id").getAsInt();
            Class<? extends Timer> timerClazz = this.getClassById(id);

            if (timerClazz == null) {
                return null;
            }

            if (timerClazz.getSuperclass().equals(SecondsTimer.class)) {
                int seconds = object.get("seconds").getAsInt();
                boolean paused = object.get("paused").getAsBoolean();
                SecondsTimer secondsTimer = (SecondsTimer) timerClazz.getConstructor(int.class).newInstance(seconds);
                secondsTimer.setPaused(paused);
                return secondsTimer;
            } else if (timerClazz.getSuperclass().equals(TimestampTimer.class)) {
                long time = object.get("time").getAsLong();
                return timerClazz.getConstructor(long.class).newInstance(time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void handleMovement(Player player, Location from, Location to) {
        if (from.getBlockZ() == to.getBlockZ() && from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()) {
            return;
        }
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(player);
        if (profile == null) {
            return;
        }

        this.cancelTimer(profile, HomeTimer.class);
        this.cancelTimer(profile, StuckTimer.class);
        this.cancelTimer(profile, LogoutTimer.class);
    }

    public void teleportToSafeLocation(Player player, Team currentTeam) { //need to put it in a param for pvptimer claim
        ACuboid currentClaim = currentTeam.getClaim();

        if (currentClaim == null) {
            return;
        }

        ACuboid newClaim = currentClaim.expand(3);

        List<SafeLocation> possibleLocations = Lists.newArrayList();
        for (int x = newClaim.getMinX(); x < newClaim.getMaxX(); x++) {
            for (int z = newClaim.getMinZ(); z < newClaim.getMaxZ(); z++) {
                if (currentClaim.contains(x, z)) {
                    continue;
                }
                possibleLocations.add(new SafeLocation(x, 0, z, newClaim.getWorld().getName()));
            }
        }

        int size = possibleLocations.size();
        if (size <= 0) {
            player.sendMessage(CC.RED + "There was no safe location.");
            return;
        }

        World world = currentClaim.getWorld();
        SafeLocation safeLocation = possibleLocations.get(this.random.nextInt(size));
        Location chosenLocation = this.getTeleportLocation(world, safeLocation);
        Block block = chosenLocation.getBlock();

        int attempts = 0;
        while (block == null || block.getType() != Material.AIR || block.isLiquid()) {
            if (attempts >= 3) {
                chosenLocation = null;
                break;
            }

            possibleLocations.remove(safeLocation);
            safeLocation = possibleLocations.get(this.random.nextInt(size));
            chosenLocation = this.getTeleportLocation(world, safeLocation);
            block = chosenLocation.getBlock();
            attempts++;
        }

        if (chosenLocation == null) {
            player.sendMessage(CC.RED + "There was no safe location.");
            return;
        }

        Location finalChosenLocation = chosenLocation;
        Bukkit.getServer().getScheduler().callSyncMethod(HCF.getInstance(), () -> player.teleport(finalChosenLocation));
    }

    public Location getTeleportLocation(World world, SafeLocation safeLocation) {
        int x = safeLocation.getX();
        int z = safeLocation.getZ();
        double y = (world.getHighestBlockYAt(x, z) + 1.5D);
        return new Location(world, x, y, z);
    }
}
