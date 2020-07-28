package us.donut.bitcoin.mining;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import us.donut.bitcoin.Bitcoin;
import us.donut.bitcoin.Util;
import us.donut.bitcoin.config.BitcoinConfig;
import us.donut.bitcoin.config.Messages;

import java.util.*;

public class ComputerManager implements Listener {

    private static ComputerManager instance;
    private Bitcoin plugin;
    private MiningManager miningManager;
    private Map<Player, ItemStack> computerUsers = new HashMap<>();
    private List<String> recipeRows;
    private NamespacedKey computerIDKey;
    private NamespacedKey usesLeftKey;
    private NamespacedKey recipeKey;
    private ShapedRecipe computerRecipe;
    private int computerUses;
    private boolean enabled;

    private ComputerManager() {
        plugin = Bitcoin.getInstance();
        miningManager = MiningManager.getInstance();
        computerIDKey = new NamespacedKey(plugin, "computerID");
        usesLeftKey = new NamespacedKey(plugin, "usesLeft");
        recipeKey = new NamespacedKey(plugin, "computerRecipe");
    }

    public static ComputerManager getInstance() {
        return instance != null ? instance : (instance = new ComputerManager());
    }

    public void reload() {
        enabled = BitcoinConfig.getBoolean("computers");
        unregisterRecipe();
        if (enabled) {
            computerUses = BitcoinConfig.getInt("computer_uses_before_break");
            registerRecipe();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getRecipeString() {
        return String.join("\n", recipeRows).toLowerCase().replace("_", " ");
    }

    private void unregisterRecipe() {
        if (computerRecipe != null) {
            List<Recipe> recipeList = new ArrayList<>();
            Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
            while (recipeIterator.hasNext()) {
                Recipe recipe = recipeIterator.next();
                if (!recipe.equals(computerRecipe)) {
                    recipeList.add(recipe);
                }
            }
            Bukkit.clearRecipes();
            for (Recipe recipe : recipeList) {
                Bukkit.addRecipe(recipe);
            }
        }
    }

    private void registerRecipe() {
        computerRecipe = new ShapedRecipe(recipeKey, getComputerItem());
        computerRecipe.shape("ABC", "DEF", "GHI");
        int i = 0;
        for (String recipeRow : recipeRows = BitcoinConfig.getStringList("computer_recipe")) {
            for (String material : recipeRow.split(",")) {
                Material ingredient = Material.valueOf(material.trim().toUpperCase());
                if (ingredient != Material.AIR) {
                    computerRecipe.setIngredient("ABCDEFGHI".charAt(i), ingredient);
                }
                i++;
            }
        }
        Bukkit.addRecipe(computerRecipe);
    }

    public ItemStack getComputerItem() {
        ItemStack computerItem = Util.createItemStack(Material.COMMAND_BLOCK, Messages.COMPUTER_ITEM_NAME.toString(), Messages.get("computer_item_lore", computerUses));
        ItemMeta itemMeta = computerItem.getItemMeta();
        itemMeta.getPersistentDataContainer().set(computerIDKey, PersistentDataType.STRING, UUID.randomUUID().toString());
        itemMeta.getPersistentDataContainer().set(usesLeftKey, PersistentDataType.INTEGER, computerUses);
        computerItem.setItemMeta(itemMeta);
        return computerItem;
    }

    void potentialUse(Player player) {
        if (enabled && computerUsers.containsKey(player)) {
            ItemStack computerItem = computerUsers.get(player);
            int usesLeft = computerItem.getItemMeta().getPersistentDataContainer().getOrDefault(usesLeftKey, PersistentDataType.INTEGER, 1) - 1;
            if (usesLeft == 0) {
                player.getInventory().remove(computerItem);
                player.sendMessage(Messages.COMPUTER_BROKE.toString());
            } else {
                ItemMeta itemMeta = computerItem.getItemMeta();
                itemMeta.getPersistentDataContainer().set(usesLeftKey, PersistentDataType.INTEGER, usesLeft);
                itemMeta.setLore(Arrays.asList(Messages.get("computer_item_lore", usesLeft).split("\n")));
                computerItem.setItemMeta(itemMeta);
            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(computerIDKey, PersistentDataType.STRING)) {
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.getPersistentDataContainer().set(computerIDKey, PersistentDataType.STRING, UUID.randomUUID().toString());
            item.setItemMeta(itemMeta);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (enabled && event.getHand() == EquipmentSlot.HAND && event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(computerIDKey, PersistentDataType.STRING)) {
                Player player = event.getPlayer();
                computerUsers.put(player, item);
                Bukkit.getScheduler().runTask(plugin, () -> miningManager.openInterface(player));
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (enabled && miningManager.isMiningInterface(event.getInventory()) && event.getPlayer() instanceof Player) {
            computerUsers.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (enabled) {
            computerUsers.remove(event.getPlayer());
        }
    }
}
