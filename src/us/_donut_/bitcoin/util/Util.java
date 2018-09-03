package us._donut_.bitcoin.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import us._donut_.bitcoin.Bitcoin;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

public class Util {

    private static Bitcoin plugin = Bitcoin.plugin;
    private static NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
    private static Map<UUID, String> skullTextures = new HashMap<>();
    private static Map<UUID, OfflinePlayer> uuidOfflinePlayerCache = new HashMap<>();
    private static Map<UUID, Long> lastPlayedCache = new HashMap<>();

    public static synchronized void saveYml(File file, YamlConfiguration config) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ItemStack createItemStack(Material item, Short dataValue, String name, @Nullable String lore) {
        ItemStack itemStack = new ItemStack(item, 1, dataValue);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        if (lore != null) { itemMeta.setLore(Arrays.asList(lore.split("\n"))); }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createItemStackWithAmount(Material item, Integer amount, Short dataValue, String name, @Nullable String lore) {
        ItemStack itemStack = createItemStack(item, dataValue, name, lore);
        itemStack.setAmount(amount);
        return itemStack;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getSkull(UUID playerUUID, String playerName, String displayName, String lore) {
        ItemStack skull;
        if (!Bukkit.getVersion().contains("1.13")) {
            skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        } else {
            skull = new ItemStack(Material.valueOf("PLAYER_HEAD"), 1);
        }
        if (plugin.getServer().getOnlineMode()) {
            if (!skullTextures.containsKey(playerUUID)) {
                try {
                    URL address = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + playerUUID.toString().replace("-", ""));
                    InputStreamReader pageInput = new InputStreamReader(address.openStream());
                    BufferedReader source = new BufferedReader(pageInput);
                    String sourceLine = source.readLine();
                    skullTextures.put(playerUUID, sourceLine.split("\"")[17]);
                    UUID hashAsId = new UUID(skullTextures.get(playerUUID).hashCode(), skullTextures.get(playerUUID).hashCode());
                    skull = Bukkit.getUnsafe().modifyItemStack(skull, "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + skullTextures.get(playerUUID) + "\"}]}}}");
                    source.close();
                } catch (IOException e) {
                    SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                    skullMeta.setOwner(playerName);
                }
            } else {
                UUID hashAsId = new UUID(skullTextures.get(playerUUID).hashCode(), skullTextures.get(playerUUID).hashCode());
                skull = Bukkit.getUnsafe().modifyItemStack(skull, "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + skullTextures.get(playerUUID) + "\"}]}}}");
            }
        } else {
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.setOwner(playerName);
        }
        ItemMeta skullMeta = skull.getItemMeta();
        skullMeta.setDisplayName(displayName);
        String[] multiLineLore = lore.split("\n");
        skullMeta.setLore(Arrays.asList(multiLineLore));
        skull.setItemMeta(skullMeta);
        return skull;
    }

    public static Material getGlass(int dataValue) {
        switch (dataValue) {
            case 0:
                return Material.valueOf("WHITE_STAINED_GLASS_PANE");
            case 1:
                return Material.valueOf("ORANGE_STAINED_GLASS_PANE");
            case 2:
                return Material.valueOf("MAGENTA_STAINED_GLASS_PANE");
            case 3:
                return Material.valueOf("LIGHT_BLUE_STAINED_GLASS_PANE");
            case 4:
                return Material.valueOf("YELLOW_STAINED_GLASS_PANE");
            case 5:
                return Material.valueOf("LIME_STAINED_GLASS_PANE");
            case 6:
                return Material.valueOf("PINK_STAINED_GLASS_PANE");
            case 7:
                return Material.valueOf("GRAY_STAINED_GLASS_PANE");
            case 8:
                return Material.valueOf("LIGHT_GRAY_STAINED_GLASS_PANE");
            case 9:
                return Material.valueOf("CYAN_STAINED_GLASS_PANE");
            case 10:
                return Material.valueOf("PURPLE_STAINED_GLASS_PANE");
            case 11:
                return Material.valueOf("BLUE_STAINED_GLASS_PANE");
            case 12:
                return Material.valueOf("BROWN_STAINED_GLASS_PANE");
            case 13:
                return Material.valueOf("GREEN_STAINED_GLASS_PANE");
            case 14:
                return Material.valueOf("RED_STAINED_GLASS_PANE");
            case 15:
                return Material.valueOf("BLACK_STAINED_GLASS_PANE");
            default:
                return null;
        }
    }

    public static String colorMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static double round(int places, double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static String formatNumber(Number number) {
        if (String.valueOf(number).contains(".")) {
            String[] num = String.valueOf(number).split("\\.");
            return numberFormat.format(Integer.parseInt(num[0])) + "." + num[1];
        } else {
            return numberFormat.format(number);
        }
    }

    public static String formatRoundNumber(Number number) {
        return formatNumber(round(plugin.getBitcoinManager().getDisplayRoundAmount(), number.doubleValue()));
    }

    public static String formatRound2Number(Number number) {
        return formatNumber(round(2, number.doubleValue()));
    }

    public static Long getTicksFromTime(String time) {
        int hours;
        int minutes;
        try {
            hours = Integer.valueOf(time.split(":")[0]);
            minutes = Integer.valueOf(time.split(":")[1]);
        } catch (NumberFormatException e) {
            return null;
        }
        if (hours > 24 || hours < 0 || minutes > 59 || minutes < 0) { return null; }
        return (long) (((18001 + (hours * 1000) + ((minutes / 60.0) * 1000))) % 24000);
    }

    public static Map<UUID, OfflinePlayer> getUUIDOfflinePlayerCache() {
        return uuidOfflinePlayerCache;
    }

    public static Map<UUID, Long> getLastPlayedCache() {
        return lastPlayedCache;
    }
}