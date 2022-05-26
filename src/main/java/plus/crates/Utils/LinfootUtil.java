package plus.crates.Utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class LinfootUtil {

    public static Enchantment getEnchantmentFromNiceName(String name) {
        Enchantment enchantment = null;
        try {
            enchantment = Enchantment.getByName(name);
        } catch (Exception ignored) {
        }

        if (enchantment != null)
            return enchantment;

        switch (name.toLowerCase()) {
            case "sharpness":
                enchantment = Enchantment.DAMAGE_ALL;
                break;
            case "unbreaking":
                enchantment = Enchantment.DURABILITY;
                break;
            case "efficiency":
                enchantment = Enchantment.DIG_SPEED;
                break;
            case "protection":
                enchantment = Enchantment.PROTECTION_ENVIRONMENTAL;
                break;
            case "power":
                enchantment = Enchantment.ARROW_DAMAGE;
                break;
            case "punch":
                enchantment = Enchantment.ARROW_KNOCKBACK;
                break;
            case "infinite":
                enchantment = Enchantment.ARROW_INFINITE;
                break;
        }

        return enchantment;
    }

    public static ItemStack buildItemStack(ItemStack itemStack, String name, List<String> lore) {
        if (name == null && lore == null) {
            return itemStack;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (name != null) {
            itemMeta.setDisplayName(ChatColor.RESET + name);
        }
        if (lore != null) {
            itemMeta.setLore(lore);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static void copyConfigSection(FileConfiguration config, String fromPath, String toPath) {
        Map<String, Object> vals = config.getConfigurationSection(fromPath).getValues(true);
        String toDot = toPath.equals("") ? "" : ".";
        for (String s : vals.keySet()) {
            Object val = vals.get(s);
            if (val instanceof List)
                val = new ArrayList((List) val);
            config.set(toPath + toDot + s, val);
        }
    }

    // System.out.println(versionCompare("1.6", "1.8")); // -1 as 1.8 is newer
    // System.out.println(versionCompare("1.7", "1.8")); // -1 as 1.8 is newer
    // System.out.println(versionCompare("1.8", "1.8")); // 0 as same
    // System.out.println(versionCompare("1.9", "1.8")); // 1 as 1.9 is newer
    public static int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        return Integer.signum(vals1.length - vals2.length);
    }
    public static <T> T getRandomEntry(Set<T> set) {
        if (set == null) return null;

        int random = ThreadLocalRandom.current().nextInt(set.size());

        int i = 0;
        for (T elem : set) {
            if (i == random) return elem;
            i++;
        }

        return null;
    }

    public static Color getColor(int i) {
        switch (i) {
            default:
            case 1:
                return Color.AQUA;
            case 2:
                return Color.BLACK;
            case 3:
                return Color.BLUE;
            case 4:
                return Color.FUCHSIA;
            case 5:
                return Color.GRAY;
            case 6:
                return Color.GREEN;
            case 7:
                return Color.LIME;
            case 8:
                return Color.MAROON;
            case 9:
                return Color.NAVY;
            case 10:
                return Color.OLIVE;
            case 11:
                return Color.ORANGE;
            case 12:
                return Color.PURPLE;
            case 13:
                return Color.RED;
            case 14:
                return Color.SILVER;
            case 15:
                return Color.TEAL;
            case 16:
                return Color.WHITE;
            case 17:
                return Color.YELLOW;
        }
    }
    public static String formatLocation(Location location) {
        return location.getWorld().getName() + "|" + location.getBlockX() + "|" + location.getBlockY() + "|" + location.getBlockZ();
    }
}
