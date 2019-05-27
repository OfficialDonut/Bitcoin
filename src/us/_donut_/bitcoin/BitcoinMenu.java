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
import org.bukkit.inventory.meta.SkullMeta;
import us._donut_.bitcoin.config.Messages;
import us._donut_.bitcoin.config.Sounds;
import us._donut_.bitcoin.hooks.ServerEconomy;
import us._donut_.bitcoin.mining.ComputerManager;
import us._donut_.bitcoin.mining.MiningManager;

import java.util.*;

public class BitcoinMenu implements Listener {

    private static BitcoinMenu instance;
    private Bitcoin plugin;
    private BitcoinManager bitcoinManager;
    private PlayerDataManager playerDataManager;
    private Map<Player, Inventory> menus = new HashMap<>();
    private Map<ItemStack, String> itemPermissions = new HashMap<>();
    private List<Player> playersExchanging = new ArrayList<>();
    private List<Player> playersTransferring = new ArrayList<>();
    private List<Player> playersBuying = new ArrayList<>();
    private int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
    private ItemStack darkBlueGlass;
    private ItemStack lightBlueGlass;
    private ItemStack transferBitcoinItem;
    private ItemStack exchangeBitcoinItem;
    private ItemStack buyBitcoinItem;
    private ItemStack miningBitcoinItem;
    private ItemStack helpItem;
    private ItemStack blackMarketItem;

    private BitcoinMenu() {
        plugin = Bitcoin.getInstance();
        bitcoinManager = BitcoinManager.getInstance();
        playerDataManager = PlayerDataManager.getInstance();
        runGlassAnimation();
    }

    public static BitcoinMenu getInstance() {
        return instance != null ? instance : (instance = new BitcoinMenu());
    }

    void reload() {
        darkBlueGlass = Util.createItemStack(Material.BLUE_STAINED_GLASS_PANE, " ", null);
        lightBlueGlass = Util.createItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ", null);
        transferBitcoinItem = Util.createItemStack(Material.WRITABLE_BOOK, Messages.TRANSFER_ITEM_NAME.toString(), Messages.TRANSFER_ITEM_LORE.toString());
        exchangeBitcoinItem = Util.createItemStack(Material.GOLD_INGOT, Messages.EXCHANGE_ITEM_NAME.toString(), Messages.EXCHANGE_ITEM_LORE.toString());
        buyBitcoinItem = Util.createItemStack(Material.EMERALD, Messages.BUY_ITEM_NAME.toString(), Messages.BUY_ITEM_LORE.toString());
        miningBitcoinItem = Util.createItemStack(Material.DIAMOND_PICKAXE, Messages.MINING_ITEM_NAME.toString(), Messages.MINING_ITEM_LORE.toString());
        ItemMeta itemMeta = miningBitcoinItem.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS);
        miningBitcoinItem.setItemMeta(itemMeta);
        helpItem = Util.createItemStack(Material.PAPER, Messages.HELP_ITEM_NAME.toString(), Messages.HELP_ITEM_LORE.toString());
        blackMarketItem = Util.createItemStack(Material.ENDER_CHEST, Messages.BLACK_MARKET_ITEM_NAME.toString(), Messages.BLACK_MARKET_ITEM_LORE.toString());

        itemPermissions.put(transferBitcoinItem, "bitcoin.gui.transfer");
        itemPermissions.put(exchangeBitcoinItem, "bitcoin.gui.sell");
        itemPermissions.put(buyBitcoinItem, "bitcoin.gui.buy");
        itemPermissions.put(miningBitcoinItem, "bitcoin.gui.mine");
        itemPermissions.put(helpItem, "bitcoin.gui.help");
        itemPermissions.put(blackMarketItem, "bitcoin.gui.blackmarket");

        Util.closeInventories(menus);
        menus.clear();
    }

    private void runGlassAnimation() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> menus.values().forEach(menu -> {
            for (int slot : borderSlots) {
                menu.setItem(slot, darkBlueGlass.equals(menu.getItem(slot)) ? lightBlueGlass : darkBlueGlass);
            }
        }),0, 5);
    }

    private boolean isBitcoinMenu(Inventory inventory) {
        return menus.values().contains(inventory);
    }

    public void open(Player player) {
        Inventory menu = menus.computeIfAbsent(player, this::createMenu);
        menus.get(player).setItem(10, getSkull(player));
        player.openInventory(menu);
    }

    private Inventory createMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, Messages.MENU_TITLE.toString());
        menu.setItem(10, getSkull(player));
        menu.setItem(11, transferBitcoinItem);
        menu.setItem(12, buyBitcoinItem);
        menu.setItem(13, exchangeBitcoinItem);
        menu.setItem(14, miningBitcoinItem);
        menu.setItem(15, blackMarketItem);
        menu.setItem(16, helpItem);
        for (int slot : borderSlots) {
            menu.setItem(slot, slot % 2 == 0 ? darkBlueGlass : lightBlueGlass);
        }
        return menu;
    }

    private ItemStack getSkull(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack skull = Util.createItemStack(Material.PLAYER_HEAD, Messages.STATISTIC_ITEM_NAME.toString(), Messages.get("statistic_item_lore",
                bitcoinManager.format(playerDataManager.getBalance(uuid)),
                Util.formatNumber(playerDataManager.getPuzzlesSolved(uuid)),
                bitcoinManager.format(playerDataManager.getBitcoinsMined(uuid)),
                String.valueOf(playerDataManager.getBestPuzzleTime(uuid) / 60.0).split("\\.")[0],
                String.valueOf(playerDataManager.getBestPuzzleTime(uuid) % 60)));
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
        }
        skull.setItemMeta(skullMeta);
        return skull;
    }

    private void sendCancelButton(Player player) {
        TextComponent cancelButton = new TextComponent(TextComponent.fromLegacyText(Messages.CANCEL_BUTTON.toString()));
        cancelButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bitcoin cancel"));
        cancelButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.CANCEL_BUTTON_HOVER.toString()).create()));
        player.spigot().sendMessage(cancelButton);
    }

    void cancelAction(Player player) {
        if (playersExchanging.contains(player)) {
            playersExchanging.remove(player);
            player.sendMessage(Messages.CANCELLED_EXCHANGE.toString());
            player.playSound(player.getLocation(), Sounds.get("cancelled_exchange"), 1, 1);
        } else if (playersTransferring.contains(player)) {
            playersTransferring.remove(player);
            player.sendMessage(Messages.CANCELLED_TRANSFER.toString());
            player.playSound(player.getLocation(), Sounds.get("cancelled_transfer"), 1, 1);
        } else if (playersBuying.contains(player)) {
            playersBuying.remove(player);
            player.sendMessage(Messages.CANCELLED_PURCHASE.toString());
            player.playSound(player.getLocation(), Sounds.get("cancelled_purchase"), 1, 1);
        } else {
            player.sendMessage(Messages.NOTHING_TO_CANCEL.toString());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        menus.remove(event.getPlayer());
        playersExchanging.remove(event.getPlayer());
        playersTransferring.remove(event.getPlayer());
        playersBuying.remove(event.getPlayer());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().startsWith("/bitcoin cancel")) {
            if (playersExchanging.contains(event.getPlayer()) || playersTransferring.contains(event.getPlayer()) || playersBuying.contains(event.getPlayer())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Messages.CANNOT_USE_COMMANDS.toString());
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playersExchanging.contains(player)) {
            event.setCancelled(true);
            playersExchanging.remove(player);
            Bukkit.getScheduler().runTask(plugin, () -> player.performCommand("bitcoin sell " + event.getMessage()));
        } else if (playersTransferring.contains(player)) {
            event.setCancelled(true);
            playersTransferring.remove(player);
            if (event.getMessage().split(" ").length != 2) {
                player.sendMessage(Messages.INVALID_ENTRY.toString());
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> player.performCommand("bitcoin transfer " + event.getMessage()));
            }
        } else if (playersBuying.contains(player)) {
            event.setCancelled(true);
            playersBuying.remove(player);
            Bukkit.getScheduler().runTask(plugin, () -> player.performCommand("bitcoin buy " + event.getMessage()));
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isBitcoinMenu(event.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (isBitcoinMenu(event.getDestination()) || isBitcoinMenu(event.getSource())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && isBitcoinMenu(event.getClickedInventory()) && event.getWhoClicked() instanceof Player) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();

            if (itemPermissions.containsKey(item)) {
                if (!player.hasPermission(itemPermissions.get(item))) {
                    player.sendMessage(Messages.NO_PERMISSION.toString());
                    return;
                }
            }

            if (transferBitcoinItem.equals(item) || buyBitcoinItem.equals(item) || exchangeBitcoinItem.equals(item)) {
                if (transferBitcoinItem.equals(item)) {
                    playersTransferring.add(player);
                    player.playSound(player.getLocation(), Sounds.get("click_transfer_item"), 1, 1);
                    player.sendMessage(Messages.get("begin_transfer", bitcoinManager.format(playerDataManager.getBalance(player.getUniqueId()))));
                } else {
                    if (!ServerEconomy.isPresent()) {
                        player.playSound(player.getLocation(), Sounds.get("no_economy"), 1, 1);
                        player.sendMessage(Messages.NO_ECONOMY.toString());
                        return;
                    }
                    if (buyBitcoinItem.equals(item)) {
                        playersBuying.add(player);
                        player.playSound(player.getLocation(), Sounds.get("click_buy_item"), 1, 1);
                        player.sendMessage(Messages.get("begin_purchase", bitcoinManager.format((bitcoinManager.getAmountInBank())), bitcoinManager.getFormattedValue(), bitcoinManager.getPurchaseTaxPercentage() + "%"));
                    } else if (exchangeBitcoinItem.equals(item)) {
                        playersExchanging.add(player);
                        player.playSound(player.getLocation(), Sounds.get("click_exchange_item"), 1, 1);
                        player.sendMessage(Messages.get("begin_exchange", bitcoinManager.format(playerDataManager.getBalance(player.getUniqueId())), bitcoinManager.getFormattedValue()));
                    }
                }
                Bukkit.getScheduler().runTask(plugin, player::closeInventory);
                sendCancelButton(player);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if ((transferBitcoinItem.equals(item) ? playersTransferring : buyBitcoinItem.equals(item) ? playersBuying : playersExchanging).contains(player)) {
                        player.performCommand("bitcoin cancel");
                    }
                }, 300);
                return;
            }

            if (miningBitcoinItem.equals(item)) {
                ComputerManager computerManager = ComputerManager.getInstance();
                if (computerManager.isEnabled()) {
                    Bukkit.getScheduler().runTask(plugin, player::closeInventory);
                    player.sendMessage(Messages.get("computer_help", ComputerManager.getInstance().getRecipeString()));
                } else {
                    player.playSound(player.getLocation(), Sounds.get("click_mining_item"), 1, 1);
                    MiningManager.getInstance().openInterface(player);
                }
            } else if (blackMarketItem.equals(item)) {
                player.playSound(player.getLocation(), Sounds.get("click_black_market_item"), 1, 1);
                BlackMarket.getInstance().open(player);
            } else if (helpItem.equals(item)) {
                Bukkit.getScheduler().runTask(plugin, player::closeInventory);
                player.playSound(player.getLocation(), Sounds.get("click_help_item"), 1, 1);
                player.sendMessage(Messages.HELP_COMMAND.toString());
            }
        }
    }
}
