package plus.crates.storage;

import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IStorageHandler {

    CompletableFuture<Integer> getPlayerData(UUID uuid, String key);

    CompletableFuture<Void> setPlayerData(UUID uuid, String key, String value);

    CompletableFuture<Void> incPlayerData(UUID uuid, String key, Integer value);

    CompletableFuture<Void> removeCrateLocation(String crate, Location location);

    CompletableFuture<Void> addCrateLocation(String crate, Location location);

    CompletableFuture<Map<UUID, Map<String, Integer>>> getPendingKeys();

    CompletableFuture<Void> updateKeysData(UUID uuid, Map<String, Integer> keys);
}
