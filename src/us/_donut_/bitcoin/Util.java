package us._donut_.bitcoin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

class Util {

    private Bitcoin plugin;
    private Map<UUID, String> skullTextures = new HashMap<>();
    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
    private Map<UUID, OfflinePlayer> uuidOfflinePlayerMap = new HashMap<>();

    Util(Bitcoin pluginInstance) {
        plugin = pluginInstance;
    }

    void saveYml(File file, YamlConfiguration config) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void loadConfigDefaults() {
        YamlConfiguration bitcoinConfig = plugin.getBitcoinConfig();
        bitcoinConfig.addDefault("amount_in_bank", 0);
        bitcoinConfig.addDefault("bitcoin_display_rounding", 5);
        bitcoinConfig.addDefault("bitcoin_max_value", -1);
        bitcoinConfig.addDefault("bitcoin_min_value", 0);
        bitcoinConfig.addDefault("bitcoin_value", 1000);
        bitcoinConfig.addDefault("broadcast_balance_reset_message", true);
        bitcoinConfig.addDefault("broadcast_real_value", true);
        bitcoinConfig.addDefault("circulation_limit", -1);
        bitcoinConfig.addDefault("days_of_inactivity_until_balance_reset", 30);
        bitcoinConfig.addDefault("exchange_currency_symbol", "$");
        bitcoinConfig.addDefault("fluctuation_frequency", "6:00");
        bitcoinConfig.addDefault("max_bitcoin_value_fluctuation", 100);
        bitcoinConfig.addDefault("max_mining_reward", 50);
        bitcoinConfig.addDefault("min_bitcoin_value_fluctuation", 0);
        bitcoinConfig.addDefault("min_mining_reward", 10);
        bitcoinConfig.addDefault("new_mining_puzzle_delay", 0);
        bitcoinConfig.addDefault("purchase_tax_percentage", 15);
        bitcoinConfig.addDefault("puzzle_difficulty", "easy");
        bitcoinConfig.addDefault("use_playerpoints", false);
        bitcoinConfig.addDefault("use_pointsapi", false);
        bitcoinConfig.addDefault("use_real_value", false);
        bitcoinConfig.addDefault("world", "world");

        bitcoinConfig.options().copyDefaults(true);
        saveYml(plugin.getConfigFile(), bitcoinConfig);
    }

    ItemStack createItemStack(Material item, Short dataValue, String name, String lore) {
        ItemStack itemStack = new ItemStack(item, 1, dataValue);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        if (lore != null) { itemMeta.setLore(Arrays.asList(lore.split("\n"))); }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    ItemStack createItemStackWithAmount(Material item, Integer amount, Short dataValue, String name, String lore) {
        ItemStack itemStack = new ItemStack(item, amount, dataValue);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        if (lore != null) { itemMeta.setLore(Arrays.asList(lore.split("\n"))); }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @SuppressWarnings("deprecation")
    ItemStack getSkull(UUID playerUUID, String playerName, String displayName, String lore) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
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

    String colorMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    double round(int places, double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    String formatNumber(Number number) {
        if (String.valueOf(number).contains(".")) {
            String[] num = String.valueOf(number).split("\\.");
            return numberFormat.format(Integer.parseInt(num[0])) + "." + num[1];
        } else {
            return numberFormat.format(number);
        }
    }

    Long getTicksFromTime(String time) {
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

    Map<UUID, OfflinePlayer> getUUIDOfflinePlayerMap() {
        return uuidOfflinePlayerMap;
    }
}