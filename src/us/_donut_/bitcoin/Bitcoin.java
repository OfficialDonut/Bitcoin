package us._donut_.bitcoin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Bitcoin extends JavaPlugin {

    private Util util;
    private BitcoinManager bitcoinManager;
    private Mining mining;
    private BitcoinMenu bitcoinMenu;
    private File configFile;
    private YamlConfiguration bitcoinConfig;
    private Economy economy;
    private Messages messages;

    @Override
    public void onEnable() {
        util = new Util(this);

        configFile = new File(getDataFolder(), "config.yml");
        bitcoinConfig = YamlConfiguration.loadConfiguration(configFile);
        if (!configFile.exists()) { getLogger().info("Generated config.yml!"); }
        if (new File(getDataFolder(), "Player Data").mkdirs()) { getLogger().info("Generated player data folder!"); }
        util.loadConfigDefaults();

        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) { economy = economyProvider.getProvider(); }

        messages = new Messages(this);
        getServer().getPluginManager().registerEvents(bitcoinManager = new BitcoinManager(this), this);
        getServer().getPluginManager().registerEvents(mining = new Mining(this), this);
        getServer().getPluginManager().registerEvents(bitcoinMenu = new BitcoinMenu(this), this);
        BitcoinCommand bitcoinCommand;
        getServer().getPluginManager().registerEvents(bitcoinCommand = new BitcoinCommand(this), this);
        getCommand("bitcoin").setExecutor(bitcoinCommand);

        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }

    Util getUtil() { return util; }
    BitcoinManager getBitcoinManager() { return bitcoinManager; }
    Mining getMining() { return mining; }
    BitcoinMenu getBitcoinMenu() { return bitcoinMenu; }
    Economy getEconomy() { return economy; }
    File getConfigFile() { return configFile; }
    YamlConfiguration getBitcoinConfig() { return bitcoinConfig; }
    Messages getMessages() { return messages; }
}