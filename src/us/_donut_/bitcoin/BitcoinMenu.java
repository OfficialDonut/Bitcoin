package us._donut_.bitcoin;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

import java.util.*;

class BitcoinMenu implements Listener {

    private Bitcoin plugin;
    private Util util;
    private BitcoinManager bitcoinManager;
    private BlackMarket blackMarket;
    private Messages messages;
    private Sounds sounds;
    private Map<Player, Inventory> menus = new HashMap<>();
    private int[] evenSlots = {0, 2, 4, 6, 8, 18, 20, 22, 24, 26};
    private int[] oddSlots = {1, 3, 5, 7, 9, 17, 19, 21, 23, 25};
    private ItemStack darkBlueGlass;
    private ItemStack lightBlueGlass;
    private ItemStack transferBitcoinItem;
    private ItemStack exchangeBitcoinItem;
    private ItemStack buyBitcoinItem;
    private ItemStack miningBitcoinItem;
    private ItemStack helpItem;
    private ItemStack blackMarketItem;
    private List<Player> playersExchanging = new ArrayList<>();
    private List<Player> playersTransferring = new ArrayList<>();
    private List<Player> playersBuying = new ArrayList<>();

    BitcoinMenu(Bitcoin pluginInstance) {
        plugin = pluginInstance;
        util = plugin.getUtil();
        bitcoinManager = plugin.getBitcoinManager();
        blackMarket = plugin.getBlackMarket();
        messages = plugin.getMessages();
        sounds = plugin.getSounds();
        reload();
        updateGlassInMenus();
    }

    void reload() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTitle().equalsIgnoreCase(messages.getMessage("menu_title"))) { player.closeInventory(); }
        }
        menus.clear();
        darkBlueGlass = util.createItemStack(Material.STAINED_GLASS_PANE, (short) 11, " ", null);
        lightBlueGlass = util.createItemStack(Material.STAINED_GLASS_PANE, (short) 3, " ", null);
        transferBitcoinItem = util.createItemStack(Material.BOOK_AND_QUILL, (short) 0, messages.getMessage("transfer_item_name"), messages.getMessage("transfer_item_lore"));
        exchangeBitcoinItem = util.createItemStack(Material.GOLD_INGOT, (short) 0, messages.getMessage("exchange_item_name"), messages.getMessage("exchange_item_lore"));
        buyBitcoinItem = util.createItemStack(Material.EMERALD, (short) 0, messages.getMessage("buy_item_name"), messages.getMessage("buy_item_lore"));
        miningBitcoinItem = util.createItemStack(Material.DIAMOND_PICKAXE, (short) 0, messages.getMessage("mining_item_name"), messages.getMessage("mining_item_lore"));
        helpItem = util.createItemStack(Material.PAPER, (short) 0, messages.getMessage("help_item_name"), messages.getMessage("help_item_lore"));
        blackMarketItem = util.createItemStack(Material.ENDER_CHEST, (short) 0, messages.getMessage("black_market_item_name"), messages.getMessage("black_market_item_lore"));
    }

    List<Player> getPlayersExchanging() { return playersExchanging; }
    List<Player> getPlayersTransferring() { return playersTransferring; }
    List<Player> getPlayersBuying() { return playersBuying; }

    void open(Player player) {
        if (menus.containsKey(player)) {
            menus.get(player).setItem(10, util.getSkull(player.getUniqueId(), player.getName(), messages.getMessage("statistic_item_name"), messages.getMessage("statistic_item_lore").replace("{BALANCE}", util.formatNumber(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getBalance(player.getUniqueId())))).replace("{AMOUNT_SOLVED}", util.formatNumber(bitcoinManager.getPuzzlesSolved(player.getUniqueId()))).replace("{AMOUNT_MINED}", util.formatNumber(bitcoinManager.getBitcoinsMined(player.getUniqueId()))).replace("{MIN}", String.valueOf(bitcoinManager.getBestPuzzleTime(player.getUniqueId()) / 60.0).split("\\.")[0]).replace("{SEC}", String.valueOf(bitcoinManager.getBestPuzzleTime(player.getUniqueId()) % 60))));
        } else {
            createMenu(player);
        }
        player.openInventory(menus.get(player));
    }

    private void createMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, messages.getMessage("menu_title"));
        menu.setItem(10, util.getSkull(player.getUniqueId(), player.getName(), messages.getMessage("statistic_item_name"), messages.getMessage("statistic_item_lore").replace("{BALANCE}", util.formatNumber(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getBalance(player.getUniqueId())))).replace("{AMOUNT_SOLVED}", util.formatNumber(bitcoinManager.getPuzzlesSolved(player.getUniqueId()))).replace("{AMOUNT_MINED}", util.formatNumber(bitcoinManager.getBitcoinsMined(player.getUniqueId()))).replace("{MIN}", String.valueOf(bitcoinManager.getBestPuzzleTime(player.getUniqueId()) / 60.0).split("\\.")[0]).replace("{SEC}", String.valueOf(bitcoinManager.getBestPuzzleTime(player.getUniqueId()) % 60))));
        menu.setItem(11, transferBitcoinItem);
        menu.setItem(12, buyBitcoinItem);
        menu.setItem(13, exchangeBitcoinItem);
        menu.setItem(14, miningBitcoinItem);
        menu.setItem(15, blackMarketItem);
        menu.setItem(16, helpItem);
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
            if (event.getSlot() == 11) {
                if (!player.hasPermission("bitcoin.gui.transfer")) { player.sendMessage(messages.getMessage("no_permission")); }
                player.closeInventory();
                player.playSound(player.getLocation(), sounds.getSound("click_transfer_item"), 1, 1);
                playersTransferring.add(player);
                player.sendMessage(messages.getMessage("begin_transfer").replace("{BALANCE}", String.valueOf(bitcoinManager.getBalance(player.getUniqueId()))));
                sendCancelButton(player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (playersTransferring.contains(player)) {
                            player.performCommand("bitcoin cancel");
                        }
                    }
                }.runTaskLater(plugin, 300);
            } else if (event.getSlot() == 12) {
                if (!player.hasPermission("bitcoin.gui.buy")) { player.sendMessage(messages.getMessage("no_permission")); }
                if (!plugin.getEconomy().hasEconomy()) {
                    player.playSound(player.getLocation(), sounds.getSound("no_economy"), 1, 1);
                    player.sendMessage(messages.getMessage("no_economy"));
                } else {
                    player.closeInventory();
                    player.playSound(player.getLocation(), sounds.getSound("click_buy_item"), 1, 1);
                    playersBuying.add(player);
                    player.sendMessage(messages.getMessage("begin_purchase").replace("{BANK}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(),bitcoinManager.getAmountInBank()))).replace("{VALUE}", bitcoinManager.getExchangeCurrencySymbol() + bitcoinManager.getBitcoinValue()).replace("{TAX}", bitcoinManager.getPurchaseTaxPercentage() + "%"));
                    sendCancelButton(player);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (playersBuying.contains(player)) {
                                player.performCommand("bitcoin cancel");
                            }
                        }
                    }.runTaskLater(plugin, 300);
                }
            } else if (event.getSlot() == 13) {
                if (!player.hasPermission("bitcoin.gui.sell")) { player.sendMessage(messages.getMessage("no_permission")); }
                player.closeInventory();
                if (!plugin.getEconomy().hasEconomy()) {
                    player.playSound(player.getLocation(), sounds.getSound("no_economy"), 1, 1);
                    player.sendMessage(messages.getMessage("no_economy"));
                } else {
                    player.playSound(player.getLocation(), sounds.getSound("click_exchange_item"), 1, 1);
                    playersExchanging.add(player);
                    player.sendMessage(messages.getMessage("begin_exchange").replace("{BALANCE}", String.valueOf(bitcoinManager.getBalance(player.getUniqueId()))).replace("{VALUE}", bitcoinManager.getExchangeCurrencySymbol() + bitcoinManager.getBitcoinValue()));
                    sendCancelButton(player);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (playersExchanging.contains(player)) {
                                player.performCommand("bitcoin cancel");
                            }
                        }
                    }.runTaskLater(plugin, 300);
                }
            } else if (event.getSlot() == 14) {
                if (!player.hasPermission("bitcoin.gui.mine")) { player.sendMessage(messages.getMessage("no_permission")); }
                player.playSound(player.getLocation(), sounds.getSound("click_mining_item"), 1, 1);
                plugin.getMining().openInterface(player);
            } else if (event.getSlot() == 15) {
                if (!player.hasPermission("bitcoin.gui.blackmarket")) { player.sendMessage(messages.getMessage("no_permission")); }
                player.playSound(player.getLocation(), sounds.getSound("click_black_market_item"), 1, 1);
                blackMarket.open(player);
            } else if (event.getSlot() == 16) {
                if (!player.hasPermission("bitcoin.gui.help")) { player.sendMessage(messages.getMessage("no_permission")); }
                player.closeInventory();
                player.playSound(player.getLocation(), sounds.getSound("click_help_item"), 1, 1);
                player.sendMessage(messages.getMessage("help_command"));
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onQuit(PlayerQuitEvent event) {
        if (menus.containsKey(event.getPlayer())) { menus.remove(event.getPlayer()); }
        if (playersExchanging.contains(event.getPlayer())) { playersExchanging.remove(event.getPlayer()); }
        if (playersTransferring.contains(event.getPlayer())) { playersTransferring.remove(event.getPlayer()); }
        if (playersBuying.contains(event.getPlayer())) { playersBuying.remove(event.getPlayer()); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().equalsIgnoreCase("/bitcoin cancel")) {
            if (playersExchanging.contains(event.getPlayer()) || playersTransferring.contains(event.getPlayer()) || playersBuying.contains(event.getPlayer())) {
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
                double exchangeAmount = Double.valueOf(event.getMessage());
                if (exchangeAmount > bitcoinManager.getBalance(player.getUniqueId())) { player.sendMessage(messages.getMessage("not_enough_bitcoins").replace("{BALANCE}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getBalance(player.getUniqueId()))))); return; }
                    if (exchangeAmount <= 0) { player.sendMessage(messages.getMessage("invalid_number")); return; }
                    bitcoinManager.withdraw(player.getUniqueId(), exchangeAmount);
                    bitcoinManager.addToBank(exchangeAmount);
                    player.playSound(player.getLocation(), sounds.getSound("complete_exchange"), 1, 1);
                    player.sendMessage(messages.getMessage("complete_exchange").replace("{AMOUNT}", util.formatNumber(exchangeAmount)).replace("{NEW_AMOUNT}", bitcoinManager.getExchangeCurrencySymbol() + util.formatNumber(util.round(2, bitcoinManager.getBitcoinValue() * exchangeAmount))));
                    plugin.getEconomy().depositPlayer(player, player.getWorld().getName(), bitcoinManager.getBitcoinValue() * exchangeAmount);
                    playersExchanging.remove(player);
            } catch (NumberFormatException e) {
                player.sendMessage(messages.getMessage("invalid_number"));
            }

        } else if (playersTransferring.contains(player)) {
            event.setCancelled(true);
            String[] message = event.getMessage().split(" ");
            if (message.length != 2) { player.sendMessage(messages.getMessage("invalid_entry")); return; }
            Player recipient = Bukkit.getPlayer(message[0]);
            if (recipient == null) { player.sendMessage(messages.getMessage("not_online").replace("{PLAYER}", message[0])); return; }
            if (recipient.equals(player)) { player.sendMessage(messages.getMessage("cannot_transfer_to_self")); return; }
            try {
                double transferAmount = Double.valueOf(message[1]);
                if (transferAmount > bitcoinManager.getBalance(player.getUniqueId())) { player.sendMessage(messages.getMessage("not_enough_bitcoins").replace("{BALANCE}",util.formatNumber(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getBalance(player.getUniqueId()))))); return; }
                if (transferAmount <= 0) { player.sendMessage(messages.getMessage("invalid_number")); return; }
                bitcoinManager.withdraw(player.getUniqueId(), transferAmount);
                bitcoinManager.deposit(recipient.getUniqueId(), transferAmount);
                player.sendMessage(messages.getMessage("complete_transfer").replace("{AMOUNT}", util.formatNumber(transferAmount)).replace("{RECIPIENT}", recipient.getName()));
                recipient.sendMessage(messages.getMessage("receive_bitcoins").replace("{AMOUNT}", util.formatNumber(transferAmount)).replace("{SENDER}", player.getName()));
                player.playSound(player.getLocation(), sounds.getSound("complete_transfer"), 1, 1);
                recipient.playSound(player.getLocation(), sounds.getSound("complete_transfer"), 1, 1);
                playersTransferring.remove(player);
            } catch (NumberFormatException e) {
                player.sendMessage(messages.getMessage("invalid_number"));
            }

        } else if (playersBuying.contains(player)) {
            event.setCancelled(true);
            try {
                double buyAmount = Double.valueOf(event.getMessage());
                if (buyAmount > bitcoinManager.getAmountInBank()) { player.sendMessage(messages.getMessage("not_enough_in_bank").replace("{AMOUNT}", util.formatNumber(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getAmountInBank())))); return; }
                if (buyAmount <= 0) { player.sendMessage(messages.getMessage("invalid_number")); return; }
                double cost = (buyAmount * bitcoinManager.getBitcoinValue()) * (1 + bitcoinManager.getPurchaseTaxPercentage() / 100);
                if (cost > plugin.getEconomy().getBalance(player)) { player.sendMessage(messages.getMessage("not_enough_money")); return; }
                bitcoinManager.deposit(player.getUniqueId(), buyAmount);
                bitcoinManager.removeFromBank(buyAmount);
                player.playSound(player.getLocation(), sounds.getSound("complete_purchase"), 1, 1);
                player.sendMessage(messages.getMessage("complete_purchase").replace("{AMOUNT}", util.formatNumber(buyAmount)).replace("{COST}", bitcoinManager.getExchangeCurrencySymbol() + util.formatNumber(util.round(2, bitcoinManager.getBitcoinValue() * buyAmount))).replace("{TAX}", bitcoinManager.getExchangeCurrencySymbol() + util.formatNumber(util.round(2, bitcoinManager.getPurchaseTaxPercentage() / 100 * cost))));
                plugin.getEconomy().withdrawPlayer(player, player.getWorld().getName(), cost);
                playersBuying.remove(player);
            } catch (NumberFormatException e) {
                player.sendMessage(messages.getMessage("invalid_number"));
            }
        }
    }
}