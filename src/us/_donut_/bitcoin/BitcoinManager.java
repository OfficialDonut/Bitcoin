package us._donut_.bitcoin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import us._donut_.bitcoin.configuration.Message;
import us._donut_.bitcoin.configuration.Sounds;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static us._donut_.bitcoin.util.Util.*;

public class BitcoinManager implements Listener {

    private Bitcoin plugin = Bitcoin.plugin;
    private Sounds sounds;
    private Map<UUID, Double> balances = new HashMap<>();
    private Map<UUID, Integer> puzzlesSolved = new HashMap<>();
    private Map<UUID, Double> bitcoinsMined = new HashMap<>();
    private Map<UUID, Long> puzzleTimes = new HashMap<>();
    private Map<UUID, File> playerFiles = new HashMap<>();
    private Map<UUID, YamlConfiguration> playerFileConfigs = new HashMap<>();
    private Map<UUID, String> offlinePlayerDisplayNames = new HashMap<>();
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
    private boolean alreadyFluctuated = false;
    private boolean alreadyBroadcasted = false;
    private long timeSinceLastFluctuation = 0L;
    private long timeSinceLastBroadcast = 0L;
    private double bitcoinsInCirculation;
    private Random random = new Random();

    BitcoinManager() {
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
                OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
                if (player != null && player.getName() != null) {
                    playerFiles.put(playerUUID, file);
                    playerFileConfigs.put(playerUUID, config);
                    balances.put(playerUUID, config.getDouble("balance"));
                    puzzlesSolved.put(playerUUID, config.getInt("puzzles_solved"));
                    bitcoinsMined.put(playerUUID, config.getDouble("bitcoins_mined"));
                    puzzleTimes.put(playerUUID, config.getLong("best_puzzle_time"));
                    if (config.contains("display_name")) {
                        offlinePlayerDisplayNames.put(playerUUID, config.getString("display_name"));
                    }
                    getUUIDOfflinePlayerCache().put(playerUUID, player);
                }
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
                Long timeInTicks = getTicksFromTime(frequencyString);
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
                Long timeInTicks = getTicksFromTime(frequencyString);
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
    public Double getAmountInBank() { return amountInBank; }
    public Double getPurchaseTaxPercentage() { return purchaseTaxPercentage; }
    public Double getBalance(UUID playerUUID) { return balances.getOrDefault(playerUUID, 0.0); }
    public Integer getPuzzlesSolved(UUID playerUUID) { return puzzlesSolved.getOrDefault(playerUUID, 0); }
    public Double getBitcoinsMined(UUID playerUUID) { return bitcoinsMined.getOrDefault(playerUUID, 0.0); }
    public Long getBestPuzzleTime(UUID playerUUID) { return puzzleTimes.getOrDefault(playerUUID, 0L); }
    public Integer getDisplayRoundAmount() { return displayRoundAmount; }
    public Double getCirculationLimit() { return circulationLimit; }
    public String getExchangeCurrencySymbol() { return exchangeCurrencySymbol; }

    public Double getBitcoinValue() {
        if (useRealValue) {
             try {
                 URL address = new URL("https://blockchain.info/ticker");
                 InputStreamReader pageInput = new InputStreamReader(address.openStream());
                 BufferedReader source = new BufferedReader(pageInput);
                 source.readLine();
                 double value = Double.valueOf(source.readLine().split("\"last\" : ")[1].split(",")[0]);
                 lastRealValue = value;
                 return round(2, value);
             } catch (IOException | NumberFormatException e) {
                 return round(2, lastRealValue);
             }
        } else {
            return bitcoinValue;
        }
    }

    public Double getBitcoinsInCirculation() {
        bitcoinsInCirculation = amountInBank;
        balances.values().forEach(balance -> bitcoinsInCirculation += balance);
        return bitcoinsInCirculation;
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

    String getOfflinePlayerName(OfflinePlayer offlinePlayer) {
        return offlinePlayerDisplayNames.getOrDefault(offlinePlayer.getUniqueId(), offlinePlayer.getName());
    }

    void setBalance(UUID playerUUID, double balance) {
        balances.put(playerUUID, balance);
        playerFileConfigs.get(playerUUID).set("balance", balance);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID)));
    }

    void withdraw(UUID playerUUID, double amount) {
        balances.put(playerUUID, balances.get(playerUUID) - amount);
        playerFileConfigs.get(playerUUID).set("balance", balances.get(playerUUID));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID)));
    }

    void deposit(UUID playerUUID, double amount) {
        balances.put(playerUUID, balances.get(playerUUID) + amount);
        playerFileConfigs.get(playerUUID).set("balance", balances.get(playerUUID));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID)));
    }

    void setPuzzlesSolved(UUID playerUUID, int amount) {
        puzzlesSolved.put(playerUUID, amount);
        playerFileConfigs.get(playerUUID).set("puzzles_solved", amount);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID)));
    }

    void setBitcoinsMined(UUID playerUUID, double amount) {
        bitcoinsMined.put(playerUUID, amount);
        playerFileConfigs.get(playerUUID).set("bitcoins_mined", bitcoinsMined.get(playerUUID));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID)));
    }

    void addToBank(double amount) {
        amountInBank += amount;
        plugin.getBitcoinConfig().set("amount_in_bank", amountInBank);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveYml(plugin.getConfigFile(), plugin.getBitcoinConfig()));
    }

    void removeFromBank(double amount) {
        amountInBank -= amount;
        plugin.getBitcoinConfig().set("amount_in_bank", amountInBank);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveYml(plugin.getConfigFile(), plugin.getBitcoinConfig()));
    }

    void setBestPuzzleTime(UUID playerUUID, long time) {
        puzzleTimes.put(playerUUID, time);
        playerFileConfigs.get(playerUUID).set("best_puzzle_time", puzzleTimes.get(playerUUID));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID)));
    }

    private void setOfflinePlayerName(UUID playerUUID, String name) {
        offlinePlayerDisplayNames.put(playerUUID, name);
        playerFileConfigs.get(playerUUID).set("display_name", name);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID)));
    }

    void resetBalances() {
        balances.keySet().forEach(uuid -> setBalance(uuid, 0));
    }

    void resetMined() {
        bitcoinsMined.keySet().forEach(uuid -> setBitcoinsMined(uuid, 0));
    }

    void resetSolved() {
        puzzlesSolved.keySet().forEach(uuid -> setPuzzlesSolved(uuid, 0));
    }

    void resetTimes() {
        puzzleTimes.keySet().forEach(uuid -> setBestPuzzleTime(uuid, 0L));
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onJoin(PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (!playerFiles.containsKey(playerUUID)) {
            File file = new File(plugin.getDataFolder() + File.separator + "Player Data" + File.separator + playerUUID.toString() + ".yml");
            playerFiles.put(playerUUID, file);
            playerFileConfigs.put(playerUUID, YamlConfiguration.loadConfiguration(file));
        }
        File playerFile = playerFiles.get(playerUUID);
        YamlConfiguration playerFileConfig = playerFileConfigs.get(playerUUID);
        if (!playerFileConfig.contains("balance")) { setBalance(playerUUID, 0); }
        if (!playerFileConfig.contains("puzzles_solved")) { setPuzzlesSolved(playerUUID, 0); }
        if (!playerFileConfig.contains("bitcoins_mined")) { setBitcoinsMined(playerUUID, 0); }
        if (!playerFileConfig.contains("best_puzzle_time")) { setBestPuzzleTime(playerUUID, 0); }
        getLastPlayedCache().remove(playerUUID);
        Bukkit.getScheduler().runTaskLater(plugin, () -> setOfflinePlayerName(event.getPlayer().getUniqueId(), event.getPlayer().getDisplayName()), 20);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        File playerFile = playerFiles.get(playerUUID);
        YamlConfiguration playerFileConfig = playerFileConfigs.get(playerUUID);
        setOfflinePlayerName(event.getPlayer().getUniqueId(), event.getPlayer().getDisplayName());
        getLastPlayedCache().put(playerUUID, System.currentTimeMillis());
    }

    void fluctuate() {
        double fluctuation = round(2, minFluctuation + (random.nextDouble() * (maxFluctuation - minFluctuation)));
        if (random.nextBoolean()) { fluctuation = fluctuation * -1; }
        if (bitcoinValue + fluctuation < bitcoinMinValue) {
            fluctuation = round(2, bitcoinValue - bitcoinMinValue);
            bitcoinValue = bitcoinMinValue;
        } else if (bitcoinMaxValue > 0 && bitcoinValue + fluctuation > bitcoinMaxValue) {
            fluctuation = round(2, bitcoinMaxValue - bitcoinValue);
            bitcoinValue = bitcoinMaxValue;
        } else {
            bitcoinValue = round(2, bitcoinValue + fluctuation);
        }
        plugin.getBitcoinConfig().set("bitcoin_value", bitcoinValue);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveYml(plugin.getConfigFile(), plugin.getBitcoinConfig()));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (bitcoinValue > (bitcoinValue - fluctuation) && bitcoinValue != bitcoinMinValue) {
                player.sendMessage(Message.VALUE_INCREASE.toString()
                        .replace("{VALUE}", exchangeCurrencySymbol + bitcoinValue)
                        .replace("{CHANGE}", exchangeCurrencySymbol + (fluctuation)));
            } else {
                player.sendMessage(Message.VALUE_DECREASE.toString()
                        .replace("{VALUE}", exchangeCurrencySymbol + bitcoinValue)
                        .replace("{CHANGE}", exchangeCurrencySymbol + (fluctuation * -1)));
            }
            player.playSound(player.getLocation(), sounds.getSound("value_change"), 1, 1);
        }
    }

    private void runTimeChecker(long timeInTicks) {
        timeChecker = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (world.getTime() % 24000 == timeInTicks) {
                if (!alreadyFluctuated) {
                    fluctuate();
                    alreadyFluctuated = true;
                }
            }
            if (alreadyFluctuated && world.getTime() % 24000 != timeInTicks) {
                alreadyFluctuated = false;
            }
        },0, 1);
    }

    private void runFrequencyChecker(long frequency) {
        frequencyChecker = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
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
        },0, 1);
    }

    private void runInactivityChecker() {
        inactivityChecker = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            balances.forEach((uuid, balance) -> {
                OfflinePlayer player = getUUIDOfflinePlayerCache().getOrDefault(uuid, Bukkit.getOfflinePlayer(uuid));
                getUUIDOfflinePlayerCache().put(uuid, player);
                if (balance > 0 && !player.isOnline()) {
                    long lastPlayed = getLastPlayedCache().getOrDefault(uuid, player.getLastPlayed());
                    getLastPlayedCache().put(uuid, lastPlayed);
                    if (System.currentTimeMillis() - lastPlayed > inactivityPeriod) {
                        if (broadcastBalanceReset) {
                            Bukkit.broadcastMessage(Message.INACTIVE_BALANCE_RESET.toString()
                                    .replace("{AMOUNT}", String.valueOf(balances.get(uuid)))
                                    .replace("{PLAYER}", player.getName()));
                        }
                        addToBank(balances.get(uuid));
                        setBalance(uuid, 0);
                    }
                }
            });
        },0, 18000);
    }

    private void startBroadcastRealValue(long frequency) {
        notifyRealValue = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            timeSinceLastBroadcast++;
            if (timeSinceLastBroadcast == frequency) {
                if (!alreadyBroadcasted) {
                    String value = formatRound2Number(getBitcoinValue());
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        player.sendMessage(Message.REAL_VALUE_ANNOUNCEMENT.toString().replace("{VALUE}", exchangeCurrencySymbol + value));
                        player.playSound(player.getLocation(), sounds.getSound("real_value_announcement"), 1, 1);
                    }
                    timeSinceLastBroadcast = 0;
                    alreadyBroadcasted = true;
                }
            }
            if (alreadyBroadcasted && timeSinceLastBroadcast != frequency) {
                alreadyBroadcasted = false;
            }
        },0, 1);
    }

    private void startRealValueTimeChecker(long timeInTicks) {
        notifyRealValue = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (world.getTime() % 24000 == timeInTicks) {
                if (!alreadyBroadcasted) {
                    String value = formatRound2Number(getBitcoinValue());
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        player.sendMessage(Message.REAL_VALUE_ANNOUNCEMENT.toString().replace("{VALUE}", exchangeCurrencySymbol + value));
                        player.playSound(player.getLocation(), sounds.getSound("real_value_announcement"), 1, 1);
                    }
                    alreadyBroadcasted = true;
                }
            }
            if (alreadyBroadcasted && world.getTime() % 24000 != timeInTicks) {
                alreadyBroadcasted = false;
            }
        },0, 1);
    }
}