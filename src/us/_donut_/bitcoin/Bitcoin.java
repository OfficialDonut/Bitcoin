package us._donut_.bitcoin;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import us._donut_.bitcoin.configuration.Config;
import us._donut_.bitcoin.configuration.Message;
import us._donut_.bitcoin.configuration.Sounds;
import us._donut_.bitcoin.placeholders.RegisterMVdWPlaceholderAPI;
import us._donut_.bitcoin.placeholders.RegisterPlaceholderAPI;

import java.io.File;

import static us._donut_.bitcoin.util.Util.*;

public class Bitcoin extends JavaPlugin {

    private BitcoinManager bitcoinManager;
    private Mining mining;
    private BitcoinMenu bitcoinMenu;
    private BlackMarket blackMarket;
    private File blackMarketFile;
    private YamlConfiguration blackMarketConfig;
    private ServerEconomy economy;
    private Config config;
    private Sounds sounds;
    private static BitcoinAPI api;
    public static Bitcoin plugin;

    @Override
    public void onEnable() {
        plugin = this;

        config = new Config();
        blackMarketFile = new File(getDataFolder(), "black_market.yml");
        blackMarketConfig = YamlConfiguration.loadConfiguration(blackMarketFile);
        if (!blackMarketFile.exists()) { saveYml(blackMarketFile, blackMarketConfig); getLogger().info("Generated black_market.yml!"); }
        if (new File(getDataFolder(), "Player Data").mkdirs()) { getLogger().info("Generated player data folder!"); }
        sounds = new Sounds();
        economy = new ServerEconomy();
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

    void reload() {
        Message.reload();
        config.reload();
        sounds.reload();
        economy.reload();
        bitcoinMenu.reload();
        bitcoinManager.reload();
        mining.reload();
        blackMarket.reload();
    }

    public BitcoinManager getBitcoinManager() { return bitcoinManager; }
    YamlConfiguration getBitcoinConfig() { return config.getBitcoinConfig(); }
    File getConfigFile() { return config.getConfigFile(); }
    Mining getMining() { return mining; }
    BlackMarket getBlackMarket() { return blackMarket; }
    BitcoinMenu getBitcoinMenu() { return bitcoinMenu; }
    ServerEconomy getEconomy() { return economy; }
    File getBlackMarketFile() { return blackMarketFile; }
    YamlConfiguration getBlackMarketConfig() { return blackMarketConfig; }
    Sounds getSounds() { return sounds; }

    public static BitcoinAPI getAPI() { return api; }
}