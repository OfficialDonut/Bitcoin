package us._donut_.bitcoin;

import be.maximvdw.placeholderapi.PlaceholderAPI;

public class RegisterMVdWPlaceholderAPI {

    RegisterMVdWPlaceholderAPI() {
        Bitcoin plugin = Bitcoin.plugin;
        BitcoinManager bitcoinManager = plugin.getBitcoinManager();
        Util util = plugin.getUtil();
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_value", placeholderReplaceEvent -> bitcoinManager.getExchangeCurrencySymbol() + util.formatRound2Number(bitcoinManager.getBitcoinValue()));
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_bank", placeholderReplaceEvent -> util.formatRoundNumber(bitcoinManager.getAmountInBank()));
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_tax", placeholderReplaceEvent -> bitcoinManager.getPurchaseTaxPercentage() + "%");
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_circulation", placeholderReplaceEvent -> util.formatRoundNumber(bitcoinManager.getBitcoinsInCirculation()));
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_circulation_limit", placeholderReplaceEvent -> util.formatRoundNumber(bitcoinManager.getCirculationLimit()));
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_balance", placeholderReplaceEvent -> placeholderReplaceEvent.getOfflinePlayer() != null ? util.formatRoundNumber(bitcoinManager.getBalance(placeholderReplaceEvent.getOfflinePlayer().getUniqueId())) : "");
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_amount_mined", placeholderReplaceEvent -> placeholderReplaceEvent.getOfflinePlayer() != null ? util.formatRoundNumber(bitcoinManager.getBitcoinsMined(placeholderReplaceEvent.getOfflinePlayer().getUniqueId())) : "");
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_puzzles_solved", placeholderReplaceEvent -> placeholderReplaceEvent.getOfflinePlayer() != null ? util.formatNumber(bitcoinManager.getPuzzlesSolved(placeholderReplaceEvent.getOfflinePlayer().getUniqueId())) : "");
    }
}