package rip.alpha.hcf.team.grid;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.mcscrims.libraries.util.cuboid.alpha.ACuboid;

import java.util.UUID;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public class ClaimEntry {

    private UUID teamId;
    private ACuboid cuboid;

}
