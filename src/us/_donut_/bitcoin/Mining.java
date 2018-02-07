package us._donut_.bitcoin;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

class Mining implements Listener {

    private Bitcoin plugin;
    private Util util;
    private Messages messages;
    private Sounds sounds;
    private BitcoinManager bitcoinManager;
    private ItemStack resetButton;
    private ItemStack solveButton;
    private ItemStack exitButton;
    private ItemStack plainGlassPane;
    private List<ItemStack> coloredGlass = new ArrayList<>();
    private Integer[] moveableSlots = {0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29};
    private Integer[] immovableSlots = {5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34};
    private Map<Player, Inventory> miningInterfaces = new HashMap<>();
    private Map<Integer, Short> puzzleAnswer = new HashMap<>();
    private Map<Integer, Short> initialArrangement = new HashMap<>();
    private double minReward;
    private double maxReward;
    private double reward;
    private long newPuzzleDelay;

    Mining(Bitcoin pluginInstance) {
        plugin = pluginInstance;
        util = plugin.getUtil();
        bitcoinManager = plugin.getBitcoinManager();
        messages = plugin.getMessages();
        sounds = plugin.getSounds();

        reload();
        generateNewPuzzle();
    }

    void reload() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTitle().equalsIgnoreCase(messages.getMessage("mining_menu_title"))) { player.closeInventory(); }
        }
        miningInterfaces.clear();
        coloredGlass.clear();
        minReward = plugin.getBitcoinConfig().getDouble("min_mining_reward");
        maxReward = plugin.getBitcoinConfig().getDouble("max_mining_reward");
        newPuzzleDelay = plugin.getBitcoinConfig().getLong("new_mining_puzzle_delay");
        resetButton = util.createItemStack(Material.TNT, (short) 0, messages.getMessage("reset_item_name"), messages.getMessage("reset_item_lore"));
        solveButton = util.createItemStack(Material.SLIME_BALL, (short) 0, messages.getMessage("solve_item_name"), messages.getMessage("solve_item_lore"));
        exitButton = util.createItemStack(Material.BARRIER, (short) 0, messages.getMessage("exit_item_name"), messages.getMessage("exit_item_lore"));
        plainGlassPane = util.createItemStack(Material.THIN_GLASS, (short) 0, " ", null);
        String[] glassNames = {messages.getMessage("white_tile"), messages.getMessage("orange_tile"), messages.getMessage("magenta_tile"), messages.getMessage("light_blue_tile"), messages.getMessage("yellow_tile"),
                messages.getMessage("lime_tile"), messages.getMessage("pink_tile"), messages.getMessage("gray_tile"), messages.getMessage("light_gray_tile"), messages.getMessage("cyan_tile"), messages.getMessage("purple_tile"),
                messages.getMessage("blue_tile"), messages.getMessage("brown_tile"), messages.getMessage("green_tile"), messages.getMessage("red_tile"), messages.getMessage("black_tile")};
        for (short i = 0; i < 16; i++) { coloredGlass.add(util.createItemStack(Material.STAINED_GLASS_PANE, i, glassNames[i], null)); }
    }

    void openInterface(Player player) {
        if (!initialArrangement.isEmpty()) {
            if (!miningInterfaces.containsKey(player)) { createInterface(player); }
            player.openInventory(miningInterfaces.get(player));
        } else {
            player.sendMessage(messages.getMessage("generating_puzzle"));
        }
    }

    private void generateNewPuzzle() {
        List<Integer> glassColors = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15));
        for (int i : moveableSlots) {
            int color = glassColors.get(new Random().nextInt(glassColors.size()));
            puzzleAnswer.put(i, (short) color);
            glassColors.remove(glassColors.indexOf(color));
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                List<Integer> randomizedSlots = getRandomArrangement();
                if (randomizedSlots != null) {
                    int i = 0;
                    for (int slot : moveableSlots) { initialArrangement.put(slot, puzzleAnswer.get(randomizedSlots.get(i))); i++; }
                    for (Player player : Bukkit.getOnlinePlayers()) { player.sendMessage(messages.getMessage("generated_puzzle")); }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private List<Integer> getRandomArrangement() {
        List<Integer> slots = new ArrayList<>(Arrays.asList(moveableSlots));
        List<Integer> randomizedSlots = new ArrayList<>();
        for (int i = 1; i < 16; i++) {
            int slot = slots.get(new Random().nextInt(slots.size()));
            randomizedSlots.add(slot);
            slots.remove(slots.indexOf(slot));
        }

        int inversions = 0;
        for (int i : randomizedSlots) {
            for (int i2 : randomizedSlots) {
                if (randomizedSlots.indexOf(i) < randomizedSlots.indexOf(i2) && i > i2) { inversions++; }
            }
        }
        if ((inversions & 1) == 0) {
            return randomizedSlots;
        } else {
            return null;
        }
    }

    private void createInterface(Player player) {
        Inventory miningInterface = Bukkit.createInventory(null, 54, messages.getMessage("mining_menu_title"));
        miningInterface.setItem(48, resetButton);
        miningInterface.setItem(49, solveButton);
        miningInterface.setItem(50, exitButton);
        miningInterface.setItem(30, null);
        for (int slot : new int[]{4, 13, 22, 31, 40, 39, 38, 37, 36, 41, 42, 43, 44}) { miningInterface.setItem(slot, plainGlassPane); }
        for (int slot : immovableSlots) { miningInterface.setItem(slot, coloredGlass.get(puzzleAnswer.get(slot - 5))); }
        for (int slot : moveableSlots) { miningInterface.setItem(slot, coloredGlass.get(initialArrangement.get(slot))); }
        miningInterfaces.put(player, miningInterface);
    }

    private Boolean puzzleIsSolved(Inventory miningInterface) {
        for (int slot : moveableSlots) {
            if (miningInterface.getItem(slot) == null || miningInterface.getItem(slot).getDurability() != puzzleAnswer.get(slot)) {
                return false;
            }
        }
        return true;
    }

    private void moveTile(Inventory miningInterface, int slot) {
        Integer newSlot;
        if (Arrays.asList(moveableSlots).contains(slot - 1) && miningInterface.getItem(slot - 1) == null) {
            newSlot = slot - 1;
        } else if (Arrays.asList(moveableSlots).contains(slot + 1) && miningInterface.getItem(slot + 1) == null) {
            newSlot =  slot + 1;
        } else if (Arrays.asList(moveableSlots).contains(slot - 9) && miningInterface.getItem(slot - 9) == null) {
            newSlot = slot - 9;
        } else if (Arrays.asList(moveableSlots).contains(slot + 9) && miningInterface.getItem(slot + 9) == null) {
            newSlot = slot + 9;
        } else if (slot + 1 == 30 && miningInterface.getItem(30) == null) {
            newSlot = 30;
        } else if (slot + 9 == 30 && miningInterface.getItem(30) == null) {
            newSlot = 30;
        } else {
            newSlot = null;
        }
        if (newSlot != null) {
            miningInterface.setItem(newSlot, miningInterface.getItem(slot));
            miningInterface.setItem(slot, null);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onQuit(PlayerQuitEvent event) {
        if (miningInterfaces.containsKey(event.getPlayer())) {
            miningInterfaces.remove(event.getPlayer());
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onDragInGUI(InventoryDragEvent event) {
        if (event.getInventory().getName() != null && event.getInventory().getName().equalsIgnoreCase(messages.getMessage("mining_menu_title"))) { event.setCancelled(true); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onMoveInGUI(InventoryMoveItemEvent event) {
        if (event.getDestination().getName() != null && event.getDestination().getName().equalsIgnoreCase(messages.getMessage("mining_menu_title"))) { event.setCancelled(true); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase(messages.getMessage("mining_menu_title"))) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getSlot() == 48) {
                player.playSound(player.getLocation(), sounds.getSound("reset_tiles"), 1, 1);
                for (int slot : moveableSlots) { event.getInventory().setItem(slot, coloredGlass.get(initialArrangement.get(slot))); }
                event.getInventory().setItem(30, null);
            } else if (event.getSlot() == 49) {
                if (puzzleIsSolved(event.getInventory())) {
                    reward = util.round(2, minReward + (maxReward - minReward) * new Random().nextDouble());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (bitcoinManager.getBitcoinsInCirculation() >= bitcoinManager.getCirculationLimit()) { reward = 0; cancel(); }
                            if (bitcoinManager.getCirculationLimit() > 0 && bitcoinManager.getBitcoinsInCirculation() + reward >= bitcoinManager.getCirculationLimit()) {
                                reward = reward / 2.0;
                            } else {
                                cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 0, 1);
                    bitcoinManager.deposit(player.getUniqueId(), reward);
                    bitcoinManager.setPuzzlesSolved(player.getUniqueId(), bitcoinManager.getPuzzlesSolved(player.getUniqueId()) + 1);
                    bitcoinManager.setBitcoinsMined(player.getUniqueId(), bitcoinManager.getBitcoinsMined(player.getUniqueId()) + reward);
                    player.sendMessage(messages.getMessage("reward").replace("{REWARD}", String.valueOf(reward)));
                    for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
                        if (loopPlayer.getOpenInventory().getTitle().equalsIgnoreCase(messages.getMessage("mining_menu_title"))) { loopPlayer.closeInventory(); }
                        loopPlayer.sendMessage(messages.getMessage("solved").replace("{SOLVER}", player.getName()).replace("{REWARD}", String.valueOf(reward)));
                        loopPlayer.sendMessage(messages.getMessage("generating_puzzle"));
                        loopPlayer.playSound(loopPlayer.getLocation(), sounds.getSound("puzzle_solved"), 1, 1);
                    }
                    initialArrangement.clear();
                    puzzleAnswer.clear();
                    miningInterfaces.clear();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            generateNewPuzzle();
                        }
                    }.runTaskLater(plugin, newPuzzleDelay);
                } else {
                    player.playSound(player.getLocation(), sounds.getSound("click_solve_when_not_solved"), 1, 1);
                }
            } else if (event.getSlot() == 50) {
                player.playSound(player.getLocation(), sounds.getSound("exit_mining"), 1, 1);
                player.closeInventory();
            } else if (Arrays.asList(moveableSlots).contains(event.getSlot()) || event.getSlot() == 30) {
                moveTile(event.getInventory(), event.getSlot());
            }
        }
    }
}