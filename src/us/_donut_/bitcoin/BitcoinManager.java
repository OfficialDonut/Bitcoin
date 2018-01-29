package us._donut_.bitcoin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

class BitcoinManager implements Listener {

    private Bitcoin plugin;
    private Util util;
    private Messages messages;
    private Sounds sounds;
    private Map<UUID, Double> balances = new HashMap<>();
    private Map<UUID, Integer> puzzlesSolved = new HashMap<>();
    private Map<UUID, Double> bitcoinsMined = new HashMap<>();
    private Map<UUID, File> playerFiles = new HashMap<>();
    private Map<UUID, YamlConfiguration> playerFileConfigs = new HashMap<>();
    private double bitcoinValue;
    private int displayRoundAmount;
    private double minFluctuation;
    private double maxFluctuation;
    private double circulationLimit;
    private String exchangeCurrencySymbol;
    private World world;
    private Double amountInBank;
    private Double purchaseTaxPercentage;
    private BukkitTask timeChecker;
    private BukkitTask frequencyChecker;

    BitcoinManager(Bitcoin pluginInstance) {
        plugin = pluginInstance;
        util = plugin.getUtil();
        messages = plugin.getMessages();
        sounds = plugin.getSounds();
        reload();
    }

    void reload() {
        balances.clear();
        playerFiles.clear();
        playerFileConfigs.clear();
        puzzlesSolved.clear();
        bitcoinsMined.clear();

        amountInBank = plugin.getBitcoinConfig().getDouble("amount_in_bank");
        purchaseTaxPercentage = plugin.getBitcoinConfig().getDouble("purchase_tax_percentage");
        bitcoinValue = plugin.getBitcoinConfig().getDouble("bitcoin_value");
        displayRoundAmount = plugin.getBitcoinConfig().getInt("bitcoin_display_rounding");
        exchangeCurrencySymbol = plugin.getBitcoinConfig().getString("exchange_currency_symbol");
        circulationLimit = plugin.getBitcoinConfig().getDouble("circulation_limit");
        world = Bukkit.getWorld(plugin.getBitcoinConfig().getString("world"));
        if (world == null) { world = Bukkit.getWorlds().get(0); }
        minFluctuation = plugin.getBitcoinConfig().getDouble("min_bitcoin_value_fluctuation");
        maxFluctuation = plugin.getBitcoinConfig().getDouble("max_bitcoin_value_fluctuation");
        if (minFluctuation > maxFluctuation) {
            minFluctuation = plugin.getBitcoinConfig().getDouble("max_bitcoin_value_fluctuation");
            maxFluctuation = plugin.getBitcoinConfig().getDouble("min_bitcoin_value_fluctuation");
        }
        File[] playerDataFiles = new File(plugin.getDataFolder() + File.separator + "Player Data").listFiles();
        if (playerDataFiles != null) {
            for (File file : playerDataFiles) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                UUID playerUUID = UUID.fromString(file.getName().split("\\.yml")[0]);
                playerFiles.put(playerUUID, file);
                playerFileConfigs.put(playerUUID, config);
                balances.put(playerUUID, config.getDouble("balance"));
                puzzlesSolved.put(playerUUID, config.getInt("puzzles_solved"));
                bitcoinsMined.put(playerUUID, config.getDouble("bitcoins_mined"));
            }
        }

