package rip.alpha.hcf.effect;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.MobEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rip.foxtrot.spigot.handler.PotionEffectHandler;

@RequiredArgsConstructor
public class EffectPotionHandler implements PotionEffectHandler {

    private final PlayerEffectHandler playerEffectHandler;

    @Override
    public boolean onPotionAdd(LivingEntity livingEntity, PotionEffect potionEffect) {
        return false;
    }

    @Override
    public boolean onPotionRemove(LivingEntity livingEntity, PotionEffect potionEffect) {
        return false;
    }

    @Override
    public boolean onPotionTick(LivingEntity livingEntity, MobEffect mobEffect) {
        if (!(livingEntity instanceof EntityPlayer)) {
            return false;
        }
        return mobEffect.getDuration() > PlayerEffect.CHECK_EFFECT_TICKS;
    }

    @Override
    public boolean onPotionExtend(LivingEntity livingEntity, PotionEffect potionEffect) {
        return false;
    }

    @Override
    public void onPotionExpire(LivingEntity entity, PotionEffect effect) {
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;
        PotionEffectType type = effect.getType();
        this.playerEffectHandler.restorePotionEffect(player, type);
    }
}
