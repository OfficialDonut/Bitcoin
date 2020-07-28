package us.donut.bitcoin.mining;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.donut.bitcoin.Util;
import us.donut.bitcoin.config.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public enum GridType {

    EASY {
        @Override
        public void initGrid(Inventory inventory) {
            super.initGrid(inventory);
            for (int i = 0; i < getGridSlots().length - 1; i++) {
                inventory.setItem(getGridSlots()[i] + 5, getSolution().get(i));
            }
        }
    },

    HARD;

    static {
        EASY.gridSlots = new int[]{0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30};
        EASY.plainGlassSlots = new int[]{4, 13, 22, 31, 40, 39, 38, 37, 36, 41, 42, 43, 44};
        HARD.gridSlots = IntStream.rangeClosed(0, 44).toArray();
        HARD.plainGlassSlots = new int[]{45, 46, 47, 51, 52, 53};

        Material[] easyTileGlass = {Material.WHITE_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE,
                Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE, Material.PINK_STAINED_GLASS_PANE,
                Material.GRAY_STAINED_GLASS_PANE, Material.CYAN_STAINED_GLASS_PANE, Material.PURPLE_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE,
                Material.BROWN_STAINED_GLASS_PANE, Material.GREEN_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE};

        Messages[] easyTileNames = {Messages.WHITE_TILE, Messages.ORANGE_TILE, Messages.MAGENTA_TILE, Messages.LIGHT_BLUE_TILE, Messages.YELLOW_TILE,
                Messages.LIME_TILE, Messages.PINK_TILE, Messages.GRAY_TILE, Messages.CYAN_TILE, Messages.PURPLE_TILE,
                Messages.BLUE_TILE, Messages.BROWN_TILE, Messages.GREEN_TILE, Messages.RED_TILE, Messages.BLACK_TILE};

        for (int i = 0; i < EASY.gridSlots.length - 1; i++) {
            ItemStack itemStack = Util.createItemStack(easyTileGlass[i], easyTileNames[i].toString(), null);
            EASY.grid.add(itemStack);
            EASY.solution.add(itemStack);
        }

        for (int i = 0; i < GridType.HARD.gridSlots.length - 1; i++) {
            Material glass = i % 2 == 0 ? Material.BLUE_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            ItemStack itemStack = Util.createItemStack(glass, i + 1, Util.color("&9&l") + (i + 1), null);
            HARD.grid.add(itemStack);
            HARD.solution.add(itemStack);
        }
    }

    private List<ItemStack> grid = new ArrayList<>();
    private List<ItemStack> solution = new ArrayList<>();
    private int[] gridSlots;
    private int[] plainGlassSlots;

    public List<ItemStack> getSolution() {
        return solution;
    }

    public int[] getGridSlots() {
        return gridSlots;
    }

    public int[] getPlainGlassSlots() {
        return plainGlassSlots;
    }

    public void generate() {
        do {
            Collections.shuffle(grid);
            if (this == EASY) {
                Collections.shuffle(solution);
            }
        } while (!isSolvable());
    }

    public void initGrid(Inventory inventory) {
        inventory.setItem(gridSlots[gridSlots.length - 1], null);
        for (int i = 0; i < gridSlots.length - 1; i++) {
            inventory.setItem(gridSlots[i], grid.get(i));
        }
    }

    public boolean isGridSlot(int slot) {
        for (int gridSlot : gridSlots) {
            if (slot == gridSlot) {
                return true;
            }
        }
        return false;
    }

    public boolean isSolved(Inventory inventory) {
        for (int i = 0; i < gridSlots.length - 1; i++) {
            if (!solution.get(i).equals(inventory.getItem(gridSlots[i]))) {
                return false;
            }
        }
        return true;
    }

    private boolean isSolvable() {
        int inversions = 0;
        for (int i = 0; i < grid.size(); i++) {
            for (int j = i + 1; j < grid.size(); j++) {
                ItemStack tile1 = grid.get(i);
                ItemStack tile2 = grid.get(j);
                if (grid.indexOf(tile1) < grid.indexOf(tile2) != solution.indexOf(tile1) < solution.indexOf(tile2)) {
                    inversions++;
                }
            }
        }
        return inversions % 2 == 0;
    }
}
