package plus.crates.handlers;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import plus.crates.crates.Crate;
import plus.crates.crates.Key;
import plus.crates.crates.KeyCrate;
import plus.crates.CratesPlus;
import plus.crates.opener.Opener;
import plus.crates.Utils.LegacyMaterial;
import plus.crates.Utils.LinfootUtil;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CrateHandler {

    private final CratesPlus cratesPlus;

    private final Map<UUID, Opener> openings = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> pendingKeys = new HashMap<>();

    public CrateHandler(CratesPlus cratesPlus) {
        this.cratesPlus = cratesPlus;

        Map<UUID, Map<String, Integer>> pendingKeysData = cratesPlus.getStorageHandler().getPendingKeys().join(); // todo
        if (pendingKeysData != null) {
            pendingKeys.putAll(pendingKeysData);
        }
    }

    public void spawnFirework(Location location) {
        Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        Random r = ThreadLocalRandom.current();
        int rt = r.nextInt(4) + 1;
        FireworkEffect.Type type = FireworkEffect.Type.BALL;
        if (rt == 1) type = FireworkEffect.Type.BALL;
        if (rt == 2) type = FireworkEffect.Type.BALL_LARGE;
        if (rt == 3) type = FireworkEffect.Type.BURST;
        if (rt == 4) type = FireworkEffect.Type.CREEPER;
        if (rt == 5) type = FireworkEffect.Type.STAR;
        int r1i = r.nextInt(17) + 1;
        int r2i = r.nextInt(17) + 1;
        Color c1 = LinfootUtil.getColor(r1i);
        Color c2 = LinfootUtil.getColor(r2i);
        FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();
        fwm.addEffect(effect);
        int rp = r.nextInt(2) + 1;
        fwm.setPower(rp);
        fw.setFireworkMeta(fwm);
    }

    public void giveCrateKey(OfflinePlayer offlinePlayer) {
        Set<String> crates = Objects.requireNonNull(cratesPlus.getConfig().getConfigurationSection("Crates")).getKeys(false);
        String crateType = LinfootUtil.getRandomEntry(crates);
        giveCrateKey(offlinePlayer, crateType);
    }

    public void giveCrateKey(OfflinePlayer offlinePlayer, String crateType) {
        giveCrateKey(offlinePlayer, crateType, 1);
    }

    public void giveCrateKey(OfflinePlayer offlinePlayer, String crateType, int amount) {
        giveCrateKey(offlinePlayer, crateType, amount, true);
    }

    public void giveCrateKey(OfflinePlayer offlinePlayer, String crateType, int amount, boolean showMessage) {
        giveCrateKey(offlinePlayer, crateType, amount, showMessage, false);
    }

    public void giveCrateKey(OfflinePlayer offlinePlayer, String crateType, int amount, boolean showMessage, boolean forceClaim) {
        if (offlinePlayer == null) return;

        if (crateType == null) {
            giveCrateKey(offlinePlayer);
            return;
        }

        if (!(cratesPlus.getConfigHandler().getCrates().get(crateType.toLowerCase()) instanceof KeyCrate))
            return;

        KeyCrate crate = (KeyCrate) cratesPlus.getConfigHandler().getCrates().get(crateType.toLowerCase());
        if (crate == null) {
            if (offlinePlayer.isOnline())
                ((Player) offlinePlayer).sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Crate type: '" + crateType + "' does not exist");
            return;
        }

        Key key = crate.getKey();
        if (key == null) {
            if (offlinePlayer.isOnline()) {
                ((Player) offlinePlayer).sendMessage(cratesPlus.getPluginPrefix() + ChatColor.RED + "Could not get key for crate: '" + crateType + "'");
            }
            return;
        }

        if (offlinePlayer.isOnline()) {
            Player player = (Player) offlinePlayer;

            // Check if inventory is full, if so add it to the claim area. Or if forceClaim is true
            if (player.getInventory().firstEmpty() == -1 || forceClaim) {
                Map<String, Integer> keys = new HashMap<>();
                if (hasPendingKeys(player.getUniqueId()))
                    keys = getPendingKey(player.getUniqueId());
                if (keys.containsKey(crateType))
                    amount = amount + keys.get(crateType);
                keys.put(crateType, amount);
                pendingKeys.put(player.getUniqueId(), keys);
                updateKeysData(offlinePlayer.getUniqueId());
                if (showMessage)
                    MessageHandler.sendMessage(player, "&aYour inventory is full, you can claim your keys later using /crate", crate, null);
                return;
            }

            ItemStack keyItem = key.getKeyItem(amount);
            Map<Integer, ItemStack> remaining = player.getInventory().addItem(keyItem);
            int amountLeft = 0;
            for (Map.Entry<Integer, ItemStack> item : remaining.entrySet()) {
                amountLeft += item.getValue().getAmount();
            }
            if (amountLeft > 0) {
                giveCrateKey(offlinePlayer, crateType, amountLeft); // Should put rest into the claim area
            }

            if (showMessage)
                MessageHandler.sendMessage(player, "&aYou have been given a %crate% &acrate key", crate, null);
        } else {
            Map<String, Integer> keys = new HashMap<>();
            if (hasPendingKeys(offlinePlayer.getUniqueId()))
                keys = getPendingKey(offlinePlayer.getUniqueId());
            if (keys.containsKey(crateType))
                amount = amount + keys.get(crateType);
            keys.put(crateType, amount);
            pendingKeys.put(offlinePlayer.getUniqueId(), keys);
            updateKeysData(offlinePlayer.getUniqueId());
        }
    }

    private void updateKeysData(UUID uuid) {
        Map<String, Integer> keys = pendingKeys.get(uuid);
        cratesPlus.getStorageHandler().updateKeysData(uuid, keys);

    }

    @Deprecated
    public void giveCrate(Player player, String crateType) {
        Crate crate = cratesPlus.getConfigHandler().getCrates().get(crateType.toLowerCase());
        if (crate == null) return;
        giveCrate(player, crate);
    }

    public void giveCrate(Player player, Crate crate) {
        if (player == null || !player.isOnline() || crate == null) return;

        ItemStack crateItem = new ItemStack(crate.getBlock(), 1, (short) crate.getBlockData());
        ItemMeta crateMeta = crateItem.getItemMeta();
        assert crateMeta != null;
        crateMeta.setDisplayName(crate.getName(true) + " Crate");
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Place this crate somewhere!");
        lore.add("");
        crateMeta.setLore(lore);
        crateItem.setItemMeta(crateMeta);
        player.getInventory().addItem(crateItem);
        player.sendMessage(cratesPlus.getPluginPrefix() + ChatColor.GREEN + "You have been given a " + crate.getName(true) + ChatColor.GREEN + " crate");
    }

    @Deprecated
    public ItemStack stringToItemstackOld(String i) {
        String[] args = i.split(":", -1);
        if (args.length >= 2 && args[0].equalsIgnoreCase("command")) {
            /** Commands */
            String command = args[1];
            String title = "Command: /" + command;
            if (args.length == 3) {
                title = args[2];
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            ItemStack itemStack = new ItemStack(LegacyMaterial.EMPTY_MAP.getMaterial());
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.RESET + title);
            itemStack.setItemMeta(itemMeta);
            return itemStack;
        } else if (args.length == 1) {
            /** Item without any amounts or enchantments */
            String[] args1 = args[0].split("-");
            ItemStack itemStack;
            if (args1.length == 1) {
                itemStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(args1[0].toUpperCase())));
            } else {
                itemStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(args1[0].toUpperCase())), 1, Byte.parseByte(args1[1]));
            }
            return itemStack;
        } else if (args.length == 2) {
            return new ItemStack(Objects.requireNonNull(Material.getMaterial(args[0].toUpperCase())), Integer.parseInt(args[1]));
        } else if (args.length == 3) {
            String[] enchantments = args[2].split("\\|", -1);
            ItemStack itemStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(args[0])), Integer.parseInt(args[1]));
            for (String e : enchantments) {
                String[] args1 = e.split("-", -1);
                if (args1.length == 1) {
                    try {
                        itemStack.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByName(args1[0])), 1);
                    } catch (Exception ignored) {
                    }
                } else if (args1.length == 2) {
                    try {
                        itemStack.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByName(args1[0])), Integer.parseInt(args1[1]));
                    } catch (Exception ignored) {
                    }
                }
            }
            return itemStack;
        }
        return null;
    }

    public ItemStack stringToItemstack(String i, Player player, boolean isWin) {
        try {
            String[] args = i.split(":", -1);
            if (args.length >= 2 && args[0].equalsIgnoreCase("command")) {
                String name = "Command";
                String commands;
                if (args.length >= 3 && !args[1].equalsIgnoreCase("NONE")) {
                    name = ChatColor.translateAlternateColorCodes('&', args[1]);
                    commands = args[2];
                } else {
                    commands = args[1];
                }

                if (isWin) {
                    /** Do Commands */
                    String[] args1 = commands.split("\\|");
                    for (String command : args1) {
                        player.sendMessage(command);
                        command = command.replaceAll("%name%", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }
                }

                ItemStack itemStack = new ItemStack(LegacyMaterial.EMPTY_MAP.getMaterial());
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = new ArrayList<String>();
                lore.add(ChatColor.DARK_GRAY + "Crate Command");
                itemMeta.setLore(lore);
                itemMeta.setDisplayName(ChatColor.RESET + name);
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            } else if (args.length == 1) {
                /** Item without any amounts, custom name or enchantments */
                String[] args1 = args[0].split("-");
                ItemStack itemStack;
                if (args1.length == 1) {
                    itemStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(args1[0].toUpperCase())));
                } else {
                    itemStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(args1[0].toUpperCase())), 1, Byte.parseByte(args1[1]));
                }
                return itemStack;
            } else if (args.length == 2) {
                String[] args1 = args[0].split("-");
                if (args1.length == 1) {
                    return new ItemStack(Objects.requireNonNull(Material.getMaterial(args1[0].toUpperCase())), Integer.parseInt(args[1]));
                } else {
                    return new ItemStack(Objects.requireNonNull(Material.getMaterial(args1[0].toUpperCase())), Integer.parseInt(args[1]), Byte.parseByte(args1[1]));
                }
            } else if (args.length == 3) {
                if (args[2].equalsIgnoreCase("NONE")) {
                    String[] args1 = args[0].split("-");
                    if (args1.length == 1) {
                        return new ItemStack(Objects.requireNonNull(Material.getMaterial(args1[0].toUpperCase())), Integer.parseInt(args[1]));
                    } else {
                        return new ItemStack(Objects.requireNonNull(Material.getMaterial(args1[0].toUpperCase())), Integer.parseInt(args[1]), Byte.parseByte(args1[1]));
                    }
                } else {
                    String[] args1 = args[0].split("-");
                    ItemStack itemStack;
                    if (args1.length == 1) {
                        itemStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(args1[0].toUpperCase())), Integer.parseInt(args[1]));
                    } else {
                        itemStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(args1[0].toUpperCase())), Integer.parseInt(args[1]), Byte.parseByte(args1[1]));
                    }
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    assert itemMeta != null;
                    itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', args[2]));
                    itemStack.setItemMeta(itemMeta);
                    return itemStack;
                }
            } else if (args.length == 4) {
                String[] enchantments = args[3].split("\\|", -1);
                String[] args1 = args[0].split("-");
                ItemStack itemStack;
                if (args1.length == 1) {
                    itemStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(args1[0].toUpperCase())), Integer.parseInt(args[1]));
                } else {
                    itemStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(args1[0].toUpperCase())), Integer.parseInt(args[1]), Byte.parseByte(args1[1]));
                }
                for (String e : enchantments) {
                    args1 = e.split("-", -1);
                    if (args1.length == 1) {
                        try {
                            itemStack.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByName(args1[0])), 1);
                        } catch (Exception ignored) {
                        }
                    } else if (args1.length == 2) {
                        try {
                            itemStack.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByName(args1[0])), Integer.parseInt(args1[1]));
                        } catch (Exception ignored) {
                        }
                    }
                }
                if (!args[2].equalsIgnoreCase("NONE")) {
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    assert itemMeta != null;
                    itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', args[2]));
                    itemStack.setItemMeta(itemMeta);
                }
                return itemStack;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public String itemstackToString(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return null;

        StringBuilder finalString = new StringBuilder();
        finalString.append(itemStack.getType().toString());
        if (itemStack.getData().getData() != 0) {
            finalString.append("-").append(itemStack.getData().getData());
        }
        finalString.append(":").append(itemStack.getAmount());

        if (itemStack.hasItemMeta() && Objects.requireNonNull(itemStack.getItemMeta()).hasDisplayName()) {
            finalString.append(":").append(itemStack.getItemMeta().getDisplayName());
        } else {
            finalString.append(":NONE");
        }

        int i = 0;
        for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer level = entry.getValue();
            if (i == 0) {
                finalString.append(":");
            } else {
                finalString.append("|");
            }
            if (level > 1) {
                finalString.append(enchantment.getName().toUpperCase()).append("-").append(level);
            } else {
                finalString.append(enchantment.getName().toUpperCase());
            }
            i++;
        } return finalString.toString();
    }

    public Map<UUID, Opener> getOpenings() {
        return openings;
    }

    public boolean hasOpening(UUID uuid) {
        return getOpening(uuid) != null;
    }

    public Opener getOpening(UUID uuid) {
        if (openings.containsKey(uuid))
            return openings.get(uuid);
        return null;
    }

    public void addOpening(UUID uuid, Opener opener) {
        openings.put(uuid, opener);
    }

    public void removeOpening(UUID uuid) {
        if (hasOpening(uuid))
            openings.remove(uuid);
    }

    public Map<UUID, Map<String, Integer>> getPendingKeys() {
        return pendingKeys;
    }

    public Map<String, Integer> getPendingKey(UUID uuid) {
        if (!hasPendingKeys(uuid))
            return null;
        return pendingKeys.get(uuid);
    }

    public boolean hasPendingKeys(UUID uuid) {
        return pendingKeys.containsKey(uuid) && !pendingKeys.get(uuid).isEmpty();
    }

    public void claimKey(UUID uuid, String crate) {
        Map<String, Integer> keys = pendingKeys.get(uuid);
        Integer amount = keys.get(crate);
        keys.remove(crate);
        pendingKeys.put(uuid, keys);
        updateKeysData(uuid);
        giveCrateKey(Bukkit.getOfflinePlayer(uuid), crate, amount, false);
    }

}
