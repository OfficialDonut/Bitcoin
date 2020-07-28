package us.donut.bitcoin.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import us.donut.bitcoin.Bitcoin;
import us.donut.bitcoin.BitcoinManager;
import us.donut.bitcoin.PlayerDataManager;
import us.donut.bitcoin.Util;

public class MVdWHook {

    public static void hook() {
        Bitcoin plugin = Bitcoin.getInstance();
        BitcoinManager bitcoinManager = BitcoinManager.getInstance();
        PlayerDataManager playerDataManager = PlayerDataManager.getInstance();
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_value", e -> bitcoinManager.getFormattedValue());
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_bank", e -> bitcoinManager.format(bitcoinManager.getAmountInBank()));
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_tax", e -> bitcoinManager.getPurchaseTaxPercentage() + "%");
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_circulation", e -> bitcoinManager.format(bitcoinManager.getBitcoinsInCirculation()));
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_circulation_limit", e -> bitcoinManager.format(bitcoinManager.getCirculationLimit()));
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_balance", e -> e.getOfflinePlayer() != null ? bitcoinManager.format(playerDataManager.getBalance(e.getOfflinePlayer().getUniqueId())) : "");
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_amount_mined", e -> e.getOfflinePlayer() != null ? bitcoinManager.format(playerDataManager.getBitcoinsMined(e.getOfflinePlayer().getUniqueId())) : "");
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_puzzles_solved", e -> e.getOfflinePlayer() != null ? Util.formatNumber(playerDataManager.getPuzzlesSolved(e.getOfflinePlayer().getUniqueId())) : "");
    }
}
