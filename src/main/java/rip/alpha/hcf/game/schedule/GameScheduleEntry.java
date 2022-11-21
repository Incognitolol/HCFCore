package rip.alpha.hcf.game.schedule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.mcscrims.libraries.util.CC;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@RequiredArgsConstructor
public class GameScheduleEntry {

    public static final SimpleDateFormat DATE_PARSE_FORMAT = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
    public static final SimpleDateFormat DATE_CHAT_FORMAT = new SimpleDateFormat("EEEE @ hh:mmaa");

    private final long startTime;
    private final String gameName, colorName;

    @Setter
    private long lastTime = -1;

    @Setter
    private boolean started = false;

    public boolean canBeStarted() {
        return this.remainingTime() <= 0;
    }

    public long remainingTime() {
        return this.startTime - System.currentTimeMillis();
    }

    public String formatDateTime() {
        return CC.translate("&6" + DATE_CHAT_FORMAT.format(new Date(this.startTime))
                .replace("@", "&e@&6")
                .replace("AM", "am")
                .replace("PM", "pm") + " EST");
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("startTime", this.startTime);
        document.put("gameName", this.gameName);
        document.put("colorName", this.colorName);
        return document;
    }

    public static GameScheduleEntry fromDocument(Document document) {
        long startTime = document.getLong("startTime");
        String gameName = document.getString("gameName");
        String colorName = document.getString("colorName");
        return new GameScheduleEntry(startTime, gameName, colorName);
    }

}
