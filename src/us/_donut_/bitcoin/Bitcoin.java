package us._donut_.bitcoin;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Bitcoin extends JavaPlugin {

    private Util util;
    private BitcoinManager bitcoinManager;
    private Mining mining;
    private BitcoinMenu bitcoinMenu;
    private BlackMarket blackMarket;
    private File configFile;
    private YamlConfiguration bitcoinConfig;
    private File blackMarketFile;
    private YamlConfiguration blackMarketConfig;
    private ServerEconomy economy;
    private Sounds sounds;
    private static BitcoinAPI api;
    static Bitcoin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        util = new Util();

        configFile = new File(getDataFolder(), "config.yml");
        bitcoinConfig = YamlConfiguration.loadConfiguration(configFile);
        if (!configFile.exists()) { getLogger().info("Generated config.yml!"); }
        blackMarketFile = new File(getDataFolder(), "black_market.yml");
        blackMarketConfig = YamlConfiguration.loadConfiguration(blackMarketFile);
        if (!blackMarketFile.exists()) { util.saveYml(blackMarketFile, blackMarketConfig); getLogger().info("Generated black_market.yml!"); }
        if (new File(getDataFolder(), "Player Data").mkdirs()) { getLogger().info("Generated player data folder!"); }
        loadConfigDefaults();

        economy = new ServerEconomy();
        sounds = new Sounds();
        Message.reload();

        getServer().getPluginManager().registerEvents(bitcoinManager = new BitcoinManager(), this);
        getServer().getPluginManager().registerEvents(mining = new Mining(), this);
        getServer().getPluginManager().registerEvents(blackMarket = new BlackMarket(), this);
        getServer().getPluginManager().registerEvents(bitcoinMenu = new BitcoinMenu(), this);

        BitcoinCommand bitcoinCommand;
        getServer().getPluginManager().registerEvents(bitcoinCommand = new BitcoinCommand(), this);
        getCommand("bitcoin").setExecutor(bitcoinCommand);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) { new RegisterPlaceholderAPI().hook(); }
        if (getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) { new RegisterMVdWPlaceholderAPI(); }

        api = new BitcoinAPI(this);
        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }

    private void loadConfigDefaults() {
        YamlConfiguration bitcoinConfig = getBitcoinConfig();
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
        util.saveYml(getConfigFile(), bitcoinConfig);
    }

    void reload() {
        Message.reload();
        configFile = new File(getDataFolder(), "config.yml");
        bitcoinConfig = YamlConfiguration.loadConfiguration(configFile);
        sounds.reload();
        economy.reload();
        bitcoinMenu.reload();
        bitcoinManager.reload();
        mining.reload();
        blackMarket.reload();
    }

    Util getUtil() { return util; }
    BitcoinManager getBitcoinManager() { return bitcoinManager; }
    Mining getMining() { return mining; }
    BlackMarket getBlackMarket() { return blackMarket; }
    BitcoinMenu getBitcoinMenu() { return bitcoinMenu; }
    ServerEconomy getEconomy() { return economy; }
    File getConfigFile() { return configFile; }
    YamlConfiguration getBitcoinConfig() { return bitcoinConfig; }
    File getBlackMarketFile() { return blackMarketFile; }
    YamlConfiguration getBlackMarketConfig() { return blackMarketConfig; }
    Sounds getSounds() { return sounds; }

    public static BitcoinAPI getAPI() { return api; }
}