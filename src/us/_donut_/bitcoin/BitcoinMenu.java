package us._donut_.bitcoin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BitcoinMenu implements Listener {

    private Bitcoin plugin;
    private Util util;
    private BitcoinManager bitcoinManager;
    private Map<Player, Inventory> menus = new HashMap<>();
    private int[] evenSlots = {0, 2, 4, 6, 8, 18, 20, 22, 24, 26};
    private int[] oddSlots = {1, 3, 5, 7, 9, 17, 19, 21, 23, 25};
    private ItemStack darkBlueGlass;
    private ItemStack lightBlueGlass;
    private ItemStack transferBitcoinItem;
    private ItemStack exchangeBitcoinItem;
    private ItemStack miningBitcoinItem;
    private List<Player> playersExchanging = new ArrayList<>();
    private List<Player> playersTransferring = new ArrayList<>();

    BitcoinMenu(Bitcoin pluginInstance) {
        plugin = pluginInstance;
        util = plugin.getUtil();
        bitcoinManager = plugin.getBitcoinManager();

        darkBlueGlass = util.createItemStack(Material.STAINED_GLASS_PANE, (short) 11, " ", null);
        lightBlueGlass = util.createItemStack(Material.STAINED_GLASS_PANE, (short) 3, " ", null);
        transferBitcoinItem = util.createItemStack(Material.BOOK_AND_QUILL, (short) 0, util.colorMessage("&9&lTransfer Bitcoins"), util.colorMessage("&3Transfer bitcoins to another account"));
        exchangeBitcoinItem = util.createItemStack(Material.GOLD_INGOT, (short) 0, util.colorMessage("&9&lExchange Bitcoins"), util.colorMessage("&3Exchange bitcoins for other currency"));
        miningBitcoinItem = util.createItemStack(Material.DIAMOND_PICKAXE, (short) 0, util.colorMessage("&9&lBitcoin Mining"), util.colorMessage("&3Solve puzzles to earn bitcoins"));
        updateGlassInMenus();
    }

    List<Player> getPlayersExchanging() { return playersExchanging; }
    List<Player> getPlayersTransferring() { return playersTransferring; }

    void open(Player player) {
        if (menus.containsKey(player)) {
            menus.get(player).setItem(10, util.getSkull(player.getUniqueId(), util.colorMessage("&9&lStatistics"), util.colorMessage("&3Balance: &b" + bitcoinManager.getBalance(player) + " bitcoins" + "~~&3Mining puzzles solved: &b" + bitcoinManager.getPuzzlesSolved(player) + "~~&3Bitcoins mined: &b" + bitcoinManager.getBitcoinsMined(player))));
        } else {
            createMenu(player);
        }
        player.openInventory(menus.get(player));
    }

    private void createMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, util.colorMessage("&9&lBitcoin Menu"));
        menu.setItem(10, util.getSkull(player.getUniqueId(), util.colorMessage("&9&lYour Statistics"), util.colorMessage("&3Balance: &b" + bitcoinManager.getBalance(player) + " bitcoins" + "~~&3Mining puzzles solved: &b" + bitcoinManager.getPuzzlesSolved(player) + "~~&3Bitcoins mined: &b" + bitcoinManager.getBitcoinsMined(player))));
        menu.setItem(12, transferBitcoinItem);
        menu.setItem(14, exchangeBitcoinItem);
        menu.setItem(16, miningBitcoinItem);
        for (int slot : evenSlots) { menu.setItem(slot, darkBlueGlass); }
        for (int slot : oddSlots) { menu.setItem(slot, lightBlueGlass); }
        menus.put(player, menu);
    }

    private void updateGlassInMenus() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Inventory menu : menus.values()) {
                    if (menu.getItem(0).getDurability() == (short) 11) {
                        for (int slot : evenSlots) { menu.setItem(slot, lightBlueGlass); }
                        for (int slot : oddSlots) { menu.setItem(slot, darkBlueGlass); }
                    } else {
                        for (int slot : evenSlots) { menu.setItem(slot, darkBlueGlass); }
                        for (int slot : oddSlots) { menu.setItem(slot, lightBlueGlass); }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 5);
    }

    private void sendCancelButton(Player player) {
        TextComponent cancelButton = new TextComponent("[Cancel]");
        cancelButton.setColor(ChatColor.RED);
        cancelButton.setBold(true);
        cancelButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bitcoin cancel"));
        cancelButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(util.colorMessage("&cClick to cancel")).create()));
        player.spigot().sendMessage(cancelButton);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onDragInGUI(InventoryDragEvent event) {
        if (event.getInventory().getName() != null && event.getInventory().getName().equalsIgnoreCase(util.colorMessage("&9&lBitcoin Menu"))) { event.setCancelled(true); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onMoveInGUI(InventoryMoveItemEvent event) {
        if (event.getDestination().getName() != null && event.getDestination().getName().equalsIgnoreCase(util.colorMessage("&9&lBitcoin Menu"))) { event.setCancelled(true); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase(util.colorMessage("&9&lBitcoin Menu"))) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getSlot() == 12) {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                playersTransferring.add(player);
                player.sendMessage(" ");
                player.sendMessage(util.colorMessage("&aYour balance: &2" + bitcoinManager.getBalance(player) + " bitcoins"));
                player.sendMessage(util.colorMessage("&aEnter the player and amount of bitcoins (e.g. Notch 5):"));
                sendCancelButton(player);
            } else if (event.getSlot() == 14) {
                player.closeInventory();
                if (plugin.getEconomy() == null) {
                    player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1, 1);
                    player.sendMessage(util.colorMessage("&cNo economy plugin was detected."));
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    playersExchanging.add(player);
                    player.sendMessage(" ");
                    player.sendMessage(util.colorMessage("&aYour balance: &2" + bitcoinManager.getBalance(player) + " bitcoins"));
                    player.sendMessage(util.colorMessage("&aCurrent bitcoin value: &2" + bitcoinManager.getExchangeCurrencySymbol() + bitcoinManager.getBitcoinValue()));
                    player.sendMessage(util.colorMessage("&aEnter the amount of bitcoins you would like to exchange:"));
                    sendCancelButton(player);
                }
            } else if (event.getSlot() == 16) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                plugin.getMining().openInterface(player);
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onQuit(PlayerQuitEvent event) {
        if (menus.containsKey(event.getPlayer())) { menus.remove(event.getPlayer()); }
        if (playersExchanging.contains(event.getPlayer())) { playersExchanging.remove(event.getPlayer()); }
        if (playersTransferring.contains(event.getPlayer())) { playersTransferring.remove(event.getPlayer()); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().equalsIgnoreCase("/bitcoin cancel")) {
            if (playersExchanging.contains(event.getPlayer()) || playersTransferring.contains(event.getPlayer())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(util.colorMessage("&cYou cannot use commands at this time."));
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playersExchanging.contains(player)) {
            event.setCancelled(true);
            try {
                int exchangeAmount = Integer.valueOf(event.getMessage());
                if (exchangeAmount > bitcoinManager.getBalance(player)) {
                    player.sendMessage(util.colorMessage("&cYou only have " + bitcoinManager.getBalance(player) + " bitcoins."));
                } else {
                    if (exchangeAmount < 1) {
                        player.sendMessage(util.colorMessage("&cInvalid number."));
                    } else {
                        bitcoinManager.withdraw(player, exchangeAmount);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.sendMessage(util.colorMessage("&aSuccessfully exchanged " + exchangeAmount + " bitcoins for " + bitcoinManager.getExchangeCurrencySymbol() + bitcoinManager.getBitcoinValue() * exchangeAmount + plugin.getEconomy().currencyNamePlural() + "."));
                        plugin.getEconomy().depositPlayer(player, player.getWorld().getName(), bitcoinManager.getBitcoinValue() * exchangeAmount);
                        playersExchanging.remove(player);
                    }
                }
            } catch (NumberFormatException e) {
                player.sendMessage(util.colorMessage("&cInvalid number."));
            }

        } else if (playersTransferring.contains(player)) {
            event.setCancelled(true);
            String[] message = event.getMessage().split(" ");
            if (message.length == 2) {
                Player recipient = Bukkit.getPlayer(message[0]);
                if (recipient != null) {
                    if (recipient.equals(player)) {
                        player.sendMessage(util.colorMessage("&cYou cannot transfer bitcoins to yourself."));
                    } else {
                        try {
                            int transferAmount = Integer.valueOf(message[1]);
                            if (transferAmount > bitcoinManager.getBalance(player)) {
                                player.sendMessage(util.colorMessage("&cYou only have " + bitcoinManager.getBalance(player) + " bitcoins."));
                            } else {
                                if (transferAmount < 1) {
                                    player.sendMessage(util.colorMessage("&cInvalid number."));
                                } else {
                                    bitcoinManager.withdraw(player, transferAmount);
                                    bitcoinManager.deposit(recipient, transferAmount);
                                    player.sendMessage(util.colorMessage("&aSuccessfully transferred " + transferAmount + " bitcoins to &2" + recipient.getName() + "."));
                                    recipient.sendMessage(util.colorMessage("&aYou received " + transferAmount + " bitcoins from &2" + player.getName() + "."));
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                    recipient.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                    playersTransferring.remove(player);
                                }
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(util.colorMessage("&cInvalid number."));
                        }
                    }
                } else {
                    player.sendMessage(util.colorMessage("&4" + message[0] + " &cis not online."));
                }
            } else {
                player.sendMessage(util.colorMessage("&cInvalid entry."));
            }

        }
    }
}