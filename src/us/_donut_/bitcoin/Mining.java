package us._donut_.bitcoin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.scheduler.BukkitTask;
import us._donut_.bitcoin.configuration.Message;
import us._donut_.bitcoin.configuration.Sounds;

import java.util.*;

import static us._donut_.bitcoin.util.Util.*;

class Mining implements Listener {

    private Bitcoin plugin = Bitcoin.plugin;
    private Sounds sounds;
    private BitcoinManager bitcoinManager;
    private ItemStack resetButton;
    private ItemStack solveButton;
    private ItemStack exitButton;
    private ItemStack plainGlassPane;
    private List<ItemStack> coloredGlass = new ArrayList<>();
    private List<ItemStack> numberedGlass = new ArrayList<>();
    private Integer[] moveableSlots = {0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29};
    private Integer[] immovableSlots = {5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34};
    private Map<Player, Inventory> miningInterfaces = new HashMap<>();
    private Map<Integer, Short> puzzleAnswer = new HashMap<>();
    private Map<Integer, Short> initialArrangement = new HashMap<>();
    private Map<Integer, ItemStack> hardInitialArrangement = new HashMap<>();
    private Map<Player, Long> timers = new HashMap<>();
    private double minReward;
    private double maxReward;
    private double reward;
    private long newPuzzleDelay;
    private String puzzleDifficulty;
    private BukkitTask puzzleGenerator;
    private BukkitTask rewardTask;
    private Random random = new Random();

    Mining() {
        bitcoinManager = plugin.getBitcoinManager();
        sounds = plugin.getSounds();
        reload();
        generateNewPuzzle();
        startTimers();
    }

