package plus.crates.crates;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plus.crates.CratesPlus;
import plus.crates.handlers.ConfigHandler;
import plus.crates.Utils.GUI;

import java.util.List;
import java.util.UUID;

/**
 * Oh this is a fun one...
 */
public class MysteryCrate extends Crate {
    private String guiTitle = null;
    private String itemTitle = null;
    private List<String> lore = null;

    public MysteryCrate(ConfigHandler configHandler, String name) {
        super(configHandler, name);
        loadCrate();
    }

    protected void loadCrate() {
        CratesPlus cratesPlus = getCratesPlus();
        if (cratesPlus.getConfig().isSet("Crates." + name + ".GUI Title"))
            this.guiTitle = ChatColor.translateAlternateColorCodes('&', cratesPlus.getConfig().getString("Crates." + name + ".GUI Title"));

        if (cratesPlus.getConfig().isSet("Crates." + name + ".Item Title"))
            this.itemTitle = ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', cratesPlus.getConfig().getString("Crates." + name + ".Item Title"));

        if (cratesPlus.getConfig().isSet("Crates." + name + ".Lore"))
            this.lore = cratesPlus.getConfig().getStringList("Crates." + name + ".Lore");
    }

    public boolean give(OfflinePlayer offlinePlayer, Integer amount) {
        CratesPlus cratesPlus = getCratesPlus();
        UUID uuid = offlinePlayer.getUniqueId();
        cratesPlus.getStorageHandler().incPlayerData(uuid, "Crates-" + getName(false), amount);
        return true;
    }

    public void openGUI(Player player) {
        CratesPlus cratesPlus = getCratesPlus();
        GUI gui = new GUI(guiTitle != null ? guiTitle : getName(false));

        ItemStack itemStack = new ItemStack(getBlock(), 1, (short) getBlockData());
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(itemTitle != null ? itemTitle : getName(true));
        itemStack.setItemMeta(itemMeta);

        Integer count = cratesPlus.getStorageHandler().getPlayerData(player.getUniqueId(), "Crates-" + getName(false)).join(); // TODO
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                final int finalI = i;
                gui.addItem(itemStack, (player1, gui1) -> {
                    player1.sendMessage(ChatColor.YELLOW + "// TODO");
                    player1.sendMessage(ChatColor.AQUA + "#" + finalI);
                });
            }
        }

        gui.setGoBackHandler((player12, gui12) -> player12.closeInventory());
        gui.open(player);
    }

}
