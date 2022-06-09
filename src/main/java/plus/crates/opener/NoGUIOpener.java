package plus.crates.opener;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import plus.crates.crates.Crate;
import plus.crates.CratesPlus;

import java.io.IOException;
import java.util.logging.Level;

public class NoGUIOpener extends FlatConfigurableOpener  {
    private boolean chestSound = true;

    public NoGUIOpener(CratesPlus cratesPlus) {
        super(cratesPlus, "NoGUI");
    }

    @Override
    public void doSetup() {
        FileConfiguration config = getConfig();
        if (config.isSet("Chest Sound")) {
            chestSound = config.getBoolean("Chest Sound", true);
        } else {
            config.set("Chest Sound", true);
            try {
                saveConfig(config);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save opener file", e);

            }
        }
    }

    @Override
    public void doOpen(Player player, Crate crate, Location location) {
        if (chestSound) {
            Sound sound = null;
            try {
                sound = Sound.valueOf("CHEST_OPEN");
            } catch (Exception e) {
                try {
                    sound = Sound.valueOf("BLOCK_CHEST_OPEN");
                } catch (Exception ee) {
                    // This should never happen!
                }
            }
            if (sound != null)
                player.playSound(player.getLocation(), sound, (float) 0.5, 1);
        }
        crate.handleWin(player);
        finish(player);
    }

    @Override
    public void doReopen(Player player, Crate crate, Location location) {

    }

    public boolean doesSupport(Crate crate) {
        return true;
    }

}
