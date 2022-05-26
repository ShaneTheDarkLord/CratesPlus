package plus.crates.crates;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plus.crates.CratesPlus;
import plus.crates.handlers.ConfigHandler;
import plus.crates.handlers.MessageHandler;
import plus.crates.Utils.GUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class KeyCrate extends Crate {
    protected Key key;
    protected HashMap<String, Location> locations = new HashMap<>();
    protected boolean preview = true;
    protected double knockback = 0.0;

    public KeyCrate(ConfigHandler configHandler, String name) {
        super(configHandler, name);
        loadCrate();
    }

    protected void loadCrate() {
        CratesPlus cratesPlus = getCratesPlus();
        if (cratesPlus.getConfig().isSet("Crates." + name + ".Preview"))
            this.preview = cratesPlus.getConfig().getBoolean("Crates." + name + ".Preview");
        if (cratesPlus.getConfig().isSet("Crates." + name + ".Knockback"))
            this.knockback = cratesPlus.getConfig().getDouble("Crates." + name + ".Knockback");

        if (!cratesPlus.getConfig().isSet("Crates." + name + ".Key") || !cratesPlus.getConfig().isSet("Crates." + name + ".Key.Item") || !cratesPlus.getConfig().isSet("Crates." + name + ".Key.Name") || !cratesPlus.getConfig().isSet("Crates." + name + ".Key.Enchanted"))
            return;

        this.key = new Key(this, Material.valueOf(cratesPlus.getConfig().getString("Crates." + name + ".Key.Item")), (short) cratesPlus.getConfig().getInt("Crates." + name + ".Key.Data", 0), cratesPlus.getConfig().getString("Crates." + name + ".Key.Name").replaceAll("%type%", getName(true)), cratesPlus.getConfig().getBoolean("Crates." + name + ".Key.Enchanted"), cratesPlus.getConfig().getStringList("Crates." + name + ".Key.Lore"), cratesPlus);
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public boolean isPreview() {
        return preview;
    }

    public HashMap<String, Location> getLocations() {
        return locations;
    }

    public void addLocation(String string, Location location) {
        locations.put(string, location);
    }

    public Location getLocation(String key) {
        return locations.get(key);
    }

    public Location removeLocation(String key) {
        return locations.remove(key);
    }

    public void loadHolograms(Location location) {
        CratesPlus cratesPlus = getCratesPlus();

        // Do holograms
        if (cratesPlus.getConfigHandler().getHolograms(this.slug) == null || cratesPlus.getConfigHandler().getHolograms(this.slug).isEmpty())
            return;

        ArrayList<String> list = new ArrayList<>();
        for (String line : cratesPlus.getConfigHandler().getHolograms(this.slug))
            list.add(MessageHandler.convertPlaceholders(line, null, this, null));
        cratesPlus.getHologramHandler().getHologramPlugin().getHologram().create(location, this, list);
    }

    public void removeHolograms(Location location) {
        getCratesPlus().getHologramHandler().getHologramPlugin().getHologram().remove(location, this);
    }

    public void removeFromConfig(Location location) {
        getCratesPlus().getStorageHandler()
                .removeCrateLocation(this.getName(false).toLowerCase(), location)
                .exceptionally(throwable -> {
                    getCratesPlus().getLogger().log(Level.SEVERE, "Unhandled Exception", throwable);
                    return null;
                });    }

    public void addToConfig(Location location) {
        getCratesPlus().getStorageHandler()
                .addCrateLocation(this.getName(false).toLowerCase(), location)
                .exceptionally(throwable -> {
                    getCratesPlus().getLogger().log(Level.SEVERE, "Unhandled Exception", throwable);
                    return null;
                });    }

    public boolean give(OfflinePlayer offlinePlayer, Integer amount) {
        getCratesPlus().getCrateHandler().giveCrateKey(offlinePlayer, getName(false), amount);
        return true;
    }

    public double getKnockback() {
        return knockback;
    }

    public void openPreviewGUI(Player player) {
        List<Winning> winnings = this.getWinnings();
        GUI previewGUI = new GUI(this.getName(true) + " " + MessageHandler.getMessage("Possible Wins:", null, this, null));
        for (Winning winning : winnings) {
            ItemStack itemStack = winning.getPreviewItemStack();
            if (itemStack == null)
                continue;
            previewGUI.addItem(itemStack);
        }
        previewGUI.open(player);
    }

}
