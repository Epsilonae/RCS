package org.rcs.inventory;

import java.util.List;
import java.util.Random;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.rcs.Main;
import org.rcs.config.ConfigManager;

public class InventoryGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Plugin plugin = Main.getInstance();
    private final ConfigManager config = new ConfigManager();

    public InventoryGUI() {
        this.inventory = plugin.getServer().createInventory(null, 9);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void addItemsToInventory(List<ItemStack> items) {
        Random random = new Random();
        if (!items.isEmpty()) {
            int randomIndex = random.nextInt(items.size());
            inventory.setItem(4, items.get(randomIndex));
        }
    }

    public void initializeInventory() {
        addItemsToInventory(config.getInventoryItems());
    }

}
