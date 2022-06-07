package plus.crates.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plus.crates.crates.Crate;
import plus.crates.crates.KeyCrate;
import plus.crates.crates.MysteryCrate;
import plus.crates.CratesPlus;
import plus.crates.crates.Winning;
import plus.crates.handlers.MessageHandler;
import plus.crates.opener.Opener;
import plus.crates.Utils.*;

import java.lang.reflect.Constructor;
import java.util.Map;

public class CrateCommand implements CommandExecutor {
    private final CratesPlus cratesPlus;

    public CrateCommand(CratesPlus cratesPlus) {
        this.cratesPlus = cratesPlus;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String string, String[] args) {

        if (sender instanceof Player && !sender.hasPermission("cratesplus.claim")) {
            if (args.length == 0 || args[0].equalsIgnoreCase("claim")) {
                // Assume player and show "claim" GUI
                doClaim((Player) sender);
                return true;
            }
            sender.sendMessage(cratesPlus.getPluginPrefix() + MessageHandler.getMessage("&cYou do not have the correct permission to run this command", (Player) sender, null, null));
            return false;
        }

        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                default:
                    sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Unknown arg");
                    break;
                case "testmessages":
                    MessageHandler.testMessages = !MessageHandler.testMessages;
                    sender.sendMessage(ChatColor.GREEN + "Test Messages " + (MessageHandler.testMessages ? "ENABLED" : "DISABLED"));
                    break;
                case "testeggs":
                    Player player = null;
                    if (sender instanceof Player)
                        player = (Player) sender;

                    sender.sendMessage(ChatColor.AQUA + "Creating creeper egg...");
                    ItemStack itemStack = cratesPlus.getVersion_util().getSpawnEgg(EntityType.CREEPER, 1);
                    sender.sendMessage(ChatColor.AQUA + "Testing creeper egg...");
                    SpawnEggNBT spawnEggNBT = SpawnEggNBT.fromItemStack(itemStack);
                    if (spawnEggNBT.getSpawnedType().equals(EntityType.CREEPER)) {
                        sender.sendMessage(ChatColor.GREEN + "Creeper egg successful");
                        if (player != null)
                            player.getInventory().addItem(itemStack);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Creeper egg failed, please post console on GitHub");
                    }

                    sender.sendMessage(ChatColor.AQUA + "Creating spider egg...");
                    itemStack = cratesPlus.getVersion_util().getSpawnEgg(EntityType.SPIDER, 2);
                    sender.sendMessage(ChatColor.AQUA + "Testing spider egg...");
                    spawnEggNBT = SpawnEggNBT.fromItemStack(itemStack);
                    if (spawnEggNBT.getSpawnedType().equals(EntityType.SPIDER)) {
                        sender.sendMessage(ChatColor.GREEN + "Spider egg successful");
                        if (player != null)
                            player.getInventory().addItem(itemStack);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Spider egg failed, please post console on GitHub");
                    }

                    sender.sendMessage(ChatColor.AQUA + "Creating silverfish egg...");
                    itemStack = cratesPlus.getVersion_util().getSpawnEgg(EntityType.SILVERFISH, 3);
                    sender.sendMessage(ChatColor.AQUA + "Testing silverfish egg...");
                    spawnEggNBT = SpawnEggNBT.fromItemStack(itemStack);
                    if (spawnEggNBT.getSpawnedType().equals(EntityType.SILVERFISH)) {
                        sender.sendMessage(ChatColor.GREEN + "Silverfish egg successful");
                        if (player != null)
                            player.getInventory().addItem(itemStack);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Silverfish egg failed, please post console on GitHub");
                    }
                    break;
                case "claim":
                    if (sender instanceof Player) {
                        doClaim((Player) sender);
                    }
                    break;
                case "opener":
                case "openers":
                    if (args.length > 1) {
                        if (args.length < 3) {
                            sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Correct usage: /" + string + " " + args[0] + " <crate> <opener>");
                        } else {
                            if (CratesPlus.getOpenHandler().openerExist(args[2])) {
                                Opener opener = CratesPlus.getOpenHandler().getOpener(args[2]);
                                if (cratesPlus.getConfigHandler().getCrate(args[1].toLowerCase()) == null) {
                                    sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "No crate exists with that name");
                                } else if (!cratesPlus.getConfigHandler().getCrate(args[1].toLowerCase()).supportsOpener(opener)) {
                                    sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Opener does not support crate type");
                                } else {
//									cratesPlus.getConfigHandler().getCrate(args[1].toLowerCase()).setOpener(args[2]);
                                    sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.GREEN + "Set opener to " + args[2]);
                                }
                            } else {
                                sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "No opener is registered with that name");
                            }
                        }

                    } else {
                        sender.sendMessage(ChatColor.GOLD + "Registered Openers:");
                        sender.sendMessage(ChatColor.AQUA + "Name" + ChatColor.GRAY + " | " + ChatColor.YELLOW + "Plugin");
                        sender.sendMessage(ChatColor.AQUA + "");
                        for (Map.Entry<String, Opener> map : CratesPlus.getOpenHandler().getRegistered().entrySet()) {
                            sender.sendMessage(ChatColor.AQUA + map.getKey() + ChatColor.GRAY + " | " + ChatColor.YELLOW + map.getValue().getPlugin().getDescription().getName());
                        }
                    }
                    break;
                case "reload":
                    cratesPlus.reloadPlugin();
                    sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.GREEN + "CratesPlus was reloaded - This feature is not fully supported and may not work correctly");
                    break;
                case "settings":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "This command must be ran as a player");
                        return false;
                    }
                    cratesPlus.getSettingsHandler().openSettings((Player) sender);
                    break;
                case "create":
                    // TODO Handle different crate types lol, default is KeyCrate for now
                    if (sender instanceof Player && args.length < 2) {
                        // Lets try and open a sign to do the name! :D
                        player = (Player) sender;

                        cratesPlus.addCreating(player.getUniqueId());
                        try {
                            //Send fake sign cause 1.13
                            player.sendBlockChange(player.getLocation(), Material.valueOf("SIGN"), (byte) 0);

                            Constructor signConstructor = ReflectionUtil.getNMSClass("PacketPlayOutOpenSignEditor").getConstructor(ReflectionUtil.getNMSClass("BlockPosition"));
                            Object packet = signConstructor.newInstance(ReflectionUtil.getBlockPosition(player));
                            SignInputHandler.injectNetty(player);
                            ReflectionUtil.sendPacket(player, packet);

                            player.sendBlockChange(player.getLocation(), player.getLocation().getBlock().getType(), player.getLocation().getBlock().getData());
                        } catch (Exception e) {
                            e.printStackTrace();
                            cratesPlus.removeCreating(player.getUniqueId());
                        }
                        return true;
                    }

                    if (args.length < 2) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Correct Usage: /crate create <name>");
                        return false;
                    }

                    String name = args[1];
                    FileConfiguration config = cratesPlus.getConfig();
                    if (config.isSet("Crates." + name)) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + name + " crate already exists");
                        return false;
                    }

                    // Setup example item
                    config.set("Crates." + name + ".Winnings.1.Type", "ITEM");
                    config.set("Crates." + name + ".Winnings.1.Item Type", "IRON_SWORD");
                    config.set("Crates." + name + ".Winnings.1.Item Data", 0);
                    config.set("Crates." + name + ".Winnings.1.Percentage", 0);
                    config.set("Crates." + name + ".Winnings.1.Name", "&6&lExample Sword");
                    config.set("Crates." + name + ".Winnings.1.Amount", 1);

                    // Setup key with defaults
                    config.set("Crates." + name + ".Key.Item", "TRIPWIRE_HOOK");
                    config.set("Crates." + name + ".Key.Name", "%type% Crate Key");
                    config.set("Crates." + name + ".Key.Enchanted", true);

                    config.set("Crates." + name + ".Knockback", 0.0);
                    config.set("Crates." + name + ".Broadcast", false);
                    config.set("Crates." + name + ".Firework", false);
                    config.set("Crates." + name + ".Preview", true);
                    config.set("Crates." + name + ".Block", "CHEST");
                    config.set("Crates." + name + ".Color", "WHITE");
                    config.set("Crates." + name + ".Type", "KeyCrate");
                    cratesPlus.saveConfig();
                    cratesPlus.reloadConfig();

                    cratesPlus.getConfigHandler().registerCrate(cratesPlus, config, name);
                    cratesPlus.getSettingsHandler().setupCratesInventory();

                    sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.GREEN + name + " crate has been created");
                    break;
                case "rename":
                    if (args.length < 3) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Correct Usage: /crate rename <old name> <new name>");
                        return false;
                    }

                    String oldName = args[1];
                    String newName = args[2];

                    if (!cratesPlus.getConfigHandler().getCrates().containsKey(oldName.toLowerCase())) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + oldName + " crate was not found");
                        return false;
                    }
                    Crate crate = cratesPlus.getConfigHandler().getCrates().get(oldName.toLowerCase());

                    config = cratesPlus.getConfig();
                    if (config.isSet("Crates." + newName)) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + newName + " crate already exists");
                        return false;
                    }

                    LinfootUtil.copyConfigSection(config, "Crates." + crate.getName(), "Crates." + newName);

                    config.set("Crates." + crate.getName(), null);
                    cratesPlus.saveConfig();
                    cratesPlus.reloadConfig();

                    cratesPlus.getConfigHandler().getCrates().remove(oldName.toLowerCase());
                    cratesPlus.getConfigHandler().registerCrate(cratesPlus, config, newName);
                    cratesPlus.getSettingsHandler().setupCratesInventory();

                    sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.GREEN + oldName + " has been renamed to " + newName);
                    break;
                case "delete":
                    if (args.length < 2) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Correct Usage: /crate delete <name>");
                        return false;
                    }

                    name = args[1];
                    config = cratesPlus.getConfig();
                    if (!config.isSet("Crates." + name)) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + name + " crate doesn't exist");
                        return false;
                    }

                    config.set("Crates." + name, null);
                    cratesPlus.saveConfig();
                    cratesPlus.reloadConfig();
                    cratesPlus.getConfigHandler().getCrates().remove(name.toLowerCase());
                    cratesPlus.getSettingsHandler().setupCratesInventory();

                    sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.GREEN + name + " crate has been deleted");
                    break;
                case "mysterygui":
                    if (args.length < 2) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Correct Usage: /crate mysterygui <crate>");
                        return false;
                    }

                    String crateType = args[1];

                    crate = cratesPlus.getConfigHandler().getCrates().get(crateType.toLowerCase());
                    if (crate == null) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Crate not found");
                        return false;
                    }

                    if (!(crate instanceof MysteryCrate) || !(sender instanceof Player)) { // Too lazy to do separate messages
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Crate is not a Mystery Crate!");
                        return false;
                    }

                    ((MysteryCrate) crate).openGUI((Player) sender);
                    break;
                case "give":
                    if (sender.hasPermission("cratesplus.keygive")) {
                        if (args.length < 3) {
                            sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Correct Usage: /crate give <player/all/alloffline> <crate> [amount]");
                            return false;
                        }

                        Integer amount = 1;
                        if (args.length > 3) {
                            try {
                                amount = Integer.parseInt(args[3]);
                            } catch (Exception ignored) {
                                sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Invalid amount");
                                return false;
                            }
                        }

                        OfflinePlayer offlinePlayer = null;
                        if (!args[1].equalsIgnoreCase("all") && !args[1].equalsIgnoreCase("alloffline")) {
                            offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                            if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) { // Check if the player is online as "hasPlayedBefore" doesn't work until they disconnect?
                                sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "The player " + args[1] + " was not found");
                                return false;
                            }
                        }

                        crateType = args[2];

                        crate = cratesPlus.getConfigHandler().getCrates().get(crateType.toLowerCase());
                        if (crate == null) {
                            sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Crate not found");
                            return false;
                        }

                        if (offlinePlayer == null) {
                            if (args[1].equalsIgnoreCase("all")) {
                                crate.giveAll(amount);
                                sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.GREEN + "Given all online players a crate/key");
                            } else if (args[1].equalsIgnoreCase("alloffline")) {
                                /**
                                 * TODO TEST THIS and maybe give better explanation when they do `/crate give`?
                                 */
                                crate.giveAllOffline(amount);
                                sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.GREEN + "Given all online and offline players a crate/key");
                            }
                        } else {
                            if (crate.give(offlinePlayer, amount))
                                sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.GREEN + "Given " + offlinePlayer.getName() + " a crate/key");
                            else
                                sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Failed to give crate/key");
                        }
                    }
                    break;
                case "crate":
                case "keycrate":
                    if (args.length == 1) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Correct Usage: /crate crate <type> [player]");
                        return false;
                    }

                    if (args.length == 3) {
                        player = Bukkit.getPlayer(args[2]);
                    } else if (sender instanceof Player) {
                        player = (Player) sender;
                    } else {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Correct Usage: /crate crate <type> [player]");
                        return false;
                    }

                    if (player == null) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "The player " + args[2] + " was not found");
                        return false;
                    }

                    try {
                        crateType = args[1];
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Please specify a valid crate type");
                        return false;
                    }

                    if (cratesPlus.getConfigHandler().getCrates().get(crateType.toLowerCase()) == null || !(cratesPlus.getConfigHandler().getCrates().get(crateType.toLowerCase()) instanceof KeyCrate)) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "KeyCrate not found");
                        return false;
                    }

                    cratesPlus.getCrateHandler().giveCrate(player, crateType);

                    sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.GREEN + "Given " + player.getDisplayName() + ChatColor.RESET + ChatColor.GREEN + " a crate");
                    break;
                case "resetwinningcount":
                    if (args.length < 2) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED
                                + "Correct Usage: /crate resetWinningCount <type>");
                        return false;
                    }
                    Crate targetCrate = cratesPlus.getConfigHandler().getCrates().get(args[1].toLowerCase());
                    if (targetCrate == null) {
                        sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Crate not found");
                        return false;
                    }

                    cratesPlus.getConfig().set("Crates." + targetCrate.getName() + ".WinningCount", null);
                    for (Winning winning : targetCrate.getWinnings()) {
                        winning.resetWinningCount();
                    }
                    sender.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.GREEN + "Reset the winning count successfully");
                    break;
            }
        } else {
            // Help Messages
            sender.sendMessage("§e§l--------§6§l» §3§lKoffee§b§lCrates §fHelp §6§l«§e§l--------");
            sender.sendMessage("§6§l»§r §b/crate reload » §eReload configuration for KoffeeCrates");
            sender.sendMessage("§6§l»§r §b/crate create <name> » §eCreate a new crate");
            sender.sendMessage("§6§l»§r §b/crate rename <old name> <new name> » §eRename a crate");
            sender.sendMessage("§6§l»§r §b/crate delete <name> » §eDelete a crate from the config");
            sender.sendMessage("§6§l»§r §b/crate give <player/all> [crate] [amount] » §eGive player a crate/key, if no crate given it will be random");
            sender.sendMessage("§6§l»§r §b/crate crate <type> [player] » §eGive player a crate to be placed, for use by admins");
            sender.sendMessage("§6§l»§r §b/crate resetwinningcount <type> » §eReset specific crate's winning counts");
            sender.sendMessage("§e§l--------§6§l» §3§lKoffee§b§lCrates §fHelp §6§l«§e§l--------");
        }

        return true;
    }

    private void doClaim(Player player) {
        if (!cratesPlus.getCrateHandler().hasPendingKeys(player.getUniqueId())) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "You currently don't have any keys to claim");
            return;
        }
        GUI gui = new GUI("Claim Crate Keys");
        int i = 0;
        for (Map.Entry<String, Integer> map : cratesPlus.getCrateHandler().getPendingKey(player.getUniqueId()).entrySet()) {
            final String crateName = map.getKey();
            final KeyCrate crate = (KeyCrate) cratesPlus.getConfigHandler().getCrates().get(crateName.toLowerCase());
            if (crate == null)
                return; // Crate must have been removed?
            ItemStack keyItem = crate.getKey().getKeyItem(1);
            if (map.getValue() > 1) {
                ItemMeta itemMeta = keyItem.getItemMeta();
                itemMeta.setDisplayName(itemMeta.getDisplayName() + " x" + map.getValue());
                keyItem.setItemMeta(itemMeta);
            }
            gui.setItem(i, keyItem, new GUI.ClickHandler() {
                @Override
                public void doClick(Player player, GUI gui) {
                    cratesPlus.getCrateHandler().claimKey(player.getUniqueId(), crateName);
                    if (cratesPlus.getCrateHandler().hasPendingKeys(player.getUniqueId())) {
                        GUI.ignoreClosing.add(player.getUniqueId());
                        doClaim(player);
                    } else {
                        player.closeInventory();
                    }
                }
            });
            i++;
        }
        gui.open(player);
    }

}
