package plus.crates.handlers.Holograms;

import org.bukkit.Location;
import plus.crates.crates.Crate;

import java.util.ArrayList;
import java.util.List;

public class FallbackHologram implements Hologram {

    public void create(Location location, Crate crate, List<String> lines) {
        crate.getCratesPlus().getLogger().warning("Hologram #create was called but no Hologram plugin is loaded!");
    }

    public void remove(Location location, Crate crate) {
        crate.getCratesPlus().getLogger().warning("Hologram #remove was called but no Hologram plugin is loaded!");
    }

}
