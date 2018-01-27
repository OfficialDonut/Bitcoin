package us._donut_.bitcoin;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;

public class RegisterPlaceholderAPI extends EZPlaceholderHook {

    private BitcoinManager bitcoinManager;

    public RegisterPlaceholderAPI(Bitcoin pluginInstance) {
        super(pluginInstance, "bitcoin");
        bitcoinManager = pluginInstance.getBitcoinManager();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equals("value")) {
            return bitcoinManager.getExchangeCurrencySymbol() + bitcoinManager.getBitcoinValue();
        }

        if (identifier.equals("bank")) {
            return String.valueOf(bitcoinManager.getAmountInBank());
        }

        if (identifier.equals("tax")) {
            return bitcoinManager.getPurchaseTaxPercentage() + "%";
        }

        if (identifier.equals("circulation")) {
            return String.valueOf(bitcoinManager.getBitcoinsInCirculation());
        }

        if (identifier.equals("circulation_limit")) {
            return String.valueOf(bitcoinManager.getCirculationLimit());
        }

        if (player == null) {
            return "";
        }

        if (identifier.equals("balance")) {
            return String.valueOf(bitcoinManager.getBalance(player.getUniqueId()));
        }

        if (identifier.equals("amount_mined")) {
            return String.valueOf(bitcoinManager.getBitcoinsMined(player.getUniqueId()));
        }

        if (identifier.equals("puzzles_solved")) {
            return String.valueOf(bitcoinManager.getPuzzlesSolved(player.getUniqueId()));
        }
        return null;
    }
}