package us.donut.bitcoin;

import org.bukkit.entity.Player;
import us.donut.bitcoin.mining.MiningManager;

import java.util.List;
import java.util.UUID;

public class BitcoinAPI {

    private static BitcoinManager bitcoinManager;
    private static PlayerDataManager playerDataManager;
    private static MiningManager miningManager;
    private static BitcoinMenu bitcoinMenu;

    static void init() {
        bitcoinManager = BitcoinManager.getInstance();
        playerDataManager = PlayerDataManager.getInstance();
        miningManager = MiningManager.getInstance();
        bitcoinMenu = BitcoinMenu.getInstance();
    }

    public static List<UUID> getTopPlayers() { return playerDataManager.getTopBalPlayers(); }
    public static double getCirculationLimit() { return bitcoinManager.getCirculationLimit(); }
    public static double getAmountInCirculation() { return bitcoinManager.getBitcoinsInCirculation(); }
    public static double getAmountInBank() { return bitcoinManager.getAmountInBank(); }
    public static double getPurchaseTaxPercentage() { return bitcoinManager.getPurchaseTaxPercentage(); }
    public static double getBitcoinValue() { return bitcoinManager.getBitcoinValue(); }
    public static double getBalance(UUID playerUUID) { return playerDataManager.getBalance(playerUUID); }
    public static double getBitcoinsMined(UUID playerUUID) { return playerDataManager.getBitcoinsMined(playerUUID); }
    public static long getPuzzlesSolved(UUID playerUUID) { return playerDataManager.getPuzzlesSolved(playerUUID); }
    public static long getBestPuzzleTime(UUID playerUUID) { return playerDataManager.getBestPuzzleTime(playerUUID); }
    public static void setBalance(UUID playerUUID, double balance) { playerDataManager.setBalance(playerUUID, balance); }
    public static void withdraw(UUID playerUUID, double amount) { playerDataManager.withdraw(playerUUID, amount); }
    public static void deposit(UUID playerUUID, double amount) { playerDataManager.deposit(playerUUID, amount); }
    public static void setPuzzlesSolved(UUID playerUUID, int amount) { playerDataManager.setPuzzlesSolved(playerUUID, amount); }
    public static void setBitcoinsMined(UUID playerUUID, double amount) { playerDataManager.setBitcoinsMined(playerUUID, amount); }
    public static void addToBank(double amount) { bitcoinManager.addToBank(amount); }
    public static void removeFromBank(double amount) { bitcoinManager.removeFromBank(amount); }
    public static void makeValueFluctuate() { bitcoinManager.fluctuate(); }
    public static void openMainMenu(Player player) { bitcoinMenu.open(player); }
    public static void openMiningInterface(Player player) { miningManager.openInterface(player); }
}
