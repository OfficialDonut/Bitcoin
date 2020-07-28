package us.donut.bitcoin;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import us.donut.bitcoin.config.BitcoinConfig;
import us.donut.bitcoin.config.Messages;
import us.donut.bitcoin.config.Sounds;
import us.donut.bitcoin.hooks.MVdWHook;
import us.donut.bitcoin.hooks.PapiHook;
import us.donut.bitcoin.hooks.ServerEconomy;
import us.donut.bitcoin.mining.ComputerManager;
import us.donut.bitcoin.mining.MiningManager;

public class Bitcoin extends JavaPlugin {

    private static Bitcoin plugin;
    private BitcoinManager bitcoinManager;
    private PlayerDataManager playerDataManager;
    private MiningManager miningManager;
    private ComputerManager computerManager;
    private BlackMarket blackMarket;
    private BitcoinMenu bitcoinMenu;

    public static Bitcoin getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        bitcoinManager = BitcoinManager.getInstance();
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(playerDataManager = PlayerDataManager.getInstance(), this);
        pluginManager.registerEvents(miningManager = MiningManager.getInstance(), this);
        pluginManager.registerEvents(computerManager = ComputerManager.getInstance(), this);
        pluginManager.registerEvents(blackMarket = BlackMarket.getInstance(), this);
        pluginManager.registerEvents(bitcoinMenu = BitcoinMenu.getInstance(), this);
        pluginManager.registerEvents(BitcoinCommand.getInstance(), this);
        getCommand("bitcoin").setExecutor(BitcoinCommand.getInstance());
        reload();
        BitcoinAPI.init();
        ServerEconomy.hook();
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) { new PapiHook().register(); }
        if (getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) { MVdWHook.hook(); }
        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }

    public void reload() {
        Messages.reload();
        Sounds.reload();
        BitcoinConfig.reload();
        ServerEconomy.reload();
        bitcoinManager.reload();
        playerDataManager.reload();
        miningManager.reload();
        computerManager.reload();
        blackMarket.reload();
        bitcoinMenu.reload();
    }
}
