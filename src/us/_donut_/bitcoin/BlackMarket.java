package us._donut_.bitcoin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us._donut_.bitcoin.configuration.Message;
import us._donut_.bitcoin.configuration.Sounds;

import java.util.*;

import static us._donut_.bitcoin.util.Util.*;

class BlackMarket implements Listener {

    private Bitcoin plugin = Bitcoin.plugin;
    private BitcoinManager bitcoinManager;
    private Sounds sounds;
    private Inventory blackMarketInterface;
    private Map<Integer, ItemStack> slotItems = new HashMap<>();
    private Map<Integer, Double> slotPrices = new HashMap<>();
    private Map<Integer, Integer> slotStocks = new HashMap<>();

    BlackMarket() {
        bitcoinManager = plugin.getBitcoinManager();
        sounds = plugin.getSounds();
        reload();
    }

    void reload() {
        slotItems.clear();
        slotPrices.clear();
        blackMarketInterface = Bukkit.createInventory(null, 54, Message.BLACK_MARKET_TITLE.toString());
        for (String key : plugin.getBlackMarketConfig().getKeys(false)) {
            int slot = Integer.parseInt(key);
            slotItems.put(slot, plugin.getBlackMarketConfig().getItemStack(key + ".item"));
            slotPrices.put(slot, plugin.getBlackMarketConfig().getDouble(key + ".price"));
            if (plugin.getBlackMarketConfig().contains(key + ".stock")) {
                slotStocks.put(slot, plugin.getBlackMarketConfig().getInt(key + ".stock"));
            }
        }
        for (int slot : slotItems.keySet()) {
            ItemStack displayItem = slotItems.get(slot).clone();
            ItemMeta itemMeta = displayItem.getItemMeta();
            List<String> lore = new ArrayList<>();
            if (itemMeta.getLore() != null) {
                lore.addAll(itemMeta.getLore());
            }
            lore.add(" ");
            lore.add(Message.BLACK_MARKET_ITEM_COST.toString().replace("{COST}", formatNumber(slotPrices.get(slot))));
            if (slotStocks.containsKey(slot)) {
                lore.add(slotStocks.get(slot) > 0 ? Message.BLACK_MARKET_ITEM_IN_STOCK.toString().replace("{AMOUNT}", String.valueOf(slotStocks.get(slot))) : Message.BLACK_MARKET_ITEM_OUT_OF_STOCK.toString());
            } else {
                lore.add(Message.BLACK_MARKET_ITEM_INFINITE_STOCK.toString());
            }
            itemMeta.setLore(lore);
            displayItem.setItemMeta(itemMeta);
            blackMarketInterface.setItem(slot, displayItem);
        }
    }

    void open(Player player) {
        player.openInventory(blackMarketInterface);
    }

    void editItem(int slot, ItemStack itemStack, double price, @Nullable Integer stock) {
        if (itemStack.getType() == Material.AIR) {
            blackMarketInterface.setItem(slot, null);
            slotItems.remove(slot);
            slotPrices.remove(slot);
            plugin.getBlackMarketConfig().set(String.valueOf(slot), null);
        } else {
            slotItems.put(slot, itemStack.clone());
            slotPrices.put(slot, price);
            ItemStack displayItem = itemStack.clone();
            ItemMeta itemMeta = displayItem.getItemMeta();
            List<String> lore = new ArrayList<>();
            if (itemMeta.getLore() != null) {
                lore.addAll(itemMeta.getLore());
            }
            lore.add(" ");
            lore.add(Message.BLACK_MARKET_ITEM_COST.toString().replace("{COST}", formatNumber(price)));
            lore.add(stock == null ? Message.BLACK_MARKET_ITEM_INFINITE_STOCK.toString() : Message.BLACK_MARKET_ITEM_IN_STOCK.toString().replace("{AMOUNT}", String.valueOf(stock)));
            itemMeta.setLore(lore);
            displayItem.setItemMeta(itemMeta);
            blackMarketInterface.setItem(slot, displayItem);
            plugin.getBlackMarketConfig().set(String.valueOf(slot) + ".item", itemStack);
            plugin.getBlackMarketConfig().set(String.valueOf(slot) + ".price", price);
            if (stock != null) {
                slotStocks.put(slot, stock);
                plugin.getBlackMarketConfig().set(String.valueOf(slot) + ".stock", stock);
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveYml(plugin.getBlackMarketFile(), plugin.getBlackMarketConfig()));
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onDragInGUI(InventoryDragEvent event) {
        if (event.getInventory().getName() != null && event.getInventory().getName().equalsIgnoreCase(Message.BLACK_MARKET_TITLE.toString())) {
            event.setCancelled(true);
        } else if (event.getWhoClicked().getOpenInventory() != null && event.getWhoClicked().getOpenInventory().getTitle() != null && event.getWhoClicked().getOpenInventory().getTitle().equalsIgnoreCase(Message.BLACK_MARKET_TITLE.toString())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onMoveInGUI(InventoryMoveItemEvent event) {
        if (event.getDestination().getName() != null && event.getDestination().getName().equalsIgnoreCase(Message.BLACK_MARKET_TITLE.toString())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getName() != null && event.getClickedInventory().getName().equalsIgnoreCase(Message.BLACK_MARKET_TITLE.toString())) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            if (slotItems.containsKey(slot)) {
                if (slotStocks.containsKey(slot) && slotStocks.get(slot) == 0) {
                    player.sendMessage(Message.BLACK_MARKET_OUT_OF_STOCK.toString());
                    player.playSound(player.getLocation(), sounds.getSound("black_market_out_of_stock"), 1, 1);
                    return;
                }
                if (bitcoinManager.getBalance(player.getUniqueId()) > slotPrices.get(slot)) {
                    bitcoinManager.withdraw(player.getUniqueId(), slotPrices.get(slot));
                    bitcoinManager.addToBank(slotPrices.get(slot));
                    player.getInventory().addItem(slotItems.get(slot).clone());
                    player.sendMessage(Message.BLACK_MARKET_PURCHASE.toString().replace("{COST}", formatNumber((slotPrices.get(slot)))));
                    player.playSound(player.getLocation(), sounds.getSound("black_market_purchase"), 1, 1);
                    if (slotStocks.containsKey(slot)) {
                        slotStocks.put(slot, slotStocks.get(slot) - 1);
                        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
                        lore.remove(lore.size() - 1);
                        lore.add(slotStocks.get(slot) > 0 ? Message.BLACK_MARKET_ITEM_IN_STOCK.toString().replace("{AMOUNT}", String.valueOf(slotStocks.get(slot))) : Message.BLACK_MARKET_ITEM_OUT_OF_STOCK.toString());
                        ItemMeta itemMeta = event.getCurrentItem().getItemMeta();
                        itemMeta.setLore(lore);
                        event.getCurrentItem().setItemMeta(itemMeta);
                        plugin.getBlackMarketConfig().set(String.valueOf(slot) + ".stock", slotStocks.get(slot));
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveYml(plugin.getBlackMarketFile(), plugin.getBlackMarketConfig()));
                    }
                } else {
                    player.sendMessage(Message.BLACK_MARKET_NOT_ENOUGH_BITCOINS.toString());
                    player.playSound(player.getLocation(), sounds.getSound("black_market_not_enough_bitcoins"), 1, 1);
                }
            }
        } else if (event.getWhoClicked().getOpenInventory() != null && event.getWhoClicked().getOpenInventory().getTitle() != null && event.getWhoClicked().getOpenInventory().getTitle().equalsIgnoreCase(Message.BLACK_MARKET_TITLE.toString())) {
            event.setCancelled(true);
        }
    }
}