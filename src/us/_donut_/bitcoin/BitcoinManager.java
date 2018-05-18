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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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
    private Map<UUID, Long> puzzleTimes = new HashMap<>();
    private Map<UUID, File> playerFiles = new HashMap<>();
    private Map<UUID, YamlConfiguration> playerFileConfigs = new HashMap<>();
    private double lastRealValue = 1000;
    private boolean useRealValue;
    private double bitcoinValue;
    private double bitcoinMinValue;
    private double bitcoinMaxValue;
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
    private BukkitTask inactivityChecker;
    private BukkitTask notifyRealValue;
    private double inactivityPeriod;
    private boolean broadcastBalanceReset;
    private boolean broadcastRealValue;

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
        bitcoinMinValue = plugin.getBitcoinConfig().getDouble("bitcoin_min_value");
        bitcoinMaxValue = plugin.getBitcoinConfig().getDouble("bitcoin_max_value");
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
                puzzleTimes.put(playerUUID, config.getLong("best_puzzle_time"));
            }
        }

        inactivityPeriod = plugin.getBitcoinConfig().getDouble("days_of_inactivity_until_balance_reset") * 24 * 60 * 60 * 1000;
        broadcastBalanceReset = plugin.getBitcoinConfig().getBoolean("broadcast_balance_reset_message");
        if (inactivityChecker != null) {inactivityChecker.cancel();}
        if (inactivityPeriod > 0) {
            runInactivityChecker();
        }

        useRealValue = plugin.getBitcoinConfig().getBoolean("use_real_value");
        if (timeChecker != null) { timeChecker.cancel(); }
        if (frequencyChecker != null) { frequencyChecker.cancel(); }
        if (!useRealValue) {
            String frequencyString = plugin.getBitcoinConfig().getString("fluctuation_frequency");
            if (frequencyString.contains(":")) {
                Long timeInTicks = util.getTicksFromTime(frequencyString);
                if (timeInTicks == null) {
                    timeInTicks = 1L;
                }
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
        broadcastRealValue = plugin.getBitcoinConfig().getBoolean("broadcast_real_value");
        if (notifyRealValue != null) { notifyRealValue.cancel(); }
        if (useRealValue && broadcastRealValue) {
            String frequencyString = plugin.getBitcoinConfig().getString("fluctuation_frequency");
            if (frequencyString.contains(":")) {
                Long timeInTicks = util.getTicksFromTime(frequencyString);
                if (timeInTicks == null) {
                    timeInTicks = 1L;
                }
                startRealValueTimeChecker(timeInTicks);
            } else {
                long frequency;
                try {
                    frequency = Long.valueOf(frequencyString);
                } catch (NumberFormatException e) {
                    frequency = 24000L;
                }
                startBroadcastRealValue(frequency);
            }
        }
    }

    Map<UUID, YamlConfiguration> getPlayerFileConfigs() { return playerFileConfigs; }
    Double getAmountInBank() { return amountInBank; }
    Double getPurchaseTaxPercentage() { return purchaseTaxPercentage; }
    Double getBalance(UUID playerUUID) { return balances.getOrDefault(playerUUID, 0.0); }
    Integer getPuzzlesSolved(UUID playerUUID) { return puzzlesSolved.getOrDefault(playerUUID, 0); }
    Double getBitcoinsMined(UUID playerUUID) { return bitcoinsMined.getOrDefault(playerUUID, 0.0); }
    Long getBestPuzzleTime(UUID playerUUID) { return puzzleTimes.getOrDefault(playerUUID, 0L); }
    Integer getDisplayRoundAmount() { return displayRoundAmount; }
    Double getCirculationLimit() { return circulationLimit; }
    String getExchangeCurrencySymbol() { return exchangeCurrencySymbol; }

    Double getBitcoinValue() {
        if (useRealValue) {
             try {
                 URL address = new URL("https://blockchain.info/ticker");
                 InputStreamReader pageInput = new InputStreamReader(address.openStream());
                 BufferedReader source = new BufferedReader(pageInput);
                 source.readLine();
                 double value = Double.valueOf(source.readLine().split("\"last\" : ")[1].split(",")[0]);
                 lastRealValue = value;
                 return util.round(2, value);
             } catch (IOException | NumberFormatException e) {
                 return util.round(2, lastRealValue);
             }
        } else {
            return bitcoinValue;
        }
    }

    Double getBitcoinsInCirculation() {
        double bitcoins = 0;
        for (double balance : balances.values()) {
            bitcoins += balance;
        }
        bitcoins += amountInBank;
        return bitcoins;
    }

    List<OfflinePlayer> getTopBalPlayers() {
        Map<UUID, Double> sortedBalances = balances.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        List<OfflinePlayer> topPlayers = new ArrayList<>();
        for (UUID uuid : sortedBalances.keySet()) {
            topPlayers.add(Bukkit.getOfflinePlayer(uuid));
        }
        return topPlayers;
    }

    List<OfflinePlayer> getTopTimePlayers() {
        Map<UUID, Long> sortedTimes = puzzleTimes.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        List<OfflinePlayer> topPlayers = new ArrayList<>();
        for (UUID uuid : sortedTimes.keySet()) {
            if (sortedTimes.get(uuid) != 0) {
                topPlayers.add(Bukkit.getOfflinePlayer(uuid));
            }
        }
        return topPlayers;
    }

    List<OfflinePlayer> getTopSolvedPlayers() {
        Map<UUID, Integer> sortedSolved = puzzlesSolved.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        List<OfflinePlayer> topPlayers = new ArrayList<>();
        for (UUID uuid : sortedSolved.keySet()) {
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

    void setBestPuzzleTime(UUID playerUUID, long amount) {
        puzzleTimes.put(playerUUID, amount);
        playerFileConfigs.get(playerUUID).set("best_puzzle_time", puzzleTimes.get(playerUUID));
        util.saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID));
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
        if (!playerFileConfig.contains("best_puzzle_time")) { setBestPuzzleTime(event.getPlayer().getUniqueId(), 0); }
    }

    void fluctuate() {
        Random random = new Random();
        double fluctuation = util.round(2, minFluctuation + (random.nextDouble() * (maxFluctuation - minFluctuation)));
        if (random.nextBoolean()) { fluctuation = fluctuation * -1; }
        if (bitcoinValue + fluctuation < bitcoinMinValue) {
            fluctuation = util.round(2, bitcoinValue - bitcoinMinValue);
            bitcoinValue = bitcoinMinValue;
        } else if (bitcoinMaxValue > 0 && bitcoinValue + fluctuation > bitcoinMaxValue) {
            fluctuation = util.round(2, bitcoinMaxValue - bitcoinValue);
            bitcoinValue = bitcoinMaxValue;
        } else {
            bitcoinValue = util.round(2, bitcoinValue + fluctuation);
        }
        plugin.getBitcoinConfig().set("bitcoin_value", bitcoinValue);
        util.saveYml(plugin.getConfigFile(), plugin.getBitcoinConfig());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (bitcoinValue > (bitcoinValue - fluctuation) && bitcoinValue != bitcoinMinValue) {
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

    private void runInactivityChecker() {
        inactivityChecker = new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : balances.keySet()) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    if (player != null) {
                        if ((System.currentTimeMillis() - player.getLastPlayed()) > inactivityPeriod) {
                            if (balances.get(uuid) > 0 && !player.isOnline()) {
                                if (broadcastBalanceReset) {
                                    Bukkit.broadcastMessage(messages.getMessage("inactive_balance_reset").replace("{AMOUNT}", String.valueOf(balances.get(uuid))).replace("{PLAYER}", player.getName()));
                                }
                                addToBank(balances.get(uuid));
                                setBalance(uuid, 0);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 12000);
    }

    private void startBroadcastRealValue(long frequency) {
        notifyRealValue = new BukkitRunnable() {
            long timeSinceLastBroadcast = 0L;
            Boolean alreadyBroadcasted = false;
            @Override
            public void run() {
                timeSinceLastBroadcast++;
                if (timeSinceLastBroadcast == frequency) {
                    if (!alreadyBroadcasted) {
                        for (Player player : plugin.getServer().getOnlinePlayers()) {
                            player.sendMessage(messages.getMessage("real_value_announcement").replace("{VALUE}", exchangeCurrencySymbol + util.formatNumber(getBitcoinValue())));
                            player.playSound(player.getLocation(), sounds.getSound("real_value_announcement"), 1, 1);
                        }
                        timeSinceLastBroadcast = 0;
                        alreadyBroadcasted = true;
                    }
                }
                if (alreadyBroadcasted && timeSinceLastBroadcast != frequency) {
                    alreadyBroadcasted = false;
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void startRealValueTimeChecker(long timeInTicks) {
        notifyRealValue = new BukkitRunnable() {
            Boolean alreadyBroadcasted = false;
            @Override
            public void run() {
                if (world.getTime() % 24000 == timeInTicks) {
                    if (!alreadyBroadcasted) {
                        for (Player player : plugin.getServer().getOnlinePlayers()) {
                            player.sendMessage(messages.getMessage("real_value_announcement").replace("{VALUE}", exchangeCurrencySymbol + util.formatNumber(getBitcoinValue())));
                            player.playSound(player.getLocation(), sounds.getSound("real_value_announcement"), 1, 1);
                        }
                        alreadyBroadcasted = true;
                    }
                }
                if (alreadyBroadcasted && world.getTime() % 24000 != timeInTicks) {
                    alreadyBroadcasted = false;
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}