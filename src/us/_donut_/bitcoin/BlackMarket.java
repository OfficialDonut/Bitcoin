package us._donut_.bitcoin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

class BlackMarket implements Listener {

    private Bitcoin plugin;
    private Util util;
    private BitcoinManager bitcoinManager;
    private Messages messages;
    private Sounds sounds;
    private Inventory blackMarketInterface;
    private Map<Integer, ItemStack> slotItems = new HashMap<>();
    private Map<Integer, Double> slotPrices = new HashMap<>();

    BlackMarket(Bitcoin plugin) {
        this.plugin = plugin;
        util = plugin.getUtil();
        bitcoinManager = plugin.getBitcoinManager();
        messages = plugin.getMessages();
        sounds = plugin.getSounds();
        reload();
    }

    void reload() {
        slotItems.clear();
        slotPrices.clear();
        blackMarketInterface = Bukkit.createInventory(null, 54, messages.getMessage("black_market_title"));
        for (String key : plugin.getBlackMarketConfig().getKeys(false)) {
            int slot = Integer.parseInt(key);
            slotItems.put(slot, plugin.getBlackMarketConfig().getItemStack(key + ".item"));
            slotPrices.put(slot, plugin.getBlackMarketConfig().getDouble(key + ".price"));
        }
        for (int slot : slotItems.keySet()) {
            ItemStack displayItem = slotItems.get(slot).clone();
            ItemMeta itemMeta = displayItem.getItemMeta();
            List<String> lore = new ArrayList<>();
            if (itemMeta.getLore() != null) {
                lore.addAll(itemMeta.getLore());
            }
            lore.add(" ");
            lore.add(messages.getMessage("black_market_item_cost").replace("{COST}", util.formatNumber(slotPrices.get(slot))));
            itemMeta.setLore(lore);
            displayItem.setItemMeta(itemMeta);
            blackMarketInterface.setItem(slot, displayItem);
        }
    }

    void open(Player player) {
        player.openInventory(blackMarketInterface);
    }

    void editItem(int slot, ItemStack itemStack, double price) {
        if (itemStack.getType() == Material.AIR) {
            blackMarketInterface.setItem(slot, null);
            slotItems.remove(slot);
            slotPrices.remove(slot);
            plugin.getBlackMarketConfig().set(String.valueOf(slot), null);
        } else {
            slotItems.put(slot, itemStack);
            slotPrices.put(slot, price);
            ItemStack displayItem = itemStack.clone();
            ItemMeta itemMeta = displayItem.getItemMeta();
            List<String> lore = new ArrayList<>();
            if (itemMeta.getLore() != null) {
                lore.addAll(itemMeta.getLore());
            }
            lore.add(" ");
            lore.add(messages.getMessage("black_market_item_cost").replace("{COST}", util.formatNumber(price)));
            itemMeta.setLore(lore);
            displayItem.setItemMeta(itemMeta);
            blackMarketInterface.setItem(slot, displayItem);
            plugin.getBlackMarketConfig().set(String.valueOf(slot) + ".item", itemStack);
            plugin.getBlackMarketConfig().set(String.valueOf(slot) + ".price", price);
        }
        util.saveYml(plugin.getBlackMarketFile(), plugin.getBlackMarketConfig());
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onDragInGUI(InventoryDragEvent event) {
        if (event.getInventory().getName() != null && event.getInventory().getName().equalsIgnoreCase(messages.getMessage("black_market_title"))) { event.setCancelled(true); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onMoveInGUI(InventoryMoveItemEvent event) {
        if (event.getDestination().getName() != null && event.getDestination().getName().equalsIgnoreCase(messages.getMessage("black_market_title"))) { event.setCancelled(true); }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getName() != null && event.getClickedInventory().getName().equalsIgnoreCase(messages.getMessage("black_market_title"))) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            if (slotItems.containsKey(slot)) {
                if (bitcoinManager.getBalance(player.getUniqueId()) > slotPrices.get(slot)) {
                    bitcoinManager.withdraw(player.getUniqueId(), slotPrices.get(slot));
                    bitcoinManager.addToBank(slotPrices.get(slot));
                    player.getInventory().addItem(slotItems.get(slot).clone());
                    player.sendMessage(messages.getMessage("black_market_purchase").replace("{COST}", util.formatNumber((slotPrices.get(slot)))));
                    player.playSound(player.getLocation(), sounds.getSound("black_market_purchase"), 1, 1);
                } else {
                    player.sendMessage(messages.getMessage("black_market_not_enough_bitcoins"));
                    player.playSound(player.getLocation(), sounds.getSound("black_market_not_enough_bitcoins"), 1, 1);
                }
            }
        }
    }
}