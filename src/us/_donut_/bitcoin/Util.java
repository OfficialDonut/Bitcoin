package us._donut_.bitcoin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import java.util.*;

class Util {

    private Bitcoin plugin;
    private Map<UUID, String> skullTextures = new HashMap<>();

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
        if (!bitcoinConfig.contains("bitcoin_value")) { bitcoinConfig.set("bitcoin_value", 1000); }
        if (!bitcoinConfig.contains("amount_in_bank")) { bitcoinConfig.set("amount_in_bank", 0); }
        if (!bitcoinConfig.contains("bitcoin_display_rounding")) { bitcoinConfig.set("bitcoin_display_rounding", 5); }
        if (!bitcoinConfig.contains("purchase_tax_percentage")) { bitcoinConfig.set("purchase_tax_percentage", 15); }
        if (!bitcoinConfig.contains("exchange_currency_symbol")) { bitcoinConfig.set("exchange_currency_symbol", "$"); }
        if (!bitcoinConfig.contains("min_bitcoin_value_fluctuation")) { bitcoinConfig.set("min_bitcoin_value_fluctuation", 0); }
        if (!bitcoinConfig.contains("max_bitcoin_value_fluctuation")) { bitcoinConfig.set("max_bitcoin_value_fluctuation", 100); }
        if (!bitcoinConfig.contains("fluctuation_frequency")) { bitcoinConfig.set("fluctuation_frequency", "6:00"); }
        if (!bitcoinConfig.contains("min_mining_reward")) { bitcoinConfig.set("min_mining_reward", 10); }
        if (!bitcoinConfig.contains("max_mining_reward")) { bitcoinConfig.set("max_mining_reward", 50); }
        if (!bitcoinConfig.contains("circulation_limit")) { bitcoinConfig.set("circulation_limit", -1); }
        if (!bitcoinConfig.contains("world")) { bitcoinConfig.set("world", "world"); }
        if (!bitcoinConfig.contains("new_mining_puzzle_delay")) { bitcoinConfig.set("new_mining_puzzle_delay", 0); }
        if (!bitcoinConfig.contains("use_playerpoints")) { bitcoinConfig.set("use_playerpoints", false); }
        if (!bitcoinConfig.contains("use_pointsapi")) { bitcoinConfig.set("use_pointsapi", false); }
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
}