package rip.alpha.hcf.timer.type;

import com.google.gson.JsonObject;
import net.mcscrims.libraries.util.TimeUtil;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.timer.Timer;

import java.util.concurrent.TimeUnit;

public abstract class TimestampTimer extends Timer {

    private final long time;

    public TimestampTimer(int id, boolean save, long time) {
        super(id, save);
        this.time = time;
    }

    public long getRemaining() {
        return Math.max((this.time - System.currentTimeMillis()), 0L);
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", this.getId());
        jsonObject.addProperty("time", this.time);
        return jsonObject;
    }

    @Override
    public boolean isActive() {
        return this.getRemaining() > 0;
    }

    @Override
    public String formatRemaining() {
        return TimeUtil.formatTime(this.getRemaining());
    }

    @Override
    public String formatDetailedRemaining() {
        return TimeUtil.formatLongIntoDetailedString(TimeUnit.MILLISECONDS.toSeconds(this.getRemaining()));
    }

    public abstract void onTimerUpdate(TeamProfile profile);

}
