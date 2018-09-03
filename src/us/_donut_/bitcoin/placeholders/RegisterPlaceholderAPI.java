package us._donut_.bitcoin.placeholders;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;
import us._donut_.bitcoin.Bitcoin;
import us._donut_.bitcoin.BitcoinManager;

import static us._donut_.bitcoin.util.Util.*;

public class RegisterPlaceholderAPI extends EZPlaceholderHook {

    private BitcoinManager bitcoinManager;

    public RegisterPlaceholderAPI() {
        super(Bitcoin.plugin, "bitcoin");
        bitcoinManager = Bitcoin.plugin.getBitcoinManager();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equals("value")) {
            return bitcoinManager.getExchangeCurrencySymbol() + formatRound2Number(bitcoinManager.getBitcoinValue());
        }

        if (identifier.equals("bank")) {
            return formatRoundNumber(bitcoinManager.getAmountInBank());
        }

        if (identifier.equals("tax")) {
            return bitcoinManager.getPurchaseTaxPercentage() + "%";
        }

        if (identifier.equals("circulation")) {
            return formatRoundNumber(bitcoinManager.getBitcoinsInCirculation());
        }

        if (identifier.equals("circulation_limit")) {
            return formatRoundNumber(bitcoinManager.getCirculationLimit());
        }

        if (player == null) {
            return "";
        }

        if (identifier.equals("balance")) {
            return formatRoundNumber(bitcoinManager.getBalance(player.getUniqueId()));
        }

        if (identifier.equals("amount_mined")) {
            return formatRoundNumber(bitcoinManager.getBitcoinsMined(player.getUniqueId()));
        }

        if (identifier.equals("puzzles_solved")) {
            return formatNumber(bitcoinManager.getPuzzlesSolved(player.getUniqueId()));
        }
        return null;
    }
}