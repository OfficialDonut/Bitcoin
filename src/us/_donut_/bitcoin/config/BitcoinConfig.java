package us._donut_.bitcoin.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import us._donut_.bitcoin.Bitcoin;
import us._donut_.bitcoin.Util;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class BitcoinConfig {

    private static Bitcoin plugin;
    private static File configFile;
    private static YamlConfiguration yamlConfig;

    public static void reload() {
        plugin = Bitcoin.getInstance();
        configFile = new File(plugin.getDataFolder(), "config.yml");
        yamlConfig = YamlConfiguration.loadConfiguration(configFile);
        if (!configFile.exists()) {
            plugin.getLogger().info("Generated config.yml!");
        }
        loadDefaults();
    }

    private static void loadDefaults() {
        yamlConfig.addDefault("amount_in_bank", 0);
        yamlConfig.addDefault("bitcoin_display_rounding", 5);
        yamlConfig.addDefault("bitcoin_max_value", -1);
        yamlConfig.addDefault("bitcoin_min_value", 0);
        yamlConfig.addDefault("bitcoin_value", 1000);
        yamlConfig.addDefault("broadcast_balance_reset_message", true);
        yamlConfig.addDefault("broadcast_real_value", true);
        yamlConfig.addDefault("circulation_limit", -1);
        yamlConfig.addDefault("computers", false);
        yamlConfig.addDefault("computer_recipe", Arrays.asList("IRON_INGOT, REDSTONE, IRON_INGOT", "REDSTONE, DIAMOND, REDSTONE", "IRON_INGOT, REDSTONE, IRON_INGOT"));
        yamlConfig.addDefault("computer_uses_before_break", 3);
        yamlConfig.addDefault("days_of_inactivity_until_balance_reset", 30);
        yamlConfig.addDefault("fluctuation_frequency", "6:00");
        yamlConfig.addDefault("max_bitcoin_value_fluctuation", 100);
        yamlConfig.addDefault("max_mining_reward", 50);
        yamlConfig.addDefault("min_bitcoin_value_fluctuation", 0);
        yamlConfig.addDefault("min_mining_reward", 10);
        yamlConfig.addDefault("new_mining_puzzle_delay", 0);
        yamlConfig.addDefault("purchase_tax_percentage", 15);
        yamlConfig.addDefault("puzzle_difficulty", "easy");
        yamlConfig.addDefault("use_playerpoints", false);
        yamlConfig.addDefault("use_pointsapi", false);
        yamlConfig.addDefault("use_real_value", false);
        yamlConfig.addDefault("world", "world");
        yamlConfig.options().copyDefaults(true);
        save();
    }

    private static void save() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Util.saveYml(configFile, yamlConfig));
    }

    public static void set(String path, Object value) {
        yamlConfig.set(path, value);
        save();
    }

    public static String getString(String path) {
        return yamlConfig.getString(path);
    }

    public static List<String> getStringList(String path) {
        return yamlConfig.getStringList(path);
    }

    public static int getInt(String path) {
        return yamlConfig.getInt(path);
    }

    public static long getLong(String path) {
        return yamlConfig.getLong(path);
    }

    public static double getDouble(String path) {
        return yamlConfig.getDouble(path);
    }

    public static boolean getBoolean(String path) {
        return yamlConfig.getBoolean(path);
    }
}
