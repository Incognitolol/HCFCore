package rip.alpha.hcf.timer.type;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.mcscrims.libraries.util.TimeUtil;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.timer.Timer;

@Getter
@Setter
public abstract class SecondsTimer extends Timer {

    private int seconds;
    private boolean paused = false;

    public SecondsTimer(int id, boolean save, int seconds) {
        super(id, save);
        this.seconds = seconds;
    }

    public void decrement() {
        this.seconds--;
    }

    public int getRemaining() {
        return Math.max(0, this.seconds);
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", this.getId());
        jsonObject.addProperty("seconds", this.seconds);
        jsonObject.addProperty("paused", this.paused);
        return jsonObject;
    }

    @Override
    public boolean isActive() {
        return this.seconds > 0;
    }

    @Override
    public String formatRemaining() {
        return TimeUtil.formatToMMS(this.getRemaining());
    }

    @Override
    public String formatDetailedRemaining() {
        return TimeUtil.formatIntoDetailedString(this.getRemaining());
    }

    public abstract void onDecrement(TeamProfile profile);

}
