package us.donut.bitcoin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import us.donut.bitcoin.config.BitcoinConfig;
import us.donut.bitcoin.config.Messages;
import us.donut.bitcoin.config.Sounds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class BitcoinManager {

    private static BitcoinManager instance;
    private Bitcoin plugin;
    private PlayerDataManager playerDataManager;
    private World newDayWorld;
    private Random random;
    private BukkitTask timeChecker;
    private BukkitTask frequencyChecker;
    private BukkitTask inactivityChecker;

    private boolean useRealValue;
    private boolean broadcastBalanceReset;
    private boolean broadcastRealValue;
    private boolean alreadyFluctuated;

    private double bitcoinValue;
    private double bitcoinMinValue;
    private double bitcoinMaxValue;
    private double minFluctuation;
    private double maxFluctuation;
    private double circulationLimit;
    private double amountInBank;
    private double purchaseTaxPercentage;
    private double inactivityPeriod;
    private int displayRoundAmount;

    private BitcoinManager() {
        plugin = Bitcoin.getInstance();
        playerDataManager = PlayerDataManager.getInstance();
        random = new Random();
    }

    public static BitcoinManager getInstance() {
        return instance != null ? instance : (instance = new BitcoinManager());
    }

    void reload() {
        newDayWorld = Bukkit.getWorld(BitcoinConfig.getString("world"));
        if (newDayWorld ==  null) {
            newDayWorld = Bukkit.getWorlds().get(0);
        }

        useRealValue = BitcoinConfig.getBoolean("use_real_value");
        broadcastBalanceReset = BitcoinConfig.getBoolean("broadcast_balance_reset_message");
        broadcastRealValue = BitcoinConfig.getBoolean("broadcast_real_value");

        bitcoinValue = useRealValue ? getRealBitcoinValue() : BitcoinConfig.getDouble("bitcoin_value");
        bitcoinMinValue = BitcoinConfig.getDouble("bitcoin_min_value");
        bitcoinMaxValue = BitcoinConfig.getDouble("bitcoin_max_value");
        minFluctuation = BitcoinConfig.getDouble("min_bitcoin_value_fluctuation");
        maxFluctuation = BitcoinConfig.getDouble("max_bitcoin_value_fluctuation");
        circulationLimit = BitcoinConfig.getDouble("circulation_limit");
        amountInBank = BitcoinConfig.getDouble("amount_in_bank");
        purchaseTaxPercentage = BitcoinConfig.getDouble("purchase_tax_percentage");
        inactivityPeriod = BitcoinConfig.getDouble("days_of_inactivity_until_balance_reset");
        displayRoundAmount = BitcoinConfig.getInt("bitcoin_display_rounding");

        if (timeChecker != null) { timeChecker.cancel(); }
        if (frequencyChecker != null) { frequencyChecker.cancel(); }

        String frequencyString = BitcoinConfig.getString("fluctuation_frequency");
        if (!frequencyString.contains(":")) {
            try {
                runFrequencyChecker(Long.parseLong(frequencyString));
            } catch (NumberFormatException e) {
                runFrequencyChecker(24000L);
            }
        } else {
            runTimeChecker(Util.getTicksFromTime(frequencyString));
        }

        inactivityPeriod = BitcoinConfig.getDouble("days_of_inactivity_until_balance_reset") * 24 * 60 * 60 * 1000;
        broadcastBalanceReset = BitcoinConfig.getBoolean("broadcast_balance_reset_message");
        if (inactivityChecker != null) { inactivityChecker.cancel(); }
        if (inactivityPeriod > 0) {
            runInactivityChecker();
        }
    }

    private double getRealBitcoinValue() {
        try {
            URL address = new URL("https://blockchain.info/ticker");
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(address.openStream()))) {
                bufferedReader.readLine();
                double value = Double.parseDouble(bufferedReader.readLine().split("\"last\" : ")[1].split(",")[0]);
                return Util.round(2, value);
            }
        } catch (IOException | NumberFormatException e) {
            return bitcoinValue;
        }
    }

    void fluctuate() {
        double fluctuation;
        if (!useRealValue) {
            fluctuation = minFluctuation + (random.nextDouble() * (maxFluctuation - minFluctuation));
            if (random.nextBoolean()) { fluctuation *= -1; }
            if (bitcoinValue + fluctuation < bitcoinMinValue) {
                fluctuation = bitcoinValue - bitcoinMinValue;
            } else if (bitcoinMaxValue > 0 && bitcoinValue + fluctuation > bitcoinMaxValue) {
                fluctuation = bitcoinMaxValue - bitcoinValue;
            }
        } else {
            fluctuation = getRealBitcoinValue() - bitcoinValue;
        }
        fluctuation = Util.round(2, fluctuation);
        bitcoinValue = Util.round(2, bitcoinValue + fluctuation);
        BitcoinConfig.set("bitcoin_value", bitcoinValue);

        if (!useRealValue || broadcastRealValue) {
            String message = Messages.get(fluctuation > 0 ? "value_increase" : "value_decrease", bitcoinValue, fluctuation);
            Bukkit.getConsoleSender().sendMessage(message);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
                player.playSound(player.getLocation(), Sounds.get("value_change"), 1, 1);
            }
        }
    }

    private void runFrequencyChecker(long frequency) {
        frequencyChecker = Bukkit.getScheduler().runTaskTimer(plugin, this::fluctuate, frequency, frequency);
    }

    private void runTimeChecker(long time) {
        timeChecker = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (newDayWorld.getTime() % 24000 == time) {
                if (!alreadyFluctuated) {
                    fluctuate();
                    alreadyFluctuated = true;
                }
            } else if (alreadyFluctuated) {
                alreadyFluctuated = false;
            }
        },0, 1);
    }

    private void runInactivityChecker() {
        inactivityChecker = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, Double> entry : playerDataManager.getBalances().entrySet()) {
                UUID uuid = entry.getKey();
                double bal = entry.getValue();
                OfflinePlayer player = playerDataManager.getOfflinePlayer(uuid);
                if (bal > 0 && !player.isOnline()) {
                    long lastPlayed = playerDataManager.getLastPlayed(uuid);
                    if (System.currentTimeMillis() - lastPlayed > inactivityPeriod) {
                        addToBank(bal);
                        playerDataManager.setBalance(uuid, 0);
                        if (broadcastBalanceReset) {
                            Bukkit.broadcastMessage(Messages.get("inactive_balance_reset", bal, playerDataManager.getDisplayName(uuid)));
                        }
                    }
                }
            }
        },0, 18000);
    }

    public void setBank(double amount) {
        amountInBank = amount;
        BitcoinConfig.set("amount_in_bank", amountInBank);
    }

    public void addToBank(double amount) {
        setBank(amountInBank + amount);
    }

    public void removeFromBank(double amount) {
        setBank(amountInBank - amount);
    }

    public String format(double bitcoins) {
        return Util.formatNumber(Util.round(displayRoundAmount, bitcoins));
    }

    public String getFormattedValue() {
        return Util.formatRoundNumber(bitcoinValue);
    }

    public double getBitcoinValue() {
        return bitcoinValue;
    }

    public double getBitcoinsInCirculation() {
        return amountInBank + playerDataManager.getBalances().values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public double getAmountInBank() {
        return amountInBank;
    }

    public double getPurchaseTaxPercentage() {
        return purchaseTaxPercentage;
    }

    public double getCirculationLimit() {
        return circulationLimit;
    }
}
