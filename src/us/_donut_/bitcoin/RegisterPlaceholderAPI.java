package us._donut_.bitcoin;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;

public class RegisterPlaceholderAPI extends EZPlaceholderHook {

    private BitcoinManager bitcoinManager;
    private Util util;

    public RegisterPlaceholderAPI(Bitcoin pluginInstance) {
        super(pluginInstance, "bitcoin");
        bitcoinManager = pluginInstance.getBitcoinManager();
        util = pluginInstance.getUtil();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equals("value")) {
            return bitcoinManager.getExchangeCurrencySymbol() + util.formatNumber(bitcoinManager.getBitcoinValue());
        }

        if (identifier.equals("bank")) {
            return util.formatNumber(bitcoinManager.getAmountInBank());
        }

        if (identifier.equals("tax")) {
            return bitcoinManager.getPurchaseTaxPercentage() + "%";
        }

        if (identifier.equals("circulation")) {
            return util.formatNumber(bitcoinManager.getBitcoinsInCirculation());
        }

        if (identifier.equals("circulation_limit")) {
            return util.formatNumber(bitcoinManager.getCirculationLimit());
        }

        if (player == null) {
            return "";
        }

        if (identifier.equals("balance")) {
            return util.formatNumber(bitcoinManager.getBalance(player.getUniqueId()));
        }

        if (identifier.equals("amount_mined")) {
            return util.formatNumber(bitcoinManager.getBitcoinsMined(player.getUniqueId()));
        }

        if (identifier.equals("puzzles_solved")) {
            return util.formatNumber(bitcoinManager.getPuzzlesSolved(player.getUniqueId()));
        }
        return null;
    }
}