    void reload() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTitle().equalsIgnoreCase(Message.MINING_MENU_TITLE.toString())) { player.closeInventory(); }
        }
        miningInterfaces.clear();
        coloredGlass.clear();
        puzzleDifficulty = plugin.getBitcoinConfig().getString("puzzle_difficulty");
        if (!puzzleDifficulty.equalsIgnoreCase("easy") && !puzzleDifficulty.equalsIgnoreCase("hard")) {
            puzzleDifficulty = "easy";
        }
        minReward = plugin.getBitcoinConfig().getDouble("min_mining_reward");
        maxReward = plugin.getBitcoinConfig().getDouble("max_mining_reward");
        newPuzzleDelay = plugin.getBitcoinConfig().getLong("new_mining_puzzle_delay");
        resetButton = createItemStack(Material.TNT, (short) 0, Message.RESET_ITEM_NAME.toString(), Message.RESET_ITEM_LORE.toString());
        solveButton = createItemStack(Material.SLIME_BALL, (short) 0, Message.SOLVE_ITEM_NAME.toString(), Message.SOLVE_ITEM_LORE.toString());
        exitButton = createItemStack(Material.BARRIER, (short) 0, Message.EXIT_ITEM_NAME.toString(), Message.EXIT_ITEM_LORE.toString());
        String[] glassNames = {Message.WHITE_TILE.toString(), Message.ORANGE_TILE.toString(), Message.MAGENTA_TILE.toString(), Message.LIGHT_BLUE_TILE.toString(), Message.YELLOW_TILE.toString(),
                Message.LIME_TILE.toString(), Message.PINK_TILE.toString(), Message.GRAY_TILE.toString(),Message.LIGHT_GRAY_TILE.toString(), Message.CYAN_TILE.toString(), Message.PURPLE_TILE.toString(),
                Message.BLUE_TILE.toString(), Message.BROWN_TILE.toString(), Message.GREEN_TILE.toString(), Message.RED_TILE.toString(), Message.BLACK_TILE.toString()};
        if (!Bukkit.getVersion().contains("1.13")) {
            plainGlassPane = createItemStack(Material.THIN_GLASS, (short) 0, " ", null);
            for (short i = 0; i < 16; i++) { coloredGlass.add(createItemStack(Material.STAINED_GLASS_PANE, i, glassNames[i], null)); }
            for (int i = 0; i < 44; i++) { numberedGlass.add(createItemStackWithAmount(Material.STAINED_GLASS_PANE, i + 1, i % 2 == 0 ? (short) 11 : (short) 14, ChatColor.translateAlternateColorCodes('&', "&9&l") + (i + 1), null)); }
        } else {
            plainGlassPane = createItemStack(Material.valueOf("GLASS_PANE"), (short) 0, " ", null);
            for (short i = 0; i < 16; i++) { coloredGlass.add(createItemStack(getGlass(i), (short) 0, glassNames[i], null)); }
            for (int i = 0; i < 44; i++) { numberedGlass.add(createItemStackWithAmount(getGlass(i % 2 == 0 ? 11 : 14), i + 1, (short) 0, ChatColor.translateAlternateColorCodes('&', "&9&l") + (i + 1), null)); }
        }
    }

    void openInterface(Player player) {
        if (!initialArrangement.isEmpty() || !hardInitialArrangement.isEmpty()) {
            if (!miningInterfaces.containsKey(player)) { createInterface(player); }
            player.openInventory(miningInterfaces.get(player));
        } else {
            player.sendMessage(Message.GENERATING_PUZZLE.toString());
        }
    }

    private void generateNewPuzzle() {
        if (puzzleDifficulty.equalsIgnoreCase("easy")) {
            moveableSlots = new Integer[]{0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29};
            List<Integer> glassColors = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15));
            Arrays.stream(moveableSlots).forEach(slot -> {
                int color = glassColors.get(random.nextInt(glassColors.size()));
                puzzleAnswer.put(slot, (short) color);
                glassColors.remove(glassColors.indexOf(color));
            });
            puzzleGenerator = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                List<Integer> randomizedSlots = getRandomArrangement();
                if (randomizedSlots != null) {
                    int i = 0;
                    for (int slot : moveableSlots) { initialArrangement.put(slot, puzzleAnswer.get(randomizedSlots.get(i))); i++; }
                    Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(Message.GENERATED_PUZZLE.toString()));
                    puzzleGenerator.cancel();
                }
            },0, 1);
        } else {
            moveableSlots = new Integer[44];
            for (int i = 0; i < 44; i++) {
                moveableSlots[i] = i;
            }
            puzzleGenerator = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                List<ItemStack> randomizedSlots = getRandomHardArrangement();
                if (randomizedSlots != null && arrangementHasNoDuplicates(randomizedSlots)) {
                    for (int slot = 0; slot < randomizedSlots.size(); slot++) { hardInitialArrangement.put(slot, randomizedSlots.get(slot)); }
                    Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(Message.GENERATED_PUZZLE.toString()));
                    puzzleGenerator.cancel();
                }
            },0, 1);
        }
    }

    private List<Integer> getRandomArrangement() {
        List<Integer> slots = new ArrayList<>(Arrays.asList(moveableSlots));
        List<Integer> randomizedSlots = new ArrayList<>();
        for (int i = 1; i < 16; i++) {
            int slot = slots.get(random.nextInt(slots.size()));
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

    private List<ItemStack> getRandomHardArrangement() {
        List<ItemStack> numberedGlassCopy = new ArrayList<>(numberedGlass);
        List<ItemStack> randomizedSlots = new ArrayList<>();

        Arrays.stream(moveableSlots).forEach(slot -> {
            ItemStack glass = numberedGlassCopy.get(random.nextInt(numberedGlassCopy.size()));
            numberedGlassCopy.remove(glass);
            randomizedSlots.add(glass);
        });

        int inversions = 0;
        for (ItemStack i : randomizedSlots) {
            for (ItemStack i2 : randomizedSlots) {
                if (i.getAmount() < i2.getAmount() && randomizedSlots.indexOf(i) > randomizedSlots.indexOf(i2)) {
                    inversions++;
                }
            }
        }
        if ((inversions & 1) == 0) {
            return randomizedSlots;
        } else {
            return null;
        }
    }

    private boolean arrangementHasNoDuplicates(List<ItemStack> arrangement) {
        List<Integer> temp = new ArrayList<>();
        for (ItemStack itemStack : arrangement) {
            if (temp.contains(itemStack.getAmount())) {
                for (int i = 0; i < 44; i++) {
                    numberedGlass.clear();
                    if (!Bukkit.getVersion().contains("1.13")) {
                        numberedGlass.add(createItemStackWithAmount(Material.STAINED_GLASS_PANE, i + 1, i % 2 == 0 ? (short) 11 : (short) 14, ChatColor.translateAlternateColorCodes('&', "&9&l") + (i + 1), null));
                    } else {
                        numberedGlass.add(createItemStackWithAmount(i % 2 == 0 ? Material.valueOf("BLUE_STAINED_GLASS_PANE") : Material.valueOf("BLUE_STAINED_GLASS_PANE"), i + 1, (short) 0, ChatColor.translateAlternateColorCodes('&', "&9&l") + (i + 1), null));
                    }
                }
                return false;
            }
            temp.add(itemStack.getAmount());
        }
        return true;
    }

    private void createInterface(Player player) {
        Inventory miningInterface = Bukkit.createInventory(null, 54, Message.MINING_MENU_TITLE.toString());
        miningInterface.setItem(48, resetButton);
        miningInterface.setItem(49, solveButton);
        miningInterface.setItem(50, exitButton);
        if (puzzleDifficulty.equalsIgnoreCase("easy")) {
            miningInterface.setItem(30, null);
            Arrays.stream(new int[]{4, 13, 22, 31, 40, 39, 38, 37, 36, 41, 42, 43, 44}).forEach(slot -> miningInterface.setItem(slot, plainGlassPane));
            Arrays.stream(immovableSlots).forEach(slot -> miningInterface.setItem(slot, coloredGlass.get(puzzleAnswer.get(slot - 5))));
            Arrays.stream(moveableSlots).forEach(slot -> miningInterface.setItem(slot, coloredGlass.get(initialArrangement.get(slot))));
        } else {
            miningInterface.setItem(45, null);
            Arrays.stream(new int[]{45, 46, 47, 51, 52, 53}).forEach(slot -> miningInterface.setItem(slot, plainGlassPane));
            Arrays.stream(moveableSlots).forEach(slot -> miningInterface.setItem(slot, hardInitialArrangement.get(slot)));
        }
        miningInterfaces.put(player, miningInterface);
    }

    private Boolean puzzleIsSolved(Inventory miningInterface) {
        if (puzzleDifficulty.equalsIgnoreCase("easy")) {
            for (int slot : moveableSlots) {
                if (!Bukkit.getVersion().contains("1.13")) {
                    if (miningInterface.getItem(slot) == null || miningInterface.getItem(slot).getDurability() != puzzleAnswer.get(slot)) {
                        return false;
                    }
                } else {
                    if (miningInterface.getItem(slot) == null || miningInterface.getItem(slot).getType() != getGlass(puzzleAnswer.get(slot))) {
                        return false;
                    }
                }
            }
        } else {
            for (int slot : moveableSlots) {
                if (miningInterface.getItem(slot) == null || miningInterface.getItem(slot).getAmount() - 1 != slot) {
                    return false;
                }
            }
        }
        return true;
    }

    private void moveTile(Inventory miningInterface, int slot) {
        Integer newSlot;
        if (puzzleDifficulty.equalsIgnoreCase("easy")) {
            if (Arrays.asList(moveableSlots).contains(slot - 1) && miningInterface.getItem(slot - 1) == null) {
                newSlot = slot - 1;
            } else if (Arrays.asList(moveableSlots).contains(slot + 1) && miningInterface.getItem(slot + 1) == null) {
                newSlot = slot + 1;
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
        } else {
            if (Arrays.asList(moveableSlots).contains(slot - 1)  && (slot != 9 && slot != 18 && slot != 27 && slot != 36) && miningInterface.getItem(slot - 1) == null) {
                newSlot = slot - 1;
            } else if (Arrays.asList(moveableSlots).contains(slot + 1) && (slot != 8 && slot != 17 && slot != 26 && slot != 35) && miningInterface.getItem(slot + 1) == null) {
                newSlot = slot + 1;
            } else if (Arrays.asList(moveableSlots).contains(slot - 9) && miningInterface.getItem(slot - 9) == null) {
                newSlot = slot - 9;
            } else if (Arrays.asList(moveableSlots).contains(slot + 9) && miningInterface.getItem(slot + 9) == null) {
                newSlot = slot + 9;
            } else if (slot + 1 == 44 && miningInterface.getItem(44) == null) {
                newSlot = 44;
            } else if (slot + 9 == 44 && miningInterface.getItem(44) == null) {
                newSlot = 44;
            } else {
                newSlot = null;
            }
        }
        if (newSlot != null) {
            miningInterface.setItem(newSlot, miningInterface.getItem(slot));
            miningInterface.setItem(slot, null);
        }
    }

    private void startTimers() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!initialArrangement.isEmpty() || !hardInitialArrangement.isEmpty()) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getOpenInventory() != null && player.getOpenInventory().getTitle() != null && player.getOpenInventory().getTitle().equalsIgnoreCase(Message.MINING_MENU_TITLE.toString())) {
                        if (timers.containsKey(player)) {
                            timers.put(player, timers.get(player) + 1L);
                        } else {
                            timers.put(player, 1L);
                        }
                    }
                });
            }
        }, 0, 20);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onQuit(PlayerQuitEvent event) {
        miningInterfaces.remove(event.getPlayer());
        timers.remove(event.getPlayer());
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onDragInGUI(InventoryDragEvent event) {
        if (event.getInventory().getName() != null && event.getInventory().getName().equalsIgnoreCase(Message.MINING_MENU_TITLE.toString())) { event.setCancelled(true); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onMoveInGUI(InventoryMoveItemEvent event) {
        if (event.getDestination().getName() != null && event.getDestination().getName().equalsIgnoreCase(Message.MINING_MENU_TITLE.toString())) { event.setCancelled(true); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase(Message.MINING_MENU_TITLE.toString())) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getSlot() == 48) {
                player.playSound(player.getLocation(), sounds.getSound("reset_tiles"), 1, 1);
                if (puzzleDifficulty.equalsIgnoreCase("easy")) {
                    Arrays.stream(moveableSlots).forEach(slot -> event.getInventory().setItem(slot, coloredGlass.get(initialArrangement.get(slot))));
                    event.getInventory().setItem(30, null);
                } else {
                    Arrays.stream(moveableSlots).forEach(slot -> event.getInventory().setItem(slot, hardInitialArrangement.get(slot)));
                    event.getInventory().setItem(44, null);
                }
            } else if (event.getSlot() == 49) {
                if (puzzleIsSolved(event.getInventory())) {
                    reward = round(2, minReward + (maxReward - minReward) * random.nextDouble());
                    rewardTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                        if (bitcoinManager.getBitcoinsInCirculation() >= bitcoinManager.getCirculationLimit()) { reward = 0; rewardTask.cancel(); }
                        if (bitcoinManager.getCirculationLimit() > 0 && bitcoinManager.getBitcoinsInCirculation() + reward >= bitcoinManager.getCirculationLimit()) {
                            reward = reward / 2.0;
                        } else {
                            rewardTask.cancel();
                        }
                    },0, 1);
                    bitcoinManager.deposit(player.getUniqueId(), reward);
                    bitcoinManager.setPuzzlesSolved(player.getUniqueId(), bitcoinManager.getPuzzlesSolved(player.getUniqueId()) + 1);
                    bitcoinManager.setBitcoinsMined(player.getUniqueId(), bitcoinManager.getBitcoinsMined(player.getUniqueId()) + reward);
                    if (timers.get(player) < bitcoinManager.getBestPuzzleTime(player.getUniqueId()) || bitcoinManager.getBestPuzzleTime(player.getUniqueId()) == 0L) {
                        bitcoinManager.setBestPuzzleTime(player.getUniqueId(), timers.get(player));
                    }
                    player.sendMessage(Message.REWARD.toString().replace("{REWARD}", String.valueOf(reward)));

                    Bukkit.getOnlinePlayers().forEach(loopPlayer -> {
                        if (loopPlayer.getOpenInventory().getTitle().equalsIgnoreCase(Message.MINING_MENU_TITLE.toString())) { loopPlayer.closeInventory(); }
                        loopPlayer.sendMessage(Message.SOLVED.toString().replace("{SOLVER}", player.getDisplayName())
                                .replace("{REWARD}", String.valueOf(reward))
                                .replace("{MIN}", String.valueOf(timers.get(player) / 60.0).split("\\.")[0])
                                .replace("{SEC}", String.valueOf(timers.get(player) % 60)));
                        loopPlayer.sendMessage(Message.GENERATING_PUZZLE.toString());
                        loopPlayer.playSound(loopPlayer.getLocation(), sounds.getSound("puzzle_solved"), 1, 1);
                    });
                    timers.clear();
                    initialArrangement.clear();
                    hardInitialArrangement.clear();
                    puzzleAnswer.clear();
                    miningInterfaces.clear();
                    Bukkit.getScheduler().runTaskLater(plugin, this::generateNewPuzzle, newPuzzleDelay);
                } else {
                    player.playSound(player.getLocation(), sounds.getSound("click_solve_when_not_solved"), 1, 1);
                }
            } else if (event.getSlot() == 50) {
                player.playSound(player.getLocation(), sounds.getSound("exit_mining"), 1, 1);
                player.closeInventory();
            } else if (Arrays.asList(moveableSlots).contains(event.getSlot()) || event.getSlot() == 30 || (event.getSlot() == 44 && puzzleDifficulty.equalsIgnoreCase("hard"))) {
                moveTile(event.getInventory(), event.getSlot());
            }
        }
    }
}