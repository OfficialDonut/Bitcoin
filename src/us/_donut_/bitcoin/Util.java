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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        if (!bitcoinConfig.contains("exchange_currency_symbol")) { bitcoinConfig.set("exchange_currency_symbol", "$"); }
        if (!bitcoinConfig.contains("min_bitcoin_value_fluctuation")) { bitcoinConfig.set("min_bitcoin_value_fluctuation", 0); }
        if (!bitcoinConfig.contains("max_bitcoin_value_fluctuation")) { bitcoinConfig.set("max_bitcoin_value_fluctuation", 100); }
        if (!bitcoinConfig.contains("min_mining_reward")) { bitcoinConfig.set("min_mining_reward", 10); }
        if (!bitcoinConfig.contains("max_mining_reward")) { bitcoinConfig.set("max_mining_reward", 50); }
        if (!bitcoinConfig.contains("main_world_name")) { bitcoinConfig.set("world", "world"); }
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            UUID hashAsId = new UUID(skullTextures.get(playerUUID).hashCode(), skullTextures.get(playerUUID).hashCode());
            skull = Bukkit.getUnsafe().modifyItemStack(skull, "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + skullTextures.get(playerUUID) + "\"}]}}}");
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

    double round(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}