package us._donut_.bitcoin;

import me.BukkitPVP.PointsAPI.PointsAPI;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

class ServerEconomy {

    private Bitcoin plugin;
    private Economy economy;
    private Boolean usePlayerPoints;
    private Boolean usePointsAPI;
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
        usePointsAPI = plugin.getBitcoinConfig().getBoolean("use_pointsapi");
        usePlayerPoints = plugin.getBitcoinConfig().getBoolean("use_playerpoints");
        if (usePlayerPoints) {
            playerPointsAPI = PlayerPoints.class.cast(plugin.getServer().getPluginManager().getPlugin("PlayerPoints")).getAPI();
        }
    }

    void depositPlayer(OfflinePlayer player, String worldName, double amount) {
        if (usePointsAPI) {
            PointsAPI.addPoints(player, (int) amount);
        } else if (usePlayerPoints) {
            playerPointsAPI.give(player.getUniqueId(), (int) amount);
        } else {
            economy.depositPlayer(player, worldName, amount);
        }
    }

    void withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        if (usePointsAPI) {
            PointsAPI.removePoints(player, (int) amount);
        } else if (usePlayerPoints) {
            playerPointsAPI.take(player.getUniqueId(), (int) amount);
        } else {
            economy.withdrawPlayer(player, worldName, amount);
        }
    }

    double getBalance(OfflinePlayer player) {
        if (usePointsAPI) {
            return PointsAPI.getPoints(player);
        } else if (usePlayerPoints) {
            return playerPointsAPI.look(player.getUniqueId());
        } else {
            return economy.getBalance(player);
        }
    }
}