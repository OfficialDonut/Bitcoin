package us._donut_.bitcoin;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class BitcoinManager implements Listener {

    private Bitcoin plugin;
    private Util util;
    private Messages messages;
    private Map<Player, Integer> balances = new HashMap<>();
    private Map<Player, Integer> puzzlesSolved = new HashMap<>();
    private Map<Player, Integer> bitcoinsMined = new HashMap<>();
    private Map<Player, File> playerFiles = new HashMap<>();
    private Map<Player, YamlConfiguration> playerFileConfigs = new HashMap<>();
    private Double bitcoinValue;
    private Double minFluctuation;
    private Double maxFluctuation;
    private String exchangeCurrencySymbol;
    private World world;

    BitcoinManager(Bitcoin pluginInstance) {
        plugin = pluginInstance;
        util = plugin.getUtil();
        messages = plugin.getMessages();

        bitcoinValue = plugin.getBitcoinConfig().getDouble("bitcoin_value");
        exchangeCurrencySymbol = plugin.getBitcoinConfig().getString("exchange_currency_symbol");
        world = Bukkit.getWorld(plugin.getBitcoinConfig().getString("world"));
        if (world == null) { world = Bukkit.getWorlds().get(0); }
        minFluctuation = plugin.getBitcoinConfig().getDouble("min_bitcoin_value_fluctuation");
        maxFluctuation = plugin.getBitcoinConfig().getDouble("max_bitcoin_value_fluctuation");
        if (minFluctuation > maxFluctuation) {
            minFluctuation = plugin.getBitcoinConfig().getDouble("max_bitcoin_value_fluctuation");
            maxFluctuation = plugin.getBitcoinConfig().getDouble("min_bitcoin_value_fluctuation");
        }
        newDayChecker();
    }

    Integer getBalance(Player player) { return balances.get(player); }
    Integer getPuzzlesSolved(Player player) { return puzzlesSolved.get(player); }
    Integer getBitcoinsMined(Player player) { return bitcoinsMined.get(player); }
    Double getBitcoinValue() { return bitcoinValue; }
    String getExchangeCurrencySymbol() { return exchangeCurrencySymbol; }

    private void setBalance(Player player, int balance) {
        balances.put(player, balance);
        playerFileConfigs.get(player).set("balance", balance);
        util.saveYml(playerFiles.get(player), playerFileConfigs.get(player));
    }

    void withdraw(Player player, int amount) {
        balances.put(player, balances.get(player) - amount);
        playerFileConfigs.get(player).set("balance", balances.get(player));
        util.saveYml(playerFiles.get(player), playerFileConfigs.get(player));
    }

    void deposit(Player player, int amount) {
        balances.put(player, balances.get(player) + amount);
        playerFileConfigs.get(player).set("balance", balances.get(player));
        util.saveYml(playerFiles.get(player), playerFileConfigs.get(player));
    }

    void setPuzzlesSolved(Player player, int balance) {
        puzzlesSolved.put(player, balance);
        playerFileConfigs.get(player).set("puzzles_solved", balance);
        util.saveYml(playerFiles.get(player), playerFileConfigs.get(player));
    }

    void setBitcoinsMined(Player player, int balance) {
        bitcoinsMined.put(player, balance);
        playerFileConfigs.get(player).set("bitcoins_mined", balance);
        util.saveYml(playerFiles.get(player), playerFileConfigs.get(player));
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onJoin(PlayerJoinEvent event) {
        File playerFile = new File(plugin.getDataFolder() + File.separator + "Player Data", event.getPlayer().getUniqueId().toString() + ".yml");
        YamlConfiguration playerFileConfig = YamlConfiguration.loadConfiguration(playerFile);
        playerFiles.put(event.getPlayer(), playerFile);
        playerFileConfigs.put(event.getPlayer(), playerFileConfig);
        if (playerFileConfig.contains("balance")) {
            setBalance(event.getPlayer(), playerFileConfig.getInt("balance"));
        } else {
            setBalance(event.getPlayer(), 0);
        }
        if (playerFileConfig.contains("puzzles_solved")) {
            setPuzzlesSolved(event.getPlayer(), playerFileConfig.getInt("puzzles_solved"));
        } else {
            setPuzzlesSolved(event.getPlayer(), 0);
        }
        if (playerFileConfig.contains("bitcoins_mined")) {
            setBitcoinsMined(event.getPlayer(), playerFileConfig.getInt("bitcoins_mined"));
        } else {
            setBitcoinsMined(event.getPlayer(), 0);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onQuit(PlayerQuitEvent event) {
        balances.remove(event.getPlayer());
        puzzlesSolved.remove(event.getPlayer());
        bitcoinsMined.remove(event.getPlayer());
        playerFiles.remove(event.getPlayer());
        playerFileConfigs.remove(event.getPlayer());
    }

    private void newDayChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (world.getTime() % 24000 == 1) {
                    Random random = new Random();
                    double fluctuation = util.round(minFluctuation + (random.nextDouble() * (maxFluctuation - minFluctuation)));
                    if (random.nextBoolean()) { fluctuation = fluctuation * -1; }
                    bitcoinValue = util.round(bitcoinValue + fluctuation);
                    plugin.getBitcoinConfig().set("bitcoin_value", bitcoinValue);
                    util.saveYml(plugin.getConfigFile(), plugin.getBitcoinConfig());
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (bitcoinValue > (bitcoinValue - fluctuation)) {
                            player.sendMessage(messages.getMessage("value_increase").replace("{VALUE}", exchangeCurrencySymbol + bitcoinValue).replace("{CHANGE}", exchangeCurrencySymbol + (fluctuation)));
                        } else {
                            player.sendMessage(messages.getMessage("value_decrease").replace("{VALUE}", exchangeCurrencySymbol + bitcoinValue).replace("{CHANGE}", exchangeCurrencySymbol + (fluctuation * -1)));
                        }
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}