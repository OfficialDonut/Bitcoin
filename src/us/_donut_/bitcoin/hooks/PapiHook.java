package us._donut_.bitcoin.hooks;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;
import us._donut_.bitcoin.Bitcoin;
import us._donut_.bitcoin.BitcoinManager;
import us._donut_.bitcoin.PlayerDataManager;
import us._donut_.bitcoin.Util;

public class PapiHook extends EZPlaceholderHook {

    private BitcoinManager bitcoinManager;
    private PlayerDataManager playerDataManager;

    public PapiHook() {
        super(Bitcoin.getInstance(), "bitcoin");
        bitcoinManager = BitcoinManager.getInstance();
        playerDataManager = PlayerDataManager.getInstance();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equals("value")) {
            return bitcoinManager.getFormattedValue();
        }

        if (identifier.equals("bank")) {
            return bitcoinManager.format(bitcoinManager.getAmountInBank());
        }

        if (identifier.equals("tax")) {
            return bitcoinManager.getPurchaseTaxPercentage() + "%";
        }

        if (identifier.equals("circulation")) {
            return bitcoinManager.format(bitcoinManager.getBitcoinsInCirculation());
        }

        if (identifier.equals("circulation_limit")) {
            return bitcoinManager.format(bitcoinManager.getCirculationLimit());
        }

        if (player == null) {
            return "";
        }

        if (identifier.equals("balance")) {
            return bitcoinManager.format(playerDataManager.getBalance(player.getUniqueId()));
        }

        if (identifier.equals("amount_mined")) {
            return bitcoinManager.format(playerDataManager.getBitcoinsMined(player.getUniqueId()));
        }

        if (identifier.equals("puzzles_solved")) {
            return Util.formatNumber(playerDataManager.getPuzzlesSolved(player.getUniqueId()));
        }

        return null;
    }
}
