package us.donut.bitcoin.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import us.donut.bitcoin.Bitcoin;
import us.donut.bitcoin.BitcoinManager;
import us.donut.bitcoin.PlayerDataManager;
import us.donut.bitcoin.Util;

public class PapiHook extends PlaceholderExpansion {

    private Bitcoin plugin;
    private BitcoinManager bitcoinManager;
    private PlayerDataManager playerDataManager;

    public PapiHook() {
        plugin = Bitcoin.getInstance();
        bitcoinManager = BitcoinManager.getInstance();
        playerDataManager = PlayerDataManager.getInstance();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String getIdentifier() {
        return "bitcoin";
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
