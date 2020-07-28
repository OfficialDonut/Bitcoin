package us.donut.bitcoin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;

public class Util {

    private static NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);

    public static synchronized void saveYml(File file, YamlConfiguration config) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static long getTicksFromTime(String time) {
        String[] splitTime = time.split(":");
        int hours = Integer.parseInt(splitTime[0]);
        int minutes = Integer.parseInt(splitTime[1]);
        if (hours > 24 || hours < 0 || minutes > 59 || minutes < 0) { return 1; }
        return (long) (((18001 + (hours * 1000) + ((minutes / 60.0) * 1000))) % 24000);
    }

    public static double round(int places, double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static String formatNumber(Number number) {
        return numberFormat.format(number);
    }

    public static String formatRoundNumber(Number number) {
        return formatNumber(round(2, number.doubleValue()));
    }

    public static ItemStack createItemStack(Material material, int amount, String name, String lore) {
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(name);
            if (lore != null) {
                itemMeta.setLore(Arrays.asList(lore.split("\n")));
            }
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static ItemStack createItemStack(Material material, String name, String lore) {
        return createItemStack(material, 1, name, lore);
    }

    public static void closeInventories(Map<?, Inventory> map) {
        for (Inventory inventory : map.values()) {
            List<HumanEntity> viewers = inventory.getViewers();
            for (int i = viewers.size() - 1; i >= 0; i--) {
                viewers.get(i).closeInventory();
            }
        }
    }
}
