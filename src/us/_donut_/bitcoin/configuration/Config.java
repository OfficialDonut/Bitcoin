package us._donut_.bitcoin.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import us._donut_.bitcoin.Bitcoin;

import java.io.File;

import static us._donut_.bitcoin.util.Util.saveYml;

public class Config {

    private Bitcoin plugin = Bitcoin.plugin;
    private File configFile;
    private YamlConfiguration bitcoinConfig;

    public Config() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        bitcoinConfig = YamlConfiguration.loadConfiguration(configFile);
        if (!configFile.exists()) { plugin.getLogger().info("Generated config.yml!"); }
        loadConfigDefaults();
    }

    private void loadConfigDefaults() {
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
        saveYml(configFile, bitcoinConfig);
    }

    public void reload() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        bitcoinConfig = YamlConfiguration.loadConfiguration(configFile);
    }

    public File getConfigFile() {
        return configFile;
    }

    public YamlConfiguration getBitcoinConfig() {
        return bitcoinConfig;
    }
}
