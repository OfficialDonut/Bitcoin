package us.donut.bitcoin.mining;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import us.donut.bitcoin.Bitcoin;
import us.donut.bitcoin.BitcoinManager;
import us.donut.bitcoin.PlayerDataManager;
import us.donut.bitcoin.Util;
import us.donut.bitcoin.config.BitcoinConfig;
import us.donut.bitcoin.config.Messages;
import us.donut.bitcoin.config.Sounds;

import java.util.*;

public class MiningManager implements Listener {

    private static MiningManager instance;
    private Bitcoin plugin;
    private BitcoinManager bitcoinManager;
    private PlayerDataManager playerDataManager;
    private Random random;
    private Map<Player, Inventory> interfaces = new HashMap<>();
    private Map<Player, Long> puzzleTimes = new HashMap<>();
    private GridType gridType;
    private ItemStack resetButton;
    private ItemStack solveButton;
    private ItemStack exitButton;
    private ItemStack plainGlassPane;
    private BukkitTask newPuzzleTask;
    private long newPuzzleDelay;
    private double minReward;
    private double maxReward;

    private MiningManager() {
        plugin = Bitcoin.getInstance();
        bitcoinManager = BitcoinManager.getInstance();
        playerDataManager = PlayerDataManager.getInstance();
        random = new Random();
        startPuzzleTimers();
    }

    public static MiningManager getInstance() {
        return instance != null ? instance : (instance = new MiningManager());
    }

    public void reload() {
        minReward = BitcoinConfig.getDouble("min_mining_reward");
        maxReward = BitcoinConfig.getDouble("max_mining_reward");
        newPuzzleDelay = BitcoinConfig.getLong("new_mining_puzzle_delay");
        gridType = BitcoinConfig.getString("puzzle_difficulty").equalsIgnoreCase("hard") ? GridType.HARD : GridType.EASY;

        resetButton = Util.createItemStack(Material.TNT, Messages.RESET_ITEM_NAME.toString(), Messages.RESET_ITEM_LORE.toString());
        solveButton = Util.createItemStack(Material.SLIME_BALL, Messages.SOLVE_ITEM_NAME.toString(), Messages.SOLVE_ITEM_LORE.toString());
        exitButton = Util.createItemStack(Material.BARRIER, Messages.EXIT_ITEM_NAME.toString(), Messages.EXIT_ITEM_LORE.toString());
        plainGlassPane = Util.createItemStack(Material.GLASS_PANE, " ", null);

        Util.closeInventories(interfaces);
        interfaces.clear();
        puzzleTimes.clear();
        gridType.generate();
    }

    boolean isMiningInterface(Inventory inventory) {
        return interfaces.containsValue(inventory);
    }

    boolean isMiningInterface(InventoryView inventoryView) {
        return isMiningInterface(inventoryView.getTopInventory());
    }

