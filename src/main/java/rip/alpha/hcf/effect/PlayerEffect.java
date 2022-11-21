package rip.alpha.hcf.effect;

import lombok.Getter;
import net.mcscrims.libraries.util.TaskUtil;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rip.alpha.hcf.HCF;

@Getter
public class PlayerEffect {
    public static final int PERM_EFFECT_TICKS = 998877 * 20;
    public static final int CHECK_EFFECT_TICKS = 668877 * 20;

    private final PotionEffectType type;
    private final int duration;
    private final int amplifier;

    public PlayerEffect(PotionEffect effect) {
        this(effect.getType(), effect.getDuration(), effect.getAmplifier());
    }

    public PlayerEffect(PotionEffectType type, int amplifier) {
        this(type, PERM_EFFECT_TICKS, amplifier);
    }

    public PlayerEffect(PotionEffectType type, int duration, int amplifier) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public PotionEffect createEffect() {
        return new PotionEffect(this.type, this.duration, this.amplifier);
    }

    public void applyEffect(Player player) {
        TaskUtil.runSync(() -> player.addPotionEffect(this.createEffect(), true), HCF.getInstance());
    }
}
