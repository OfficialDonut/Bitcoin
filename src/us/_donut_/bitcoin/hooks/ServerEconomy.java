package us._donut_.bitcoin.hooks;

import me.BukkitPVP.PointsAPI.PointsAPI;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import us._donut_.bitcoin.Bitcoin;
import us._donut_.bitcoin.config.BitcoinConfig;

public class ServerEconomy {

    private static Bitcoin plugin;
    private static Economy economy;
    private static PlayerPointsAPI playerPointsAPI;
    private static boolean usePlayerPoints;
    private static boolean usePointsAPI;

    public static void hook() {
        plugin = Bitcoin.getInstance();
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
    }

    public static void reload() {
        usePointsAPI = BitcoinConfig.getBoolean("use_pointsapi");
        usePlayerPoints = BitcoinConfig.getBoolean("use_playerpoints");
        if (usePlayerPoints) {
            Plugin plugin = ServerEconomy.plugin.getServer().getPluginManager().getPlugin("PlayerPoints");
            playerPointsAPI = ((PlayerPoints) plugin).getAPI();
        }
    }

    public static boolean isPresent() {
        return economy != null || usePlayerPoints || usePointsAPI;
    }

    public static void deposit(OfflinePlayer player, String worldName, double amount) {
        if (usePointsAPI) {
            PointsAPI.addPoints(player, (int) amount);
        } else if (usePlayerPoints) {
            playerPointsAPI.give(player.getUniqueId(), (int) amount);
        } else {
            economy.depositPlayer(player, worldName, amount);
        }
    }

    public static void withdraw(OfflinePlayer player, String worldName, double amount) {
        if (usePointsAPI) {
            PointsAPI.removePoints(player, (int) amount);
        } else if (usePlayerPoints) {
            playerPointsAPI.take(player.getUniqueId(), (int) amount);
        } else {
            economy.withdrawPlayer(player, worldName, amount);
        }
    }

    public static double getBalance(OfflinePlayer player) {
        if (usePointsAPI) {
            return PointsAPI.getPoints(player);
        } else if (usePlayerPoints) {
            return playerPointsAPI.look(player.getUniqueId());
        } else {
            return economy.getBalance(player);
        }
    }
}