        if (timeChecker != null) { timeChecker.cancel(); }
        if (frequencyChecker != null) { frequencyChecker.cancel(); }
        String frequencyString = plugin.getBitcoinConfig().getString("fluctuation_frequency");
        if (frequencyString.contains(":")) {
            Long timeInTicks = util.getTicksFromTime(frequencyString);
            if (timeInTicks == null) { timeInTicks = 1L; }
            runTimeChecker(timeInTicks);
        } else {
            long frequency;
            try {
                frequency = Long.valueOf(frequencyString);
            } catch (NumberFormatException e) {
                frequency = 24000L;
            }
            runFrequencyChecker(frequency);
        }
    }

    Map<UUID, YamlConfiguration> getPlayerFileConfigs() { return playerFileConfigs; }
    Double getAmountInBank() { return amountInBank; }
    Double getPurchaseTaxPercentage() { return purchaseTaxPercentage; }
    Double getBalance(UUID playerUUID) { return balances.get(playerUUID); }
    Integer getPuzzlesSolved(UUID playerUUID) { return puzzlesSolved.get(playerUUID); }
    Double getBitcoinsMined(UUID playerUUID) { return bitcoinsMined.get(playerUUID); }
    Double getBitcoinValue() { return bitcoinValue; }
    Integer getDisplayRoundAmount() { return displayRoundAmount; }
    Double getCirculationLimit() { return circulationLimit; }
    String getExchangeCurrencySymbol() { return exchangeCurrencySymbol; }

    Double getBitcoinsInCirculation() {
        double bitcoins = 0;
        for (double balance : balances.values()) {
            bitcoins += balance;
        }
        bitcoins += amountInBank;
        return bitcoins;
    }

    List<OfflinePlayer> getTopPlayers() {
        Map<UUID, Double> sortedBalances = balances.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        List<OfflinePlayer> topPlayers = new ArrayList<>();
        for (UUID uuid : sortedBalances.keySet()) {
            topPlayers.add(Bukkit.getOfflinePlayer(uuid));
        }
        return topPlayers;
    }

    void setBalance(UUID playerUUID, double balance) {
        balances.put(playerUUID, balance);
        playerFileConfigs.get(playerUUID).set("balance", balance);
        util.saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID));
    }

    void withdraw(UUID playerUUID, double amount) {
        balances.put(playerUUID, balances.get(playerUUID) - amount);
        playerFileConfigs.get(playerUUID).set("balance", balances.get(playerUUID));
        util.saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID));
    }

    void deposit(UUID playerUUID, double amount) {
        balances.put(playerUUID, balances.get(playerUUID) + amount);
        playerFileConfigs.get(playerUUID).set("balance", balances.get(playerUUID));
        util.saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID));
    }

    void setPuzzlesSolved(UUID playerUUID, int amount) {
        puzzlesSolved.put(playerUUID, amount);
        playerFileConfigs.get(playerUUID).set("puzzles_solved", amount);
        util.saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID));
    }

    void setBitcoinsMined(UUID playerUUID, double amount) {
        bitcoinsMined.put(playerUUID, amount);
        playerFileConfigs.get(playerUUID).set("bitcoins_mined", bitcoinsMined.get(playerUUID));
        util.saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID));
    }

    void addToBank(double amount) {
        amountInBank += amount;
        plugin.getBitcoinConfig().set("amount_in_bank", amountInBank);
        util.saveYml(plugin.getConfigFile(), plugin.getBitcoinConfig());
    }

    void removeFromBank(double amount) {
        amountInBank -= amount;
        plugin.getBitcoinConfig().set("amount_in_bank", amountInBank);
        util.saveYml(plugin.getConfigFile(), plugin.getBitcoinConfig());
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onJoin(PlayerJoinEvent event) {
        if (!playerFiles.containsKey(event.getPlayer().getUniqueId())) {
            File file = new File(plugin.getDataFolder() + File.separator + "Player Data" + File.separator + event.getPlayer().getUniqueId().toString() + ".yml");
            playerFiles.put(event.getPlayer().getUniqueId(), file);
            playerFileConfigs.put(event.getPlayer().getUniqueId(), YamlConfiguration.loadConfiguration(file));
        }
        File playerFile = playerFiles.get(event.getPlayer().getUniqueId());
        YamlConfiguration playerFileConfig = playerFileConfigs.get(event.getPlayer().getUniqueId());
        if (!playerFileConfig.contains("balance")) { setBalance(event.getPlayer().getUniqueId(), 0); }
        if (!playerFileConfig.contains("puzzles_solved")) { setPuzzlesSolved(event.getPlayer().getUniqueId(), 0); }
        if (!playerFileConfig.contains("bitcoins_mined")) { setBitcoinsMined(event.getPlayer().getUniqueId(), 0); }
    }

    void fluctuate() {
        Random random = new Random();
        double fluctuation = util.round(2, minFluctuation + (random.nextDouble() * (maxFluctuation - minFluctuation)));
        if (random.nextBoolean()) { fluctuation = fluctuation * -1; }
        if (bitcoinValue + fluctuation < 0) {
            fluctuation = Math.abs(bitcoinValue);
            bitcoinValue = 0;
        } else {
            bitcoinValue = util.round(2, bitcoinValue + fluctuation);
        }
        plugin.getBitcoinConfig().set("bitcoin_value", bitcoinValue);
        util.saveYml(plugin.getConfigFile(), plugin.getBitcoinConfig());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (bitcoinValue > (bitcoinValue - fluctuation)) {
                player.sendMessage(messages.getMessage("value_increase").replace("{VALUE}", exchangeCurrencySymbol + bitcoinValue).replace("{CHANGE}", exchangeCurrencySymbol + (fluctuation)));
            } else {
                player.sendMessage(messages.getMessage("value_decrease").replace("{VALUE}", exchangeCurrencySymbol + bitcoinValue).replace("{CHANGE}", exchangeCurrencySymbol + (fluctuation * -1)));
            }
            player.playSound(player.getLocation(), sounds.getSound("value_change"), 1, 1);
        }
    }

    private void runTimeChecker(long timeInTicks) {
        timeChecker = new BukkitRunnable() {
            Boolean alreadyFluctuated = false;
            @Override
            public void run() {
                if (world.getTime() % 24000 == timeInTicks) {
                    if (!alreadyFluctuated) {
                        fluctuate();
                        alreadyFluctuated = true;
                    }
                }
                if (alreadyFluctuated && world.getTime() % 24000 != timeInTicks) {
                    alreadyFluctuated = false;
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void runFrequencyChecker(long frequency) {
        frequencyChecker = new BukkitRunnable() {
            long timeSinceLastFluctuation = 0L;
            Boolean alreadyFluctuated = false;
            @Override
            public void run() {
                timeSinceLastFluctuation++;
                if (timeSinceLastFluctuation == frequency) {
                    if (!alreadyFluctuated) {
                        fluctuate();
                        timeSinceLastFluctuation = 0;
                        alreadyFluctuated = true;
                    }
                }
                if (alreadyFluctuated && timeSinceLastFluctuation != frequency) {
                    alreadyFluctuated = false;
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}