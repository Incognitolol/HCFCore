package rip.alpha.hcf.effect;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.TaskUtil;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rip.alpha.hcf.HCF;

@Getter
@RequiredArgsConstructor
public class PlayerEffectRestoreEntry {

    private final PotionEffectType type;
    private final int duration;
    private final int amplifier;

    public PlayerEffectRestoreEntry(PotionEffect effect, int duration) {
        this.type = effect.getType();
        this.duration = duration;
        this.amplifier = effect.getAmplifier();
    }

    public PlayerEffectRestoreEntry(PlayerEffect effect) {
        this.type = effect.getType();
        this.duration = effect.getDuration();
        this.amplifier = effect.getAmplifier();
    }

    public PotionEffect createEffect() {
        return new PotionEffect(this.type, this.duration, this.amplifier);
    }

    public void apply(Player player) {
        TaskUtil.runSync(() -> player.addPotionEffect(this.createEffect(), true), HCF.getInstance());
    }
}
