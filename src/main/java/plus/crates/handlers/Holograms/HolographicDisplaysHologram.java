package plus.crates.handlers.Holograms;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import plus.crates.Utils.LinfootUtil;
import plus.crates.crates.Crate;

import java.util.*;

public class HolographicDisplaysHologram implements Hologram {
    private final Map<String, com.gmail.filoghost.holographicdisplays.api.Hologram> holograms = new HashMap<>();

    public void create(Location location, Crate crate, List<String> lines) {
        com.gmail.filoghost.holographicdisplays.api.Hologram hologram = HologramsAPI.createHologram(crate.getCratesPlus(), location.clone().add(0, 1.25, 0));
        for (String line : lines) {
            hologram.appendTextLine(line);
        }
        holograms.put(LinfootUtil.formatLocation(location), hologram);
    }

    public void remove(Location location, Crate crate) {
        String formatLocation = LinfootUtil.formatLocation(location);
        if (holograms.containsKey(formatLocation)) {
            holograms.get(formatLocation).delete();
            holograms.remove(formatLocation);
        }
    }
}
