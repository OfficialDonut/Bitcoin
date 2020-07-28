package us.donut.bitcoin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.donut.bitcoin.config.Messages;
import us.donut.bitcoin.config.Sounds;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlackMarket implements Listener {

    private static BlackMarket instance;
    private Bitcoin plugin;
    private BitcoinManager bitcoinManager;
    private PlayerDataManager playerDataManager;
    private File blackMarketFile;
    private YamlConfiguration blackMarketConfig;
    private Inventory blackMarketInterface;
    private Map<Integer, ItemStack> slotItems = new HashMap<>();
    private Map<Integer, Double> slotPrices = new HashMap<>();
    private Map<Integer, Integer> slotStocks = new HashMap<>();

    private BlackMarket() {
        plugin = Bitcoin.getInstance();
        bitcoinManager = BitcoinManager.getInstance();
        playerDataManager = PlayerDataManager.getInstance();
        blackMarketFile = new File(plugin.getDataFolder(), "black_market.yml");
        blackMarketConfig = YamlConfiguration.loadConfiguration(blackMarketFile);
        if (!blackMarketFile.exists()) {
            Util.saveYml(blackMarketFile, blackMarketConfig);
            plugin.getLogger().info("Generated black_market.yml!");
        }
    }

    public static BlackMarket getInstance() {
        return instance != null ? instance : (instance = new BlackMarket());
    }

    public void reload() {
        blackMarketInterface = Bukkit.createInventory(null, 54, Messages.BLACK_MARKET_TITLE.toString());
        for (String key : blackMarketConfig.getKeys(false)) {
            int slot = Integer.parseInt(key);
            slotItems.put(slot, blackMarketConfig.getItemStack(key + ".item"));
            slotPrices.put(slot, blackMarketConfig.getDouble(key + ".price"));
            if (blackMarketConfig.contains(key + ".stock")) {
                slotStocks.put(slot, blackMarketConfig.getInt(key + ".stock"));
            }
            blackMarketInterface.setItem(slot, getDisplayItem(slot));
        }
    }

    public void open(Player player) {
        player.openInventory(blackMarketInterface);
    }

    private ItemStack getDisplayItem(int slot) {
        ItemStack displayItem = slotItems.get(slot).clone();
        ItemMeta itemMeta = displayItem.getItemMeta();
        if (itemMeta != null) {
            List<String> lore = new ArrayList<>();
            if (itemMeta.getLore() != null) {
                lore.addAll(itemMeta.getLore());
                lore.add(" ");
            }
            lore.add(Messages.get("black_market_item_cost", Util.formatNumber(slotPrices.get(slot))));
            int stock = slotStocks.getOrDefault(slot, -1);
            lore.add(stock > 0 ? Messages.get("black_market_item_in_stock", stock) : stock == 0 ? Messages.BLACK_MARKET_ITEM_OUT_OF_STOCK.toString() : Messages.BLACK_MARKET_ITEM_INFINITE_STOCK.toString());
            itemMeta.setLore(lore);
            displayItem.setItemMeta(itemMeta);
        }
        return displayItem;
    }

    public void setItem(int slot, ItemStack item, double price, int stock) {
        if (item == null || item.getType() == Material.AIR) {
            blackMarketInterface.setItem(slot, null);
            slotItems.remove(slot);
            slotPrices.remove(slot);
            slotStocks.remove(slot);
            blackMarketConfig.set(String.valueOf(slot), null);
        } else {
            item = item.clone();
            slotItems.put(slot, item);
            slotPrices.put(slot, price);
            blackMarketConfig.set(slot + ".item", item);
            blackMarketConfig.set(slot + ".price", price);
            if (stock > 0) {
                slotStocks.put(slot, stock);
                blackMarketConfig.set(slot + ".stock", stock);
            }
            blackMarketInterface.setItem(slot, getDisplayItem(slot));
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Util.saveYml(blackMarketFile, blackMarketConfig));
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (blackMarketInterface.equals(event.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (blackMarketInterface.equals(event.getDestination()) || blackMarketInterface.equals(event.getSource())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && blackMarketInterface.equals(event.getClickedInventory())) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();
                int slot = event.getSlot();
                if (slotItems.containsKey(slot)) {
                    ItemStack item = slotItems.get(slot);
                    double price = slotPrices.get(slot);
                    int stock = slotStocks.getOrDefault(slot, -1);
                    if (stock == 0) {
                        player.sendMessage(Messages.BLACK_MARKET_OUT_OF_STOCK.toString());
                        player.playSound(player.getLocation(), Sounds.get("black_market_out_of_stock"), 1, 1);
                        return;
                    }
                    if (playerDataManager.getBalance(player.getUniqueId()) < price) {
                        player.sendMessage(Messages.BLACK_MARKET_NOT_ENOUGH_BITCOINS.toString());
                        player.playSound(player.getLocation(), Sounds.get("black_market_not_enough_bitcoins"), 1, 1);
                        return;
                    }
                    playerDataManager.withdraw(player.getUniqueId(), price);
                    bitcoinManager.addToBank(price);
                    Bukkit.getScheduler().runTask(plugin, () -> player.getInventory().addItem(item.clone()));
                    player.sendMessage(Messages.get("black_market_purchase", Util.formatNumber(price)));
                    player.playSound(player.getLocation(), Sounds.get("black_market_purchase"), 1, 1);
                    if (stock > 0) {
                        stock--;
                        slotStocks.put(slot, stock);
                        blackMarketConfig.set(slot + ".stock", stock);
                        Bukkit.getScheduler().runTask(plugin, () -> blackMarketInterface.setItem(slot, getDisplayItem(slot)));
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Util.saveYml(blackMarketFile, blackMarketConfig));
                    }
                }
            }
        }
    }
}
