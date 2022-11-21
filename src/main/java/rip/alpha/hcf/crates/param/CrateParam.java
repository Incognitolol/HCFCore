package rip.alpha.hcf.crates.param;

import lombok.RequiredArgsConstructor;
import net.mcscrims.command.param.IParameter;
import net.mcscrims.libraries.util.CC;
import org.bukkit.command.CommandSender;
import rip.alpha.hcf.crates.Crate;
import rip.alpha.hcf.crates.CrateHandler;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CrateParam implements IParameter<Crate> {
    private final CrateHandler crateHandler;

    @Override
    public Crate transform(CommandSender commandSender, String s) {
        Crate crate = this.crateHandler.getCrateByName(s);

        if (crate == null) {
            commandSender.sendMessage(CC.RED + "That crate cannot be found");
            return null;
        }

        return crate;
    }

    @Override
    public List<String> getTabComplete(CommandSender commandSender) {
        return new ArrayList<>(this.crateHandler.getCrateMap().keySet());
    }
}