    private void startPuzzleTimers() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isMiningInterface(player.getOpenInventory())) {
                    puzzleTimes.put(player, puzzleTimes.getOrDefault(player, 0L) + 1);
                }
            }
        }, 0, 20);
    }

    public void openInterface(Player player) {
        if (newPuzzleTask == null || newPuzzleTask.isCancelled()) {
            player.openInventory(interfaces.computeIfAbsent(player, this::createInterface));
        } else {
            player.sendMessage(Messages.GENERATING_PUZZLE.toString());
        }
    }

    private Inventory createInterface(Player player) {
        Inventory miningInterface = Bukkit.createInventory(null, 54, Messages.MINING_MENU_TITLE.toString());
        miningInterface.setItem(48, resetButton);
        miningInterface.setItem(49, solveButton);
        miningInterface.setItem(50, exitButton);
        gridType.initGrid(miningInterface);
        for (int slot : gridType.getPlainGlassSlots()) {
            miningInterface.setItem(slot, plainGlassPane);
        }
        return miningInterface;
    }

    private void moveTile(Inventory inventory, int slot) {
        for (int i : new int[]{1, -1, 9, -9}) {
            int newSlot = slot + i;
            if (gridType.isGridSlot(newSlot) && inventory.getItem(newSlot) == null) {
                if ((slot == 0 || newSlot == 0) || ((slot % 8 != 0 || newSlot % 9 != 0) && (slot % 9 != 0 || newSlot % 8 != 0))) {
                    inventory.setItem(newSlot, inventory.getItem(slot));
                    inventory.setItem(slot, null);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        interfaces.remove(event.getPlayer());
        puzzleTimes.remove(event.getPlayer());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isMiningInterface(event.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (isMiningInterface(event.getDestination()) || isMiningInterface(event.getSource())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (isMiningInterface(event.getInventory())) {
            event.setCancelled(true);
        }
        Inventory inventory = event.getClickedInventory();
        if (inventory != null && isMiningInterface(inventory) && event.getWhoClicked() instanceof Player) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();
            if (resetButton.equals(item)) {
                gridType.initGrid(inventory);
                player.playSound(player.getLocation(), Sounds.get("reset_tiles"), 1, 1);
            } else if (solveButton.equals(item)) {
                if (!gridType.isSolved(inventory)) {
                    player.playSound(player.getLocation(), Sounds.get("click_solve_when_not_solved"), 1, 1);
                    return;
                }
                double reward = Util.round(2, minReward + (maxReward - minReward) * random.nextDouble());
                double limit = bitcoinManager.getCirculationLimit();
                double circ = bitcoinManager.getBitcoinsInCirculation();
                if (circ < limit) {
                    while (limit > 0 && reward + circ > limit) {
                        reward /= 2;
                    }
                } else if (limit > 0) {
                    reward = 0;
                }
                playerDataManager.deposit(player.getUniqueId(), reward);
                playerDataManager.setPuzzlesSolved(player.getUniqueId(), playerDataManager.getPuzzlesSolved(player.getUniqueId()) + 1);
                playerDataManager.setBitcoinsMined(player.getUniqueId(), playerDataManager.getBitcoinsMined(player.getUniqueId()) + reward);
                long time = puzzleTimes.get(player);
                if (time < playerDataManager.getBestPuzzleTime(player.getUniqueId()) || playerDataManager.getBestPuzzleTime(player.getUniqueId()) == 0L) {
                    playerDataManager.setBestPuzzleTime(player.getUniqueId(), time);
                }
                player.sendMessage(Messages.get("reward", reward));
                String message = Messages.get("solved", player.getDisplayName(), reward, String.valueOf(time / 60.0).split("\\.")[0], time % 60);
                ComputerManager.getInstance().potentialUse(player);
                Bukkit.getConsoleSender().sendMessage(message);
                Bukkit.getConsoleSender().sendMessage(Messages.GENERATING_PUZZLE.toString());
                for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
                    loopPlayer.sendMessage(message);
                    loopPlayer.sendMessage(Messages.GENERATING_PUZZLE.toString());
                    loopPlayer.playSound(loopPlayer.getLocation(), Sounds.get("puzzle_solved"), 1, 1);
                }
                Util.closeInventories(interfaces);
                interfaces.clear();
                puzzleTimes.clear();
                newPuzzleTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    gridType.generate();
                    Bukkit.getConsoleSender().sendMessage(Messages.GENERATED_PUZZLE.toString());
                    for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
                        loopPlayer.sendMessage(Messages.GENERATED_PUZZLE.toString());
                    }
                    newPuzzleTask.cancel();
                }, newPuzzleDelay);
            } else if (exitButton.equals(item)) {
                Bukkit.getScheduler().runTask(plugin, player::closeInventory);
                player.playSound(player.getLocation(), Sounds.get("exit_mining"), 1, 1);
            } else if (gridType.isGridSlot(event.getSlot())) {
                moveTile(event.getClickedInventory(), event.getSlot());
            }
        }
    }
}
