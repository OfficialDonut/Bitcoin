package us._donut_.bitcoin;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;

public class RegisterPlaceholderAPI extends EZPlaceholderHook {

    private BitcoinManager bitcoinManager;
    private Util util;

    public RegisterPlaceholderAPI() {
        super(Bitcoin.plugin, "bitcoin");
        bitcoinManager = Bitcoin.plugin.getBitcoinManager();
        util = Bitcoin.plugin.getUtil();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equals("value")) {
            return bitcoinManager.getExchangeCurrencySymbol() + util.formatRound2Number(bitcoinManager.getBitcoinValue());
        }

        if (identifier.equals("bank")) {
            return util.formatRoundNumber(bitcoinManager.getAmountInBank());
        }

        if (identifier.equals("tax")) {
            return bitcoinManager.getPurchaseTaxPercentage() + "%";
        }

        if (identifier.equals("circulation")) {
            return util.formatRoundNumber(bitcoinManager.getBitcoinsInCirculation());
        }

        if (identifier.equals("circulation_limit")) {
            return util.formatRoundNumber(bitcoinManager.getCirculationLimit());
        }

        if (player == null) {
            return "";
        }

        if (identifier.equals("balance")) {
            return util.formatRoundNumber(bitcoinManager.getBalance(player.getUniqueId()));
        }

        if (identifier.equals("amount_mined")) {
            return util.formatRoundNumber(bitcoinManager.getBitcoinsMined(player.getUniqueId()));
        }

        if (identifier.equals("puzzles_solved")) {
            return util.formatNumber(bitcoinManager.getPuzzlesSolved(player.getUniqueId()));
        }
        return null;
    }
}