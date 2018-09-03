package us._donut_.bitcoin.placeholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import us._donut_.bitcoin.Bitcoin;
import us._donut_.bitcoin.BitcoinManager;

import static us._donut_.bitcoin.util.Util.*;

public class RegisterMVdWPlaceholderAPI {

    public RegisterMVdWPlaceholderAPI() {
        Bitcoin plugin = Bitcoin.plugin;
        BitcoinManager bitcoinManager = plugin.getBitcoinManager();
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_value", placeholderReplaceEvent -> bitcoinManager.getExchangeCurrencySymbol() + formatRound2Number(bitcoinManager.getBitcoinValue()));
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_bank", placeholderReplaceEvent -> formatRoundNumber(bitcoinManager.getAmountInBank()));
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_tax", placeholderReplaceEvent -> bitcoinManager.getPurchaseTaxPercentage() + "%");
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_circulation", placeholderReplaceEvent -> formatRoundNumber(bitcoinManager.getBitcoinsInCirculation()));
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_circulation_limit", placeholderReplaceEvent -> formatRoundNumber(bitcoinManager.getCirculationLimit()));
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_balance", placeholderReplaceEvent -> placeholderReplaceEvent.getOfflinePlayer() != null ? formatRoundNumber(bitcoinManager.getBalance(placeholderReplaceEvent.getOfflinePlayer().getUniqueId())) : "");
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_amount_mined", placeholderReplaceEvent -> placeholderReplaceEvent.getOfflinePlayer() != null ? formatRoundNumber(bitcoinManager.getBitcoinsMined(placeholderReplaceEvent.getOfflinePlayer().getUniqueId())) : "");
        PlaceholderAPI.registerPlaceholder(plugin, "bitcoin_puzzles_solved", placeholderReplaceEvent -> placeholderReplaceEvent.getOfflinePlayer() != null ? formatNumber(bitcoinManager.getPuzzlesSolved(placeholderReplaceEvent.getOfflinePlayer().getUniqueId())) : "");
    }
}