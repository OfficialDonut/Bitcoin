package us._donut_.bitcoin;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

class ServerEconomy {

    private Bitcoin plugin;
    private Economy economy;
    private Boolean usePlayerPoints;
    private PlayerPointsAPI playerPointsAPI;

    ServerEconomy(Bitcoin pluginInstance) {
        plugin = pluginInstance;
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) { economy = economyProvider.getProvider(); }
        reload();
    }

    Boolean hasEconomy() {
        if (economy != null) {
            return true;
        } else {
            return usePlayerPoints;
        }
    }

    void reload() {
        usePlayerPoints = plugin.getBitcoinConfig().getBoolean("use_playerpoints");
        if (usePlayerPoints) {
            playerPointsAPI = PlayerPoints.class.cast(plugin.getServer().getPluginManager().getPlugin("PlayerPoints")).getAPI();
        }
    }

    void depositPlayer(OfflinePlayer player, String worldName, double amount) {
        if (!usePlayerPoints) {
            economy.depositPlayer(player, worldName, amount);
        } else {
            playerPointsAPI.give(player.getUniqueId(), (int) amount);
        }
    }

    void withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        if (!usePlayerPoints) {
            economy.withdrawPlayer(player, worldName, amount);
        } else {
            playerPointsAPI.take(player.getUniqueId(), (int) amount);
        }
    }

    double getBalance(OfflinePlayer player) {
        if (!usePlayerPoints) {
            return economy.getBalance(player);
        } else {
            return playerPointsAPI.look(player.getUniqueId());
        }
    }
}