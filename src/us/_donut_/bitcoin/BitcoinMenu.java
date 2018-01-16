package us._donut_.bitcoin;

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
    private Messages messages;
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
        messages = plugin.getMessages();

        darkBlueGlass = util.createItemStack(Material.STAINED_GLASS_PANE, (short) 11, " ", null);
        lightBlueGlass = util.createItemStack(Material.STAINED_GLASS_PANE, (short) 3, " ", null);
        transferBitcoinItem = util.createItemStack(Material.BOOK_AND_QUILL, (short) 0, messages.getMessage("transfer_item_name"), messages.getMessage("transfer_item_lore"));
        exchangeBitcoinItem = util.createItemStack(Material.GOLD_INGOT, (short) 0, messages.getMessage("exchange_item_name"), messages.getMessage("exchange_item_lore"));
        miningBitcoinItem = util.createItemStack(Material.DIAMOND_PICKAXE, (short) 0, messages.getMessage("mining_item_name"), messages.getMessage("mining_item_lore"));
        updateGlassInMenus();
    }

    List<Player> getPlayersExchanging() { return playersExchanging; }
    List<Player> getPlayersTransferring() { return playersTransferring; }

    void open(Player player) {
        if (menus.containsKey(player)) {
            menus.get(player).setItem(10, util.getSkull(player.getUniqueId(), player.getName(), messages.getMessage("statistic_item_name"), messages.getMessage("statistic_item_lore").replace("{BALANCE}", String.valueOf(bitcoinManager.getBalance(player))).replace("{AMOUNT_SOLVED}", String.valueOf(bitcoinManager.getPuzzlesSolved(player))).replace("{AMOUNT_MINED}", String.valueOf(bitcoinManager.getBitcoinsMined(player)))));
        } else {
            createMenu(player);
        }
        player.openInventory(menus.get(player));
    }

    private void createMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, messages.getMessage("menu_title"));
        menu.setItem(10, util.getSkull(player.getUniqueId(), player.getName(), messages.getMessage("statistic_item_name"), messages.getMessage("statistic_item_lore").replace("{BALANCE}", String.valueOf(bitcoinManager.getBalance(player))).replace("{AMOUNT_SOLVED}", String.valueOf(bitcoinManager.getPuzzlesSolved(player))).replace("{AMOUNT_MINED}", String.valueOf(bitcoinManager.getBitcoinsMined(player)))));
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
        TextComponent cancelButton = new TextComponent(TextComponent.fromLegacyText(messages.getMessage("cancel_button")));
        cancelButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bitcoin cancel"));
        cancelButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(messages.getMessage("cancel_button_hover")).create()));
        player.spigot().sendMessage(cancelButton);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onDragInGUI(InventoryDragEvent event) {
        if (event.getInventory().getName() != null && event.getInventory().getName().equalsIgnoreCase(messages.getMessage("menu_title"))) { event.setCancelled(true); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onMoveInGUI(InventoryMoveItemEvent event) {
        if (event.getDestination().getName() != null && event.getDestination().getName().equalsIgnoreCase(messages.getMessage("menu_title"))) { event.setCancelled(true); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase(messages.getMessage("menu_title"))) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getSlot() == 12) {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                playersTransferring.add(player);
                player.sendMessage(messages.getMessage("begin_transfer").replace("{BALANCE}", String.valueOf(bitcoinManager.getBalance(player))));
                sendCancelButton(player);
            } else if (event.getSlot() == 14) {
                player.closeInventory();
                if (plugin.getEconomy() == null) {
                    player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1, 1);
                    player.sendMessage(messages.getMessage("no_economy"));
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    playersExchanging.add(player);
                    player.sendMessage(messages.getMessage("begin_exchange").replace("{BALANCE}", String.valueOf(bitcoinManager.getBalance(player))).replace("{VALUE}", bitcoinManager.getExchangeCurrencySymbol() + bitcoinManager.getBitcoinValue()));
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
                event.getPlayer().sendMessage(messages.getMessage("cannot_use_commands"));
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
                    player.sendMessage(messages.getMessage("not_enough_bitcoins").replace("{BALANCE}", String.valueOf(bitcoinManager.getBalance(player))));
                } else {
                    if (exchangeAmount < 1) {
                        player.sendMessage(messages.getMessage("invalid_number"));
                    } else {
                        bitcoinManager.withdraw(player, exchangeAmount);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.sendMessage(messages.getMessage("complete_exchange").replace("{AMOUNT}", String.valueOf(exchangeAmount)).replace("{NEW_AMOUNT}", bitcoinManager.getExchangeCurrencySymbol() + bitcoinManager.getBitcoinValue() * exchangeAmount));
                        plugin.getEconomy().depositPlayer(player, player.getWorld().getName(), bitcoinManager.getBitcoinValue() * exchangeAmount);
                        playersExchanging.remove(player);
                    }
                }
            } catch (NumberFormatException e) {
                player.sendMessage(messages.getMessage("invalid_number"));
            }

        } else if (playersTransferring.contains(player)) {
            event.setCancelled(true);
            String[] message = event.getMessage().split(" ");
            if (message.length == 2) {
                Player recipient = Bukkit.getPlayer(message[0]);
                if (recipient != null) {
                    if (recipient.equals(player)) {
                        player.sendMessage(messages.getMessage("cannot_transfer_to_self"));
                    } else {
                        try {
                            int transferAmount = Integer.valueOf(message[1]);
                            if (transferAmount > bitcoinManager.getBalance(player)) {
                                player.sendMessage(messages.getMessage("not_enough_bitcoins").replace("{BALANCE}", String.valueOf(bitcoinManager.getBalance(player))));
                            } else {
                                if (transferAmount < 1) {
                                    player.sendMessage(messages.getMessage("invalid_number"));
                                } else {
                                    bitcoinManager.withdraw(player, transferAmount);
                                    bitcoinManager.deposit(recipient, transferAmount);
                                    player.sendMessage(messages.getMessage("complete_transfer").replace("{AMOUNT}", String.valueOf(transferAmount)).replace("{RECIPIENT}", recipient.getName()));
                                    recipient.sendMessage(messages.getMessage("receive_bitcoins").replace("{AMOUNT}", String.valueOf(transferAmount)).replace("{SENDER}", player.getName()));
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                    recipient.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                    playersTransferring.remove(player);
                                }
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(messages.getMessage("invalid_number"));
                        }
                    }
                } else {
                    player.sendMessage(messages.getMessage("not_online").replace("{PLAYER}", message[0]));
                }
            } else {
                player.sendMessage(messages.getMessage("invalid_entry"));
            }

        }
    }
}