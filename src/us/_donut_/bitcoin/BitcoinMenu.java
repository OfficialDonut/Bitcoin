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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us._donut_.bitcoin.configuration.Message;
import us._donut_.bitcoin.configuration.Sounds;

import java.util.*;

import static us._donut_.bitcoin.util.Util.*;

class BitcoinMenu implements Listener {

    private Bitcoin plugin = Bitcoin.plugin;
    private BitcoinManager bitcoinManager;
    private BlackMarket blackMarket;
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

    BitcoinMenu() {
        bitcoinManager = plugin.getBitcoinManager();
        blackMarket = plugin.getBlackMarket();
        sounds = plugin.getSounds();
        reload();
        updateGlassInMenus();
    }

    void reload() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTitle().equalsIgnoreCase(Message.MENU_TITLE.toString())) { player.closeInventory(); }
        }
        menus.clear();
        if (!Bukkit.getVersion().contains("1.13")) {
            darkBlueGlass = createItemStack(Material.STAINED_GLASS_PANE, (short) 11, " ", null);
            lightBlueGlass = createItemStack(Material.STAINED_GLASS_PANE, (short) 3, " ", null);
            transferBitcoinItem = createItemStack(Material.BOOK_AND_QUILL, (short) 0, Message.TRANSFER_ITEM_NAME.toString(), Message.TRANSFER_ITEM_LORE.toString());
        } else {
            darkBlueGlass = createItemStack(Material.valueOf("BLUE_STAINED_GLASS_PANE"), (short) 0, " ", null);
            lightBlueGlass = createItemStack(Material.valueOf("LIGHT_BLUE_STAINED_GLASS_PANE"), (short) 0, " ", null);
            transferBitcoinItem = createItemStack(Material.valueOf("WRITABLE_BOOK"), (short) 0, Message.TRANSFER_ITEM_NAME.toString(), Message.TRANSFER_ITEM_LORE.toString());
        }
        exchangeBitcoinItem = createItemStack(Material.GOLD_INGOT, (short) 0, Message.EXCHANGE_ITEM_NAME.toString(), Message.EXCHANGE_ITEM_LORE.toString());
        buyBitcoinItem = createItemStack(Material.EMERALD, (short) 0, Message.BUY_ITEM_NAME.toString(), Message.BUY_ITEM_LORE.toString());
        miningBitcoinItem = createItemStack(Material.DIAMOND_PICKAXE, (short) 0, Message.MINING_ITEM_NAME.toString(), Message.MINING_ITEM_LORE.toString());
        ItemMeta itemMeta = miningBitcoinItem.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS);
        miningBitcoinItem.setItemMeta(itemMeta);
        helpItem = createItemStack(Material.PAPER, (short) 0, Message.HELP_ITEM_NAME.toString(), Message.HELP_ITEM_LORE.toString());
        blackMarketItem = createItemStack(Material.ENDER_CHEST, (short) 0, Message.BLACK_MARKET_ITEM_NAME.toString(), Message.BLACK_MARKET_ITEM_LORE.toString());
    }

    List<Player> getPlayersExchanging() { return playersExchanging; }
    List<Player> getPlayersTransferring() { return playersTransferring; }
    List<Player> getPlayersBuying() { return playersBuying; }

    void open(Player player) {
        if (menus.containsKey(player)) {
            menus.get(player).setItem(10, getSkull(player.getUniqueId(), player.getName(), Message.STATISTIC_ITEM_NAME.toString(), Message.STATISTIC_ITEM_LORE.toString()
                    .replace("{BALANCE}", formatRoundNumber(bitcoinManager.getBalance(player.getUniqueId())))
                    .replace("{AMOUNT_SOLVED}", formatNumber(bitcoinManager.getPuzzlesSolved(player.getUniqueId())))
                    .replace("{AMOUNT_MINED}", formatRoundNumber(bitcoinManager.getBitcoinsMined(player.getUniqueId())))
                    .replace("{MIN}", String.valueOf(bitcoinManager.getBestPuzzleTime(player.getUniqueId()) / 60.0).split("\\.")[0])
                    .replace("{SEC}", String.valueOf(bitcoinManager.getBestPuzzleTime(player.getUniqueId()) % 60))));
        } else {
            createMenu(player);
        }
        player.openInventory(menus.get(player));
    }

    private void createMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, Message.MENU_TITLE.toString());
        menu.setItem(10, getSkull(player.getUniqueId(), player.getName(), Message.STATISTIC_ITEM_NAME.toString(), Message.STATISTIC_ITEM_LORE.toString()
                .replace("{BALANCE}", formatRoundNumber(bitcoinManager.getBalance(player.getUniqueId())))
                .replace("{AMOUNT_SOLVED}", formatNumber(bitcoinManager.getPuzzlesSolved(player.getUniqueId())))
                .replace("{AMOUNT_MINED}", formatRoundNumber(bitcoinManager.getBitcoinsMined(player.getUniqueId())))
                .replace("{MIN}", String.valueOf(bitcoinManager.getBestPuzzleTime(player.getUniqueId()) / 60.0).split("\\.")[0])
                .replace("{SEC}", String.valueOf(bitcoinManager.getBestPuzzleTime(player.getUniqueId()) % 60))));
        menu.setItem(11, transferBitcoinItem);
        menu.setItem(12, buyBitcoinItem);
        menu.setItem(13, exchangeBitcoinItem);
        menu.setItem(14, miningBitcoinItem);
        menu.setItem(15, blackMarketItem);
        menu.setItem(16, helpItem);
        Arrays.stream(evenSlots).forEach(slot -> menu.setItem(slot, darkBlueGlass));
        Arrays.stream(oddSlots).forEach(slot -> menu.setItem(slot, lightBlueGlass));
        menus.put(player, menu);
    }

    private void updateGlassInMenus() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> menus.values().forEach(menu -> {
            if (!Bukkit.getVersion().contains("1.13")) {
                Arrays.stream(evenSlots).forEach(slot -> menu.setItem(slot, menu.getItem(0).getDurability() == (short) 11 ? lightBlueGlass : darkBlueGlass));
                Arrays.stream(oddSlots).forEach(slot -> menu.setItem(slot, menu.getItem(0).getDurability() == (short) 11 ? darkBlueGlass : lightBlueGlass));
            } else {
                Arrays.stream(evenSlots).forEach(slot -> menu.setItem(slot, menu.getItem(0).getType() == Material.valueOf("BLUE_STAINED_GLASS_PANE") ? lightBlueGlass : darkBlueGlass));
                Arrays.stream(oddSlots).forEach(slot -> menu.setItem(slot, menu.getItem(0).getType() == Material.valueOf("BLUE_STAINED_GLASS_PANE") ? darkBlueGlass : lightBlueGlass));
            }
        }),0, 5);
    }

    private void sendCancelButton(Player player) {
        TextComponent cancelButton = new TextComponent(TextComponent.fromLegacyText(Message.CANCEL_BUTTON.toString()));
        cancelButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bitcoin cancel"));
        cancelButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Message.CANCEL_BUTTON_HOVER.toString()).create()));
        player.spigot().sendMessage(cancelButton);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onDragInGUI(InventoryDragEvent event) {
        if (event.getInventory().getName() != null && event.getInventory().getName().equalsIgnoreCase(Message.MENU_TITLE.toString())) { event.setCancelled(true); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onMoveInGUI(InventoryMoveItemEvent event) {
        if (event.getDestination().getName() != null && event.getDestination().getName().equalsIgnoreCase(Message.MENU_TITLE.toString())) { event.setCancelled(true); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase(Message.MENU_TITLE.toString())) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getSlot() == 11) {
                if (!player.hasPermission("bitcoin.gui.transfer")) { player.sendMessage(Message.NO_PERMISSION.toString()); }
                player.closeInventory();
                player.playSound(player.getLocation(), sounds.getSound("click_transfer_item"), 1, 1);
                playersTransferring.add(player);
                player.sendMessage(Message.BEGIN_TRANSFER.toString().replace("{BALANCE}", formatRoundNumber(bitcoinManager.getBalance(player.getUniqueId()))));
                sendCancelButton(player);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (playersTransferring.contains(player)) {
                        player.performCommand("bitcoin cancel");
                    }
                }, 300);
            } else if (event.getSlot() == 12) {
                if (!player.hasPermission("bitcoin.gui.buy")) { player.sendMessage(Message.NO_PERMISSION.toString()); }
                if (!plugin.getEconomy().hasEconomy()) {
                    player.playSound(player.getLocation(), sounds.getSound("no_economy"), 1, 1);
                    player.sendMessage(Message.NO_ECONOMY.toString());
                } else {
                    player.closeInventory();
                    player.playSound(player.getLocation(), sounds.getSound("click_buy_item"), 1, 1);
                    playersBuying.add(player);
                    player.sendMessage(Message.BEGIN_PURCHASE.toString().replace("{BANK}", formatRoundNumber(bitcoinManager.getAmountInBank()))
                            .replace("{VALUE}", bitcoinManager.getExchangeCurrencySymbol() + formatRound2Number(bitcoinManager.getBitcoinValue()))
                            .replace("{TAX}", bitcoinManager.getPurchaseTaxPercentage() + "%"));
                    sendCancelButton(player);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (playersBuying.contains(player)) {
                            player.performCommand("bitcoin cancel");
                        }
                    }, 300);
                }
            } else if (event.getSlot() == 13) {
                if (!player.hasPermission("bitcoin.gui.sell")) { player.sendMessage(Message.NO_PERMISSION.toString()); }
                player.closeInventory();
                if (!plugin.getEconomy().hasEconomy()) {
                    player.playSound(player.getLocation(), sounds.getSound("no_economy"), 1, 1);
                    player.sendMessage(Message.NO_ECONOMY.toString());
                } else {
                    player.playSound(player.getLocation(), sounds.getSound("click_exchange_item"), 1, 1);
                    playersExchanging.add(player);
                    player.sendMessage(Message.BEGIN_EXCHANGE.toString()
                            .replace("{BALANCE}", String.valueOf(bitcoinManager.getBalance(player.getUniqueId())))
                            .replace("{VALUE}", bitcoinManager.getExchangeCurrencySymbol() + formatRound2Number(bitcoinManager.getBitcoinValue())));
                    sendCancelButton(player);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (playersExchanging.contains(player)) {
                            player.performCommand("bitcoin cancel");
                        }
                    }, 300);
                }
            } else if (event.getSlot() == 14) {
                if (!player.hasPermission("bitcoin.gui.mine")) { player.sendMessage(Message.NO_PERMISSION.toString()); }
                player.playSound(player.getLocation(), sounds.getSound("click_mining_item"), 1, 1);
                plugin.getMining().openInterface(player);
            } else if (event.getSlot() == 15) {
                if (!player.hasPermission("bitcoin.gui.blackmarket")) { player.sendMessage(Message.NO_PERMISSION.toString()); }
                player.playSound(player.getLocation(), sounds.getSound("click_black_market_item"), 1, 1);
                blackMarket.open(player);
            } else if (event.getSlot() == 16) {
                if (!player.hasPermission("bitcoin.gui.help")) { player.sendMessage(Message.NO_PERMISSION.toString()); }
                player.closeInventory();
                player.playSound(player.getLocation(), sounds.getSound("click_help_item"), 1, 1);
                player.sendMessage(Message.HELP_COMMAND.toString());
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onQuit(PlayerQuitEvent event) {
        menus.remove(event.getPlayer());
        playersExchanging.remove(event.getPlayer());
        playersTransferring.remove(event.getPlayer());
        playersBuying.remove(event.getPlayer());
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().equalsIgnoreCase("/bitcoin cancel")) {
            if (playersExchanging.contains(event.getPlayer()) || playersTransferring.contains(event.getPlayer()) || playersBuying.contains(event.getPlayer())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Message.CANNOT_USE_COMMANDS.toString());
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
                if (exchangeAmount > bitcoinManager.getBalance(player.getUniqueId())) { player.sendMessage(Message.NOT_ENOUGH_BITCOINS.toString().replace("{BALANCE}", formatRoundNumber(bitcoinManager.getBalance(player.getUniqueId())))); return; }
                    if (exchangeAmount <= 0) { player.sendMessage(Message.INVALID_NUMBER.toString()); return; }
                    bitcoinManager.withdraw(player.getUniqueId(), exchangeAmount);
                    bitcoinManager.addToBank(exchangeAmount);
                    player.playSound(player.getLocation(), sounds.getSound("complete_exchange"), 1, 1);
                    player.sendMessage(Message.COMPLETE_EXCHANGE.toString()
                            .replace("{AMOUNT}", formatNumber(exchangeAmount))
                            .replace("{NEW_AMOUNT}", bitcoinManager.getExchangeCurrencySymbol() + formatRound2Number(bitcoinManager.getBitcoinValue() * exchangeAmount)));
                    plugin.getEconomy().depositPlayer(player, player.getWorld().getName(), bitcoinManager.getBitcoinValue() * exchangeAmount);
                    playersExchanging.remove(player);
            } catch (NumberFormatException e) {
                player.sendMessage(Message.INVALID_NUMBER.toString());
            }

        } else if (playersTransferring.contains(player)) {
            event.setCancelled(true);
            String[] message = event.getMessage().split(" ");
            if (message.length != 2) { player.sendMessage(Message.INVALID_ENTRY.toString()); return; }
            Player recipient = Bukkit.getPlayer(message[0]);
            if (recipient == null) { player.sendMessage(Message.NOT_ONLINE.toString().replace("{PLAYER}", message[0])); return; }
            if (recipient.equals(player)) { player.sendMessage(Message.CANNOT_TRANSFER_TO_SELF.toString()); return; }
            try {
                double transferAmount = Double.valueOf(message[1]);
                if (transferAmount > bitcoinManager.getBalance(player.getUniqueId())) { player.sendMessage(Message.NOT_ENOUGH_BITCOINS.toString().replace("{BALANCE}", formatRoundNumber(bitcoinManager.getBalance(player.getUniqueId())))); return; }
                if (transferAmount <= 0) { player.sendMessage(Message.INVALID_NUMBER.toString()); return; }
                bitcoinManager.withdraw(player.getUniqueId(), transferAmount);
                bitcoinManager.deposit(recipient.getUniqueId(), transferAmount);
                player.sendMessage(Message.CANNOT_TRANSFER_TO_SELF.toString()
                        .replace("{AMOUNT}", formatNumber(transferAmount))
                        .replace("{RECIPIENT}", bitcoinManager.getOfflinePlayerName(recipient)));
                recipient.sendMessage(Message.RECEIVE_BITCOINS.toString()
                        .replace("{AMOUNT}", formatNumber(transferAmount))
                        .replace("{SENDER}", player.getDisplayName()));
                player.playSound(player.getLocation(), sounds.getSound("complete_transfer"), 1, 1);
                recipient.playSound(player.getLocation(), sounds.getSound("complete_transfer"), 1, 1);
                playersTransferring.remove(player);
            } catch (NumberFormatException e) {
                player.sendMessage(Message.INVALID_NUMBER.toString());
            }

        } else if (playersBuying.contains(player)) {
            event.setCancelled(true);
            try {
                double buyAmount = Double.valueOf(event.getMessage());
                if (buyAmount > bitcoinManager.getAmountInBank()) { player.sendMessage(Message.NOT_ENOUGH_IN_BANK.toString().replace("{AMOUNT}", formatRoundNumber(bitcoinManager.getAmountInBank()))); return; }
                if (buyAmount <= 0) { player.sendMessage(Message.INVALID_NUMBER.toString()); return; }
                double cost = (buyAmount * bitcoinManager.getBitcoinValue()) * (1 + bitcoinManager.getPurchaseTaxPercentage() / 100);
                if (cost > plugin.getEconomy().getBalance(player)) { player.sendMessage(Message.NOT_ENOUGH_MONEY.toString()); return; }
                bitcoinManager.deposit(player.getUniqueId(), buyAmount);
                bitcoinManager.removeFromBank(buyAmount);
                player.playSound(player.getLocation(), sounds.getSound("complete_purchase"), 1, 1);
                player.sendMessage(Message.COMPLETE_PURCHASE.toString()
                        .replace("{AMOUNT}", formatNumber(buyAmount))
                        .replace("{COST}", bitcoinManager.getExchangeCurrencySymbol() + formatRound2Number(bitcoinManager.getBitcoinValue() * buyAmount))
                        .replace("{TAX}", bitcoinManager.getExchangeCurrencySymbol() + formatRound2Number(bitcoinManager.getPurchaseTaxPercentage() / 100 * cost)));
                plugin.getEconomy().withdrawPlayer(player, player.getWorld().getName(), cost);
                playersBuying.remove(player);
            } catch (NumberFormatException e) {
                player.sendMessage(Message.INVALID_NUMBER.toString());
            }
        }
    }
}