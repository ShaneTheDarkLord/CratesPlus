package plus.crates.storage;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import plus.crates.CratesPlus;
import plus.crates.Utils.LinfootUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.concurrent.CompletableFuture;

public class FlatStorageHandler implements IStorageHandler {

    private final CratesPlus cratesPlus;

    private File flatFile;
    private YamlConfiguration flatConfig;

    public FlatStorageHandler(CratesPlus cratesPlus) {
        this.cratesPlus = cratesPlus;
        setupStorage();
    }

    private void setupStorage() {
        // Configure the flat file no matter what, we'll still use this for "Crate Locations" and maybe other data that is per instance
        flatFile = new File(cratesPlus.getDataFolder(), "data.yml");
        flatConfig = YamlConfiguration.loadConfiguration(flatFile);
        saveFlat();
        updateDataFile();
    }

    private void updateDataFile() {
        if (!flatConfig.isSet("Data Version") || flatConfig.getInt("Data Version") == 1) {
            flatConfig.set("Data Version", 2);
            if (flatConfig.isSet("Crate Locations"))
                flatConfig.set("Crate Locations", null);

            saveFlat();
        }
    }

    public void saveFlat() {
        try {
            flatConfig.save(flatFile);
        } catch (IOException e) {
            cratesPlus.getLogger().log(Level.SEVERE, "Failed to save storage", e);
        }
    }

    public CompletableFuture<Integer> getPlayerData(UUID uuid, String key) {
        try {
            return CompletableFuture.completedFuture(flatConfig.getInt("Player." + uuid.toString() + "." + key));
        } catch (Throwable throwable) {
            CompletableFuture<Integer> future = new CompletableFuture<>();
            future.completeExceptionally(throwable);
            return future;
        }
    }
    @Override
    public CompletableFuture<Void> setPlayerData(UUID uuid, String key, String value) {
        try {
            flatConfig.set("Player." + uuid.toString() + "." + key, value);
            saveFlat();

            return CompletableFuture.completedFuture(null);
        } catch (Throwable throwable) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(throwable);
            return future;
        }
    }

    @Override
    public CompletableFuture<Void> incPlayerData(UUID uuid, String key, Integer value) {
        try {
            int current = flatConfig.getInt("Player." + uuid.toString() + "." + key, 0);
            if (value > 0) {
                current += value;
            } else if (value < 0) {
                current -= value;
            }
            flatConfig.set("Player." + uuid.toString() + "." + key, current);
            saveFlat();


            return CompletableFuture.completedFuture(null);
        } catch (Throwable throwable) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(throwable);
            return future;
        }
    }
    @Override
    public CompletableFuture<Void> removeCrateLocation(String crate, Location location) {
        try {
            List<String> locations = getLocations(crate);
            if (locations != null) locations.remove(LinfootUtil.formatLocation(location));
            flatConfig.set("Crate Locations." + crate, locations);
            saveFlat();

            return CompletableFuture.completedFuture(null);
        } catch (Throwable throwable) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(throwable);
            return future;
        }
    }
    @Override
    public CompletableFuture<Void> addCrateLocation(String crate, Location location) {
        try {
            List<String> locations = getLocations(crate);
            if (locations != null) locations.remove(LinfootUtil.formatLocation(location));

            flatConfig.set("Crate Locations." + crate, locations);
            saveFlat();

            return CompletableFuture.completedFuture(null);
        } catch (Throwable throwable) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(throwable);
            return future;
        }
    }

    private List<String> getLocations(String crate) {
        String key = "Crate Locations." + crate;
        if (flatConfig.isSet(key)) return flatConfig.getStringList(key);
        return null;
    }

    @Override
    public CompletableFuture<Map<UUID, Map<String, Integer>>> getPendingKeys() {
        try {
            if (!flatConfig.isSet("Claims"))
                return CompletableFuture.completedFuture(null);

            Map<UUID, Map<String, Integer>> map = new HashMap<>();
            for (String uuidStr : flatConfig.getConfigurationSection("Claims").getKeys(false)) {
                List<String> dataList = flatConfig.getStringList("Claims." + uuidStr);
                if (dataList.isEmpty()) continue;

                Map<String, Integer> keys = new HashMap<>();
                for (String data : dataList) {
                    String[] args = data.split("\\|");
                    if (args.length == 1) {
                        keys.put(args[0], 1);
                    } else {
                        keys.put(args[0], Integer.valueOf(args[1]));
                    }
                }

                map.put(UUID.fromString(uuidStr), keys);
            }

            return CompletableFuture.completedFuture(map);
        }
        catch (Throwable throwable) {
            CompletableFuture<Map<UUID, Map<String, Integer>>> future = new CompletableFuture<>();
            future.completeExceptionally(throwable);
            return future;
        }
    }

    @Override
    public CompletableFuture<Void> updateKeysData(UUID uuid, Map<String, Integer> keys) {
        try {
            List<String> data = new ArrayList<>();
            for (Map.Entry<String, Integer> key : keys.entrySet()) {
                data.add(key.getKey() + "|" + key.getValue());
            }
            flatConfig.set("Claims." + uuid.toString(), data);
            saveFlat();

            return CompletableFuture.completedFuture(null);
        } catch (Throwable throwable) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(throwable);
            return future;
        }
    }

}