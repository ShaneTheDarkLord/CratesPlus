package plus.crates;

import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import plus.crates.commands.CrateCommand;
import plus.crates.handlers.*;
import plus.crates.listeners.BlockListeners;
import plus.crates.listeners.GUIListeners;
import plus.crates.listeners.PlayerInteract;
import plus.crates.listeners.PlayerJoin;
import plus.crates.storage.FlatStorageHandler;
import plus.crates.storage.IStorageHandler;
import plus.crates.Utils.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CratesPlus extends JavaPlugin implements Listener {
    private static CratesPlus plugin;
    private String pluginPrefix = "";
    private String updateMessage = "";
    private String configBackup = null;
    private boolean updateAvailable = false;
    private ConfigHandler configHandler;
    private CrateHandler crateHandler;
    private SettingsHandler settingsHandler;
    private HologramHandler hologramHandler;
    private IStorageHandler storageHandler;
    private String bukkitVersion = "0.0";
    private Version_Util version_util;
    private static OpenHandler openHandler;
    private final List<UUID> creatingCrate = new ArrayList<>();

    public void onEnable() {
        plugin = this;
        Server server = getServer();
        Pattern pattern = Pattern.compile("(^[^\\-]*)");
        Matcher matcher = pattern.matcher(server.getBukkitVersion());
        if (!matcher.find()) {
            getLogger().severe("Could not find Bukkit version... Disabling plugin");
            setEnabled(false);
            return;
        }
        bukkitVersion = matcher.group(1);

        if (getConfig().isSet("Bukkit Version"))
            bukkitVersion = getConfig().getString("Bukkit Version");

        assert bukkitVersion != null;
        if (LinfootUtil.versionCompare(bukkitVersion, "1.14.2") > 0) {
            // This means the plugin is using something newer than the latest tested build... we'll show a warning but carry on as usual
            getLogger().warning("CratesPlus has not yet been officially tested with Bukkit " + bukkitVersion + " but should still work");
        }

        if (LinfootUtil.versionCompare(bukkitVersion, "1.9") > -1) {
            // Use 1.9+ Util
            version_util = new Version_1_9(this);
        } else if (LinfootUtil.versionCompare(bukkitVersion, "1.8") > -1) {
            // Use 1.8 Util
            version_util = new Version_1_8(this);
        } else if (LinfootUtil.versionCompare(bukkitVersion, "1.7") > -1) {
            // Use Default Util
            version_util = new Version_Util(this);
        } else {
            if (!getConfig().isSet("Ignore Version") || !getConfig().getBoolean("Ignore Version")) { // People should only ignore this in the case of an error, doing an ignore on a unsupported version could break something
                setEnabled(false);
                return;
            }
            version_util = new Version_Util(this); // Use the 1.7 util? Probably has a lower chance of breaking
        }

        final ConsoleCommandSender console = server.getConsoleSender();
        getConfig().options().copyDefaults(true);
        saveConfig();

        hologramHandler = new HologramHandler();
        storageHandler = new FlatStorageHandler(this);
        // Load new messages.yml
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try {
                messagesFile.createNewFile();
                InputStream inputStream = getResource("messages.yml");
                OutputStream outputStream = new FileOutputStream(messagesFile);
                assert inputStream != null;
                ByteStreams.copy(inputStream, outputStream);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to load messages.yml", e);
            }
        }

        YamlConfiguration messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        MessageHandler.loadMessageConfiguration(this, messagesConfig, messagesFile);

        configHandler = new ConfigHandler(getConfig(), this);

        if (getConfig().getBoolean("Metrics")) {
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();

                MetricsCustom metricsCustom = new MetricsCustom(this);
                metricsCustom.start();
            } catch (IOException e) {
                // Failed to submit the stats :-(
            }
        }

        // Load the crate handler
        crateHandler = new CrateHandler(this);

        // Do Prefix
        pluginPrefix = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("Prefix", "&8[&6Complex&eCrates&8] &r")));

        // Register /crate command
        Objects.requireNonNull(Bukkit.getPluginCommand("crate")).setExecutor(new CrateCommand(this));

        // Register Events
        Bukkit.getPluginManager().registerEvents(new BlockListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoin(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListeners(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteract(this), this);

        openHandler = new OpenHandler(this);

        settingsHandler = new SettingsHandler(this);

        console.sendMessage(ChatColor.AQUA + getDescription().getName() + " Version " + getDescription().getVersion());

        switch (getHologramHandler().getHologramPlugin()) {
            default:
            case NONE:
                console.sendMessage(ChatColor.RED + "Unable to find compatible Hologram plugin, holograms will not work!");
                break;
            case HOLOGRAPHIC_DISPLAYS:
                console.sendMessage(ChatColor.GREEN + "HolographicDisplays was found, hooking in!");
                break;
            case INDIVIDUAL_HOLOGRAMS:
                console.sendMessage(ChatColor.GREEN + "IndividualHolograms was found, hooking in!");
                break;
        }

        if (configBackup != null && Bukkit.getOnlinePlayers().size() > 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("cratesplus.admin")) {
                    player.sendMessage(pluginPrefix + ChatColor.GREEN + "Your config has been updated. Your old config was backed up to " + configBackup);
                    configBackup = null;
                }
            }
        }
    }

    public void onDisable() {
        getConfigHandler().getCrates().forEach((key, crate) -> crate.onDisable());
    }

    public String uploadConfig() {
        return uploadFile("config.yml");
    }

    public String uploadData() {
        return uploadFile("data.yml");
    }

    public String uploadMessages() {
        return uploadFile("messages.yml");
    }

    public String uploadFile(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists())
            return null;
        LineIterator it;
        StringBuilder lines = new StringBuilder();
        try {
            it = FileUtils.lineIterator(file, "UTF-8");
            try {
                while (it.hasNext()) {
                    String line = it.nextLine();
                    lines.append(line).append("\n");
                }
            } finally {
                it.close();
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "failed to upload file", e);
        }
        return MCDebug.paste(fileName, lines.toString());
    }
    public void reloadPlugin() {
        reloadConfig();

        // Do Prefix
        pluginPrefix = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getConfig().getString("Prefix", "&8&l[&3&lKoffee&b&lCrates&8&l] &r")));

        // Reload Configuration
        configHandler = new ConfigHandler(getConfig(), this);

        // Settings Handler
        settingsHandler = new SettingsHandler(this);

    }


    public SettingsHandler getSettingsHandler() {
        return settingsHandler;
    }

    public String getPluginPrefix() {
        return pluginPrefix;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public HologramHandler getHologramHandler() {
        return hologramHandler;
    }

    public IStorageHandler getStorageHandler() {
        return storageHandler;
    }

    public String getUpdateMessage() {
        return updateMessage;
    }

    public String getConfigBackup() {
        return configBackup;
    }

    public void setConfigBackup(String configBackup) {
        this.configBackup = configBackup;
    }

    public Version_Util getVersion_util() {
        return version_util;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public CrateHandler getCrateHandler() {
        return crateHandler;
    }

    public static OpenHandler getOpenHandler() {
        return openHandler;
    }

    public String getBukkitVersion() {
        return bukkitVersion;
    }

    public boolean isCreating(UUID uuid) {
        return creatingCrate.contains(uuid);
    }

    public void addCreating(UUID uuid) {
        creatingCrate.add(uuid);
    }

    public void removeCreating(UUID uuid) {
        creatingCrate.remove(uuid);
    }

    public static CratesPlus getInstance() { return plugin; }

    public void logToFile(String message)

    {

        try
        {
            File dataFolder = getDataFolder();
            if(!dataFolder.exists())
            {
                dataFolder.mkdir();
            }

            File saveTo = new File(getDataFolder(), "openings.txt");
            if (!saveTo.exists())
            {
                saveTo.createNewFile();
            }


            FileWriter fw = new FileWriter(saveTo, true);

            PrintWriter pw = new PrintWriter(fw);

            pw.println(message);

            pw.flush();

            pw.close();

        } catch (IOException e)
        {

            e.printStackTrace();

        }

    }
}
