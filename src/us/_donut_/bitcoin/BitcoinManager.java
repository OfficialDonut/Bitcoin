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
    private Map<UUID, Integer> bitcoinsMined = new HashMap<>();
    private Map<UUID, File> playerFiles = new HashMap<>();
    private Map<UUID, YamlConfiguration> playerFileConfigs = new HashMap<>();
    private double bitcoinValue;
    private int displayRoundAmount;
    private double minFluctuation;
    private double maxFluctuation;
    private String exchangeCurrencySymbol;
    private World world;
    private Double amountInBank;
    private Double purchaseTaxPercentage;
    private BukkitTask newDayChecker;
    private BukkitTask customFrequencyChecker;
    private long customFrequency;

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
        world = Bukkit.getWorld(plugin.getBitcoinConfig().getString("world"));
        if (world == null) { world = Bukkit.getWorlds().get(0); }
        customFrequency = plugin.getBitcoinConfig().getLong("fluctuation_frequency");
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
                bitcoinsMined.put(playerUUID, config.getInt("bitcoins_mined"));
            }
        }
        if (newDayChecker != null) { newDayChecker.cancel(); }
        if (customFrequencyChecker != null) { customFrequencyChecker.cancel(); }
        if (customFrequency == 24000L) {
            newDayChecker();
        } else {
            customFrequencyChecker();
        }
    }

    Map<UUID, YamlConfiguration> getPlayerFileConfigs() { return playerFileConfigs; }
    Double getAmountInBank() { return amountInBank; }
    Double getPurchaseTaxPercentage() { return purchaseTaxPercentage; }
    Double getBalance(UUID playerUUID) { return balances.get(playerUUID); }
    Integer getPuzzlesSolved(UUID playerUUID) { return puzzlesSolved.get(playerUUID); }
    Integer getBitcoinsMined(UUID playerUUID) { return bitcoinsMined.get(playerUUID); }
    Double getBitcoinValue() { return bitcoinValue; }
    Integer getDisplayRoundAmount() { return displayRoundAmount; }

    String getExchangeCurrencySymbol() { return exchangeCurrencySymbol; }

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

    void setPuzzlesSolved(UUID playerUUID, int balance) {
        puzzlesSolved.put(playerUUID, balance);
        playerFileConfigs.get(playerUUID).set("puzzles_solved", balance);
        util.saveYml(playerFiles.get(playerUUID), playerFileConfigs.get(playerUUID));
    }

    void setBitcoinsMined(UUID playerUUID, int balance) {
        bitcoinsMined.put(playerUUID, balance);
        playerFileConfigs.get(playerUUID).set("bitcoins_mined", balance);
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

    private void fluctuate() {
        Random random = new Random();
        double fluctuation = util.round(2, minFluctuation + (random.nextDouble() * (maxFluctuation - minFluctuation)));
        if (random.nextBoolean()) { fluctuation = fluctuation * -1; }
        bitcoinValue = util.round(2, bitcoinValue + fluctuation);
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

    private void newDayChecker() {
        newDayChecker = new BukkitRunnable() {
            @Override
            public void run() {
                if (world.getTime() % 24000 == 1) {
                    fluctuate();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void customFrequencyChecker() {
        customFrequencyChecker = new BukkitRunnable() {
            long timeSinceLastFluctuation = 0L;
            @Override
            public void run() {
                timeSinceLastFluctuation++;
                if (timeSinceLastFluctuation == customFrequency) {
                    fluctuate();
                    timeSinceLastFluctuation = 0;
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}