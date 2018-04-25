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
    private Messages messages;
    private Sounds sounds;
    private static BitcoinAPI api;

    @Override
    public void onEnable() {
        util = new Util(this);

        configFile = new File(getDataFolder(), "config.yml");
        bitcoinConfig = YamlConfiguration.loadConfiguration(configFile);
        if (!configFile.exists()) { getLogger().info("Generated config.yml!"); }
        blackMarketFile = new File(getDataFolder(), "black_market.yml");
        blackMarketConfig = YamlConfiguration.loadConfiguration(blackMarketFile);
        if (!blackMarketFile.exists()) { util.saveYml(blackMarketFile, blackMarketConfig); getLogger().info("Generated black_market.yml!"); }
        if (new File(getDataFolder(), "Player Data").mkdirs()) { getLogger().info("Generated player data folder!"); }
        util.loadConfigDefaults();
        economy = new ServerEconomy(this);
        messages = new Messages(this);
        sounds = new Sounds(this);
        getServer().getPluginManager().registerEvents(bitcoinManager = new BitcoinManager(this), this);
        getServer().getPluginManager().registerEvents(mining = new Mining(this), this);
        getServer().getPluginManager().registerEvents(blackMarket = new BlackMarket(this), this);
        getServer().getPluginManager().registerEvents(bitcoinMenu = new BitcoinMenu(this), this);
        BitcoinCommand bitcoinCommand;
        getServer().getPluginManager().registerEvents(bitcoinCommand = new BitcoinCommand(this), this);
        getCommand("bitcoin").setExecutor(bitcoinCommand);
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) { new RegisterPlaceholderAPI(this).hook(); }
        if (getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) { new RegisterMVdWPlaceholderAPI(this); }
        api = new BitcoinAPI(this);
        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }

    void reload() {
        configFile = new File(getDataFolder(), "config.yml");
        bitcoinConfig = YamlConfiguration.loadConfiguration(configFile);
        messages.reload();
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
    Messages getMessages() { return messages; }
    Sounds getSounds() { return sounds; }

    public static BitcoinAPI getAPI() { return api; }
}