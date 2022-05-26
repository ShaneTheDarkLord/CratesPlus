package plus.crates.opener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import plus.crates.crates.Crate;
import plus.crates.crates.SupplyCrate;

public class SupplyOpener extends Opener {

    public SupplyOpener(Plugin plugin) {
        super(plugin, "Supply");
    }

    @Override
    public void doSetup() {
    }

    @Override
    public void doOpen(Player player, Crate crate, Location blockLocation) {
        ((SupplyCrate) crate).handleWin(player, blockLocation.getBlock());
    }

    @Override
    public void doReopen(Player player, Crate crate, Location blockLocation) {
    }

    @Override
    public boolean doesSupport(Crate crate) {
        return crate instanceof SupplyCrate;
    }

}
