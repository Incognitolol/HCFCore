package rip.alpha.hcf.timer;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public abstract class Timer {

    private final int id;
    private final boolean save;

    public void addTimer(UUID uuid) {
        TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(uuid);
        this.addTimer(profile);
    }

    public void addTimer(TeamProfile profile) {
        if (profile == null) {
            return;
        }
        profile.addTimer(this);
    }

    public abstract boolean isActive();

    public abstract String formatRemaining();

    public abstract String formatDetailedRemaining();

    public abstract JsonObject toJson();

    public abstract void onApply(TeamProfile profile);

    public abstract void onExtend(TeamProfile profile);

    public abstract void onRemove(TeamProfile profile);

    public abstract void onExpire(TeamProfile profile);

}
