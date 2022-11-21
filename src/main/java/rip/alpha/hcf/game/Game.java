package rip.alpha.hcf.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public abstract class Game {

    private final UUID id;

    public Document toDocument() {
        Document document = new Document();
        document.put("id", this.id.toString());
        document.put("gameClass", this.getClass().getSimpleName());
        return document;
    }

    public abstract String getName();

    public abstract String getColor();

    public abstract void start();

    public abstract void end(boolean forced);

    public abstract void reset();

    public abstract void tick();

    public abstract boolean isActive();

    public abstract List<String> getScoreboardLines();

    public abstract boolean isCompleted();

    public abstract void fromDocument(Document document);


}
