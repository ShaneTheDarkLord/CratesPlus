package plus.crates.Crates;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plus.crates.CratesPlus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplyCrate extends Crate {
	private List<String> lore = null;
	private Integer minimum = 1;
	private Integer maximum = 1;

	public SupplyCrate(CratesPlus cratesPlus, String name) {
		super(cratesPlus, name);
	}

	protected void loadCrate() {
		super.loadCrate();

		if (cratesPlus.getConfig().isSet("Crates." + name + ".Lore"))
			this.lore = cratesPlus.getConfig().getStringList("Crates." + name + ".Lore");

		if (cratesPlus.getConfig().isSet("Crates." + name + ".Minimum"))
			this.minimum = cratesPlus.getConfig().getInt("Crates." + name + ".Minimum");
		else
			this.minimum = 1;

		if (cratesPlus.getConfig().isSet("Crates." + name + ".Maximum"))
			this.maximum = cratesPlus.getConfig().getInt("Crates." + name + ".Maximum");
		else
			this.maximum = 1;

		if (this.maximum > this.getWinningsExcludeAlways().size())
			this.maximum = this.getWinningsExcludeAlways().size();

		if (this.minimum > this.getWinningsExcludeAlways().size())
			this.minimum = this.getWinningsExcludeAlways().size();

		if (this.minimum < 1)
			this.minimum = 1;

		if (this.maximum < this.minimum)
			this.maximum = this.minimum;
	}

	public void give(OfflinePlayer offlinePlayer, Integer amount) {
		if (offlinePlayer == null || !offlinePlayer.isOnline()) return; // TODO add offline player support
		Player player = (Player) offlinePlayer;
		if (player.getInventory().firstEmpty() == -1) {
			// TODO Inventory full, do something for this!
			player.sendMessage(ChatColor.RED + "Inventory full, I'll do something for this soon...");
		} else {
			player.getInventory().addItem(getCrateItemStack(amount));
		}
	}

	public void handleWin(Player player) {
		// TODO Error maybe? as we require the location!
	}

	public void handleWin(Player player, Block blockPlaced) {
		if (blockPlaced == null || blockPlaced.getType().equals(Material.AIR))
			return;
		ArrayList<ItemStack> itemStacks = new ArrayList<>();

		for (Winning winning : getWinnings()) {
			if (winning.isAlways()) {
				ItemStack itemStack = winning.runWin(player);
				if (itemStack != null) {
					itemStacks.add(itemStack);
				}
			}
		}

		// By default run win on the last win and give item
		ItemStack itemStack = getRandomWinning(this).runWin(player);
		if (itemStack != null) {
			itemStacks.add(itemStack);
		}

		if (blockPlaced.getType().equals(Material.CHEST)) {
			Chest chest = (Chest) blockPlaced.getState();
			for (ItemStack itemStack1 : itemStacks) {
				chest.getInventory().addItem(itemStack1);
			}
		} else {
			for (ItemStack itemStack1 : itemStacks) {
				HashMap<Integer, ItemStack> left = player.getInventory().addItem(itemStack1);
				for (Map.Entry<Integer, ItemStack> item : left.entrySet()) {
					player.getLocation().getWorld().dropItemNaturally(player.getLocation(), item.getValue());
				}
			}
		}
	}

	private ItemStack getCrateItemStack(Integer amount) {
		// TODO add data ID support
		ItemStack itemStack = new ItemStack(getBlock(), amount);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(getName(true) + " Crate");
		itemMeta.setLore(getLore());
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	private List<String> getLore() {
		if (this.lore == null || this.lore.size() == 0) {
			this.lore = new ArrayList<>();
			this.lore.add(ChatColor.GRAY + "Place this crate to open!");
			this.lore.add("");
		}
		return this.lore;
	}

}
