package us.donut.bitcoin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerDataManager implements Listener {

    private static PlayerDataManager instance;
    private Bitcoin plugin;
    private Map<UUID, File> playerFiles = new HashMap<>();
    private Map<UUID, YamlConfiguration> playerConfigs = new HashMap<>();
    private Map<UUID, String> displayNames = new HashMap<>();
    private Map<UUID, Double> balances = new HashMap<>();
    private Map<UUID, Double> bitcoinsMined = new HashMap<>();
    private Map<UUID, Long> puzzlesSolved = new HashMap<>();
    private Map<UUID, Long> puzzleTimes = new HashMap<>();
    private Map<UUID, Long> lastPlayedCache = new HashMap<>();
    private Map<UUID, OfflinePlayer> offlinePlayerCache = new HashMap<>();

    private PlayerDataManager() {
        plugin = Bitcoin.getInstance();
    }

    public static PlayerDataManager getInstance() {
        return instance != null ? instance : (instance = new PlayerDataManager());
    }

    void reload() {
        playerFiles.clear();
        playerConfigs.clear();
        balances.clear();
        bitcoinsMined.clear();
        puzzlesSolved.clear();
        puzzleTimes.clear();

        File[] playerDataFiles = new File(plugin.getDataFolder() + File.separator + "Player Data").listFiles();
        if (playerDataFiles != null) {
            for (File file : playerDataFiles) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                UUID uuid = UUID.fromString(file.getName().split("\\.yml")[0]);
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                playerFiles.put(uuid, file);
                playerConfigs.put(uuid, config);
                offlinePlayerCache.put(uuid, player);
                lastPlayedCache.put(uuid, player.getLastPlayed());
                balances.put(uuid, config.getDouble("balance"));
                bitcoinsMined.put(uuid, config.getDouble("bitcoins_mined"));
                puzzlesSolved.put(uuid, config.getLong("puzzles_solved"));
                puzzleTimes.put(uuid, config.getLong("best_puzzle_time"));
                displayNames.put(uuid, config.getString("display_name"));
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!playerFiles.containsKey(uuid)) {
            File file = new File(plugin.getDataFolder() + File.separator + "Player Data", uuid.toString() + ".yml");
            playerFiles.put(uuid, file);
            playerConfigs.put(uuid, YamlConfiguration.loadConfiguration(file));
        }
        YamlConfiguration playerConfig = playerConfigs.get(uuid);
        if (!playerConfig.contains("balance")) { setBalance(uuid, 0); }
        if (!playerConfig.contains("puzzles_solved")) { setPuzzlesSolved(uuid, 0); }
        if (!playerConfig.contains("bitcoins_mined")) { setBitcoinsMined(uuid, 0); }
        if (!playerConfig.contains("best_puzzle_time")) { setBestPuzzleTime(uuid, 0); }
        if (!playerConfig.contains("display_name")) { setDisplayName(uuid, event.getPlayer().getDisplayName()); }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        setDisplayName(uuid, event.getPlayer().getDisplayName());
        lastPlayedCache.put(uuid, System.currentTimeMillis());
    }

    public void saveData(UUID uuid, String dataKey, Object data) {
        YamlConfiguration config = playerConfigs.get(uuid);
        config.set(dataKey, data);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Util.saveYml(playerFiles.get(uuid), config));
    }

    public void setBitcoinsMined(UUID uuid, double amount) {
        bitcoinsMined.put(uuid, amount);
        saveData(uuid, "bitcoins_mined", amount);
    }

    public void setPuzzlesSolved(UUID uuid, long amount) {
        puzzlesSolved.put(uuid, amount);
        saveData(uuid, "puzzles_solved", amount);
    }

    public void setBestPuzzleTime(UUID uuid, long time) {
        puzzleTimes.put(uuid, time);
        saveData(uuid, "best_puzzle_time", time);
    }

    public void setDisplayName(UUID uuid, String name) {
        displayNames.put(uuid, name);
        saveData(uuid, "display_name", name);
    }

    public void setBalance(UUID uuid, double balance) {
        balances.put(uuid, balance);
        saveData(uuid, "balance", balance);
    }

    public void withdraw(UUID uuid, double amount) {
        setBalance(uuid, balances.get(uuid) - amount);
    }

    public void deposit(UUID uuid, double amount) {
        setBalance(uuid, balances.get(uuid) + amount);
    }

    public void resetBalances() {
        balances.keySet().forEach(uuid -> setBalance(uuid, 0));
    }

    public void resetMined() {
        bitcoinsMined.keySet().forEach(uuid -> setBitcoinsMined(uuid, 0));
    }

    public void resetSolved() {
        puzzlesSolved.keySet().forEach(uuid -> setPuzzlesSolved(uuid, 0));
    }

    public void resetTimes() {
        puzzleTimes.keySet().forEach(uuid -> setBestPuzzleTime(uuid, 0L));
    }

    public long getBuyLimit(Player player) {
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String permString = permission.getPermission();
            if (permString.matches("bitcoin\\.buy\\.limit\\.\\d+")) {
                return Long.parseLong(permString.split("bitcoin\\.buy\\.limit\\.")[1]);
            }
        }
        return -1;
    }

    public long getBuyDelay(Player player) {
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String permString = permission.getPermission();
            if (permString.matches("bitcoin\\.buy\\.delay\\.\\d+")) {
                return Long.parseLong(permString.split("bitcoin\\.buy\\.delay\\.")[1]);
            }
        }
        return -1;
    }

    public List<UUID> getTopBalPlayers() {
        return balances.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<UUID> getTopSolvedPlayers() {
        return puzzlesSolved.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<UUID> getTopTimePlayers() {
        return puzzleTimes.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public String getDisplayName(UUID uuid) {
        return displayNames.getOrDefault(uuid, getOfflinePlayer(uuid).getName());
    }

    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        if (offlinePlayerCache.containsKey(uuid)) {
            return offlinePlayerCache.get(uuid);
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        offlinePlayerCache.put(uuid, player);
        return player;
    }

    public long getLastPlayed(UUID uuid) {
        if (lastPlayedCache.containsKey(uuid)) {
            return lastPlayedCache.get(uuid);
        }
        long lastPlayed = getOfflinePlayer(uuid).getLastPlayed();
        lastPlayedCache.put(uuid, lastPlayed);
        return lastPlayed;
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }

    public double getBitcoinsMined(UUID uuid) {
        return bitcoinsMined.getOrDefault(uuid, 0.0);
    }

    public long getPuzzlesSolved(UUID uuid) {
        return puzzlesSolved.getOrDefault(uuid, 0L);
    }

    public long getBestPuzzleTime(UUID uuid) {
        return puzzleTimes.getOrDefault(uuid, 0L);
    }

    public Map<UUID, Double> getBalances() {
        return balances;
    }
}
