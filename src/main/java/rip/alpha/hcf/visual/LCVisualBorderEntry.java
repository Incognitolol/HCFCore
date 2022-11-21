package rip.alpha.hcf.visual;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;

import java.awt.Color;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class LCVisualBorderEntry {

    private final UUID uuid;
    private final ACuboid cuboid;
    private final Color color;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LCVisualBorderEntry)) {
            return false;
        }
        LCVisualBorderEntry lcVisualBorderEntry = (LCVisualBorderEntry) o;
        return lcVisualBorderEntry.getUuid().equals(this.getUuid());
    }
}
