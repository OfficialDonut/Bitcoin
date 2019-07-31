package us._donut_.bitcoin.mining;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import us._donut_.bitcoin.Bitcoin;
import us._donut_.bitcoin.Util;
import us._donut_.bitcoin.config.BitcoinConfig;
import us._donut_.bitcoin.config.Messages;

import java.util.*;

public class ComputerManager implements Listener {

    private static ComputerManager instance;
    private Bitcoin plugin;
    private MiningManager miningManager;
    private Map<Player, CommandBlock> computerUsers = new HashMap<>();
    private List<String> recipeRows;
    private NamespacedKey usesLeftKey;
    private NamespacedKey recipeKey;
    private ShapedRecipe computerRecipe;
    private ItemStack computerItem;
    private int computerUses;
    private boolean enabled;

    private ComputerManager() {
        plugin = Bitcoin.getInstance();
        miningManager = MiningManager.getInstance();
        recipeKey = new NamespacedKey(plugin, "computerRecipe");
        usesLeftKey = new NamespacedKey(plugin, "usesLeft");
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
                if (!recipe.getResult().equals(computerItem)) {
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
        computerRecipe = new ShapedRecipe(recipeKey, computerItem = getComputerItem());
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

    public ItemStack getComputerItem(int usesLeft) {
        ItemStack computerItem = Util.createItemStack(Material.COMMAND_BLOCK, Messages.COMPUTER_ITEM_NAME.toString(), Messages.COMPUTER_ITEM_LORE.toString());
        ItemMeta itemMeta = computerItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.getPersistentDataContainer().set(usesLeftKey, PersistentDataType.INTEGER, usesLeft);
            computerItem.setItemMeta(itemMeta);
        }
        return computerItem;
    }

    public ItemStack getComputerItem() {
        ItemStack computerItem = Util.createItemStack(Material.COMMAND_BLOCK, Messages.COMPUTER_ITEM_NAME.toString(), Messages.COMPUTER_ITEM_LORE.toString());
        ItemMeta itemMeta = computerItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.getPersistentDataContainer().set(usesLeftKey, PersistentDataType.INTEGER, computerUses);
            computerItem.setItemMeta(itemMeta);
        }
        return computerItem;
    }

    void potentialUse(Player player) {
        if (enabled) {
            if (computerUsers.containsKey(player)) {
                CommandBlock commandBlock = computerUsers.get(player);
                Integer usesLeft = commandBlock.getPersistentDataContainer().get(usesLeftKey, PersistentDataType.INTEGER);
                if (usesLeft != null && usesLeft > 0) {
                    usesLeft--;
                    if (usesLeft == 0) {
                        commandBlock.getBlock().breakNaturally();
                        player.sendMessage(Messages.COMPUTER_BROKE.toString());
                    } else {
                        commandBlock.getPersistentDataContainer().set(usesLeftKey, PersistentDataType.INTEGER, usesLeft);
                        commandBlock.update();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY) {
            return;
        }
        if (enabled && event.getHand() == EquipmentSlot.HAND) {
            Block block = event.getClickedBlock();
            ItemStack item = event.getItem();
            if (block != null) {
                Player player = event.getPlayer();
                Action action = event.getAction();
                if (block.getType() == Material.COMMAND_BLOCK) {
                    CommandBlock commandBlock = ((CommandBlock) block.getState());
                    Integer usesLeft = commandBlock.getPersistentDataContainer().get(usesLeftKey, PersistentDataType.INTEGER);
                    if (usesLeft != null) {
                        event.setCancelled(true);
                        if (player.isSneaking()) {
                            BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
                            Bukkit.getPluginManager().callEvent(breakEvent);
                            if (!breakEvent.isCancelled()) {
                                block.breakNaturally();
                                if (player.getGameMode() != GameMode.CREATIVE) {
                                    block.getWorld().dropItemNaturally(block.getLocation(), getComputerItem(usesLeft));
                                }
                            }
                        } else if (action == Action.RIGHT_CLICK_BLOCK) {
                            if (computerUsers.containsValue(commandBlock)) {
                                player.sendMessage(Messages.COMPUTER_IN_USE.toString());
                            } else {
                                computerUsers.put(player, commandBlock);
                                Bukkit.getScheduler().runTask(plugin, () -> miningManager.openInterface(player));
                            }
                        } else if (action == Action.LEFT_CLICK_BLOCK) {
                            BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
                            Bukkit.getPluginManager().callEvent(breakEvent);
                            if (!breakEvent.isCancelled()) {
                                if (usesLeft > 0) {
                                    player.sendMessage(Messages.get("computer_left_click", usesLeft));
                                }
                            }
                        }
                    }
                } else if (item != null && computerItem.isSimilar(item) && action == Action.RIGHT_CLICK_BLOCK) {
                    if (!block.getType().isInteractable() || player.isSneaking()) {
                        event.setCancelled(true);
                        Block placedBlock = block.getRelative(event.getBlockFace());
                        BlockPlaceEvent placeEvent = new BlockPlaceEvent(placedBlock, block.getState(), block, item, player, true, event.getHand());
                        Bukkit.getPluginManager().callEvent(placeEvent);
                        if (!placeEvent.isCancelled()) {
                            placedBlock.setType(Material.COMMAND_BLOCK);
                            ItemMeta itemMeta = item.getItemMeta();
                            if (itemMeta != null) {
                                Integer usesLeft = itemMeta.getPersistentDataContainer().get(usesLeftKey, PersistentDataType.INTEGER);
                                CommandBlock commandBlock = (CommandBlock) placedBlock.getState();
                                commandBlock.getPersistentDataContainer().set(usesLeftKey, PersistentDataType.INTEGER, usesLeft == null ? computerUses : usesLeft);
                                commandBlock.update();
                            }
                            player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1, 1);
                            if (player.getGameMode() != GameMode.CREATIVE) {
                                item.setAmount(item.getAmount() - 1);
                            }
                        }
                    }
                }
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
