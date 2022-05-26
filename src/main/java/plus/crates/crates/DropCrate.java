package plus.crates.crates;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import plus.crates.CratesPlus;
import plus.crates.handlers.ConfigHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Drop it like it's hot!
 */
public class DropCrate extends SupplyCrate implements Listener {
    private Random rand = new Random();
    private final List<Location> drops = new ArrayList<>();
    private List<String> worlds = new ArrayList<>();
    private int minSpawnInterval = 60 * 60; // 1 Hour
    private int maxSpawnInterval = 120 * 60; // 2 Hours
    private int radiusClosestToPlayer = 300; // will spawn 300 blocks close to a player TODO Make this and any others configurable!
    private int despawnTimer = 30 * 60; // will despawn after 30 minutes if nobody has found it
    private int minPlayers = 2;

    public DropCrate(ConfigHandler configHandler, String name) {
        super(configHandler, name);
        loadCrateMore();
        startTimer();
        getCratesPlus().getServer().getPluginManager().registerEvents(this, getCratesPlus());
    }

    private void loadCrateMore() {
        CratesPlus cratesPlus = getCratesPlus();
        if (cratesPlus.getConfig().isSet("Crates." + name + ".Worlds"))
            this.worlds = cratesPlus.getConfig().getStringList("Crates." + name + ".Worlds");

        if (cratesPlus.getConfig().isSet("Crates." + name + ".Min Spawn Interval"))
            this.minSpawnInterval = cratesPlus.getConfig().getInt("Crates." + name + ".Min Spawn Interval", this.minSpawnInterval);

        if (cratesPlus.getConfig().isSet("Crates." + name + ".Max Spawn Interval"))
            this.maxSpawnInterval = cratesPlus.getConfig().getInt("Crates." + name + ".Max Spawn Interval", this.maxSpawnInterval);

        if (cratesPlus.getConfig().isSet("Crates." + name + ".Min Players"))
            this.minPlayers = cratesPlus.getConfig().getInt("Crates." + name + ".Min Players", this.minPlayers);

        if (cratesPlus.getConfig().isSet("Crates." + name + ".Despawn Timer"))
            this.despawnTimer = cratesPlus.getConfig().getInt("Crates." + name + ".Despawn Timer", this.despawnTimer);
    }

    private void startTimer() {
        int timer = ThreadLocalRandom.current().nextInt(minSpawnInterval, maxSpawnInterval + 1);
        // TODO Use a debug option to show this?
        getCratesPlus().getLogger().info("Will attempt to drop crate \"" + getName() + "\" in " + timer + " seconds!");
        // TODO Should we validate the config first? I feel like we should...
        Bukkit.getScheduler().runTaskLater(getCratesPlus(), this::spawnCrate, 20L * timer);
    }

    private void spawnCrate() {
        startTimer(); // Start timer for the next drop
        if (Bukkit.getOnlinePlayers().size() < minPlayers) {
            return; // Not enough players online :(
        }

        List<String> worlds = new ArrayList<>();
        for (String worldName : this.worlds) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            // No players in world
            if (world.getPlayers().size() < minPlayers) continue;

        }

        if (worlds.isEmpty()) return;


        World world = Bukkit.getWorld(worlds.get(ThreadLocalRandom.current().nextInt(worlds.size())));
        if (world == null) return;

        Player player = world.getPlayers().get(ThreadLocalRandom.current().nextInt(world.getPlayers().size())); // Get random player to spawn crate near
        Location location = player.getLocation().clone();
        double randomX = ThreadLocalRandom.current().nextInt((int) location.getX() - radiusClosestToPlayer, (int) location.getX() + radiusClosestToPlayer + 1);
        double randomZ = ThreadLocalRandom.current().nextInt((int) location.getZ() - radiusClosestToPlayer, (int) location.getZ() + radiusClosestToPlayer + 1);
        location.setX(randomX);
        location.setZ(randomZ);
        location = world.getHighestBlockAt(location).getLocation().clone().add(0, 0, 0);
        if (location.getBlock().getType().equals(Material.AIR)) {
            location.getBlock().setType(getBlock());
            // TODO idk how to handle the below, is it even needed with 1.13? So reflection if thats the case...
//            location.getBlock().setData((byte) getBlockData());
            System.out.println("Crate dropped at " + location.toString());
            drops.add(location);
            // TODO Broadcast, populate and what not

            if (despawnTimer > 0) {
                // TODO Despawn
                Location finalLocation = location;
                Bukkit.getScheduler().runTaskLater(getCratesPlus(), () -> {
                    if (drops.contains(finalLocation)) {
                        drops.remove(finalLocation);
                        finalLocation.getBlock().setType(Material.AIR);
                    }
                }, despawnTimer * 20L);
            }
        }
    }

    private void openCrate(Player player, Location location) {
        drops.remove(location);
        handleWin(player, location.getBlock());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == getBlock() && drops.contains(event.getClickedBlock().getLocation())) {
            openCrate(event.getPlayer(), event.getClickedBlock().getLocation());
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        drops.forEach(location -> location.getBlock().setType(Material.AIR));
    }
}
