package rip.alpha.hcf.mobstack;

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.mcscrims.basic.Basic;
import net.mcscrims.basic.profile.BasicProfile;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.ParticleEffect;
import net.mcscrims.libraries.util.TimeUtil;
import net.mcscrims.libraries.util.items.ItemUtils;
import net.minecraft.server.v1_7_R4.EntityLiving;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class MobstackListener implements Listener {

    private final MobstackHandler mobstackHandler;

    private final Int2BooleanMap attemptedBreed;
    private final Int2LongMap lastBreed;

    private final Short2ObjectMap<Material> breedableMobs;

    public MobstackListener(MobstackHandler mobstackHandler) {
        this.mobstackHandler = mobstackHandler;

        this.attemptedBreed = new Int2BooleanOpenHashMap();
        this.lastBreed = new Int2LongOpenHashMap();

        this.breedableMobs = new Short2ObjectOpenHashMap<>();
        this.breedableMobs.put(EntityType.COW.getTypeId(), Material.WHEAT);
        this.breedableMobs.put(EntityType.SHEEP.getTypeId(), Material.WHEAT);
        this.breedableMobs.put(EntityType.PIG.getTypeId(), Material.CARROT_ITEM);
        this.breedableMobs.put(EntityType.CHICKEN.getTypeId(), Material.SEEDS);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return;
        }

        LivingEntity livingEntity = event.getEntity();
        if (mobstackHandler.getExcludedMobs().contains(livingEntity.getType().getTypeId())) {
            return;
        }

        if (!(livingEntity instanceof Creature) && !(livingEntity instanceof Slime)) {
            return;
        }

        LivingEntity foundCreature = this.mobstackHandler.getAvailableStackEntity(livingEntity);

        if (foundCreature != null) {
            event.setCancelled(true);
        } else {
            foundCreature = livingEntity;
        }

        int amount = this.mobstackHandler.getMobStackCount(foundCreature);
        this.setMobstackEntity(foundCreature, ++amount);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureDeath(EntityDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();

        if (!(livingEntity instanceof Creature) && !(livingEntity instanceof Slime)) {
            return;
        }

        Player killer = livingEntity.getKiller();
        if (killer != null) {
            BasicProfile basicProfile = Basic.getInstance().getBasicAPI().getProfile(killer.getUniqueId());
            if (basicProfile != null) {
                basicProfile.addXp(10);
            }
        }

        if (!this.mobstackHandler.isMobStackEntity(livingEntity)) {
            return;
        }

        int amount = this.mobstackHandler.getMobStackCount(livingEntity) - 1;

        if (amount > 0) {
            LivingEntity replacedEntity = (LivingEntity) livingEntity.getLocation().getWorld().spawnEntity(livingEntity.getLocation(), livingEntity.getType());
            this.setMobstackEntity(replacedEntity, amount);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = player.getItemInHand();

        if (itemStack == null) {
            return;
        }

        Entity entity = event.getRightClicked();

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        LivingEntity livingEntity = (LivingEntity) entity;
        if (!(livingEntity instanceof Creature)) {
            return;
        }

        short typeId = livingEntity.getType().getTypeId();
        Material material = this.breedableMobs.get(typeId);

        if (material == null) {
            return;
        }

        if (itemStack.getType() != material) {
            return;
        }

        if (!this.mobstackHandler.isMobStackEntity(livingEntity)) {
            return;
        }

        int amount = this.mobstackHandler.getMobStackCount(livingEntity);
        if (amount <= 0) {
            return;
        }

        event.setCancelled(true);
        if (amount == 1) {
            player.sendMessage(CC.RED + "This mob stack only has one entity inside it, you cannot breed it.");
            return;
        }

        int entityId = entity.getEntityId();
        Long lastBreed = this.lastBreed.getOrDefault(entityId, null);
        if (lastBreed != null && lastBreed - System.currentTimeMillis() > 0) {
            long diff = lastBreed - System.currentTimeMillis();
            player.sendMessage(CC.RED + "That mob is on breeding cooldown for " + TimeUtil.formatDuration(diff));
            return;
        }

        Boolean attemptedBreed = this.attemptedBreed.getOrDefault(entity.getEntityId(), false);
        ParticleEffect.HEART.display(0, 0, 0, 1, 1, entity.getLocation(), player);
        player.getInventory().setItemInHand(ItemUtils.minusItem(itemStack));

        if (!attemptedBreed) {
            player.sendMessage(CC.RED + "You have given that mob some food, give it some more to breed.");
            this.attemptedBreed.put(entityId, true);
            return;
        }

        this.attemptedBreed.put(entityId, false);
        this.lastBreed.put(entity.getEntityId(), System.currentTimeMillis() + (60 * 1000));
        amount += 1;

        if (amount > this.mobstackHandler.getMaxStack()) {
            LivingEntity replacedEntity = (LivingEntity) entity.getLocation().getWorld().spawnEntity(livingEntity.getLocation(), livingEntity.getType());
            this.setMobstackEntity(replacedEntity, 1);
            return;
        }

        this.setMobstackEntity(livingEntity, amount);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof EntityLiving) {
                if (this.mobstackHandler.isMobStackEntity((LivingEntity) entity)) {
                    entity.remove();
                }
            }
        }
    }

    private void setMobstackEntity(LivingEntity livingEntity, int amount) {
        livingEntity.setCustomName(this.mobstackHandler.getStartFormat() + amount);
        livingEntity.setCustomNameVisible(true);
    }
}
