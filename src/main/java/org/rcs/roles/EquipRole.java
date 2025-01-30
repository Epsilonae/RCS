package org.rcs.roles;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.rcs.config.ConfigManager;

public class EquipRole {

    private final ConfigManager configManager;

    public EquipRole(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void equipRoleToPlayer(Player player, String roleName) {
        Map<String, ItemStack> armor = configManager.getRoleArmor(roleName);
        giveArmorToPlayer(player, armor);

        List<ItemStack> items = configManager.getRoleItems(roleName);
        giveItemsToPlayer(player, items);

        List<PotionEffect> potionEffects = configManager.getRoleEffect(roleName);
        giveEffectToPlayer(player, potionEffects);
    }

    private void giveArmorToPlayer(Player player, Map<String, ItemStack> armor) {
        PlayerInventory inventory = player.getInventory();
        
        if (armor.get("helmet") != null) inventory.setHelmet(armor.get("helmet"));
        if (armor.get("chestplate") != null) inventory.setChestplate(armor.get("chestplate"));
        if (armor.get("leggings") != null) inventory.setLeggings(armor.get("leggings"));
        if (armor.get("boots") != null) inventory.setBoots(armor.get("boots"));
    }

    private void giveItemsToPlayer(Player player, List<ItemStack> items) {
        Inventory inventory = player.getInventory();
        
        for (ItemStack item : items) {
            if (item.getType() != Material.AIR) {
                inventory.addItem(item);
            }
        }
    }

    private void giveEffectToPlayer(Player player, List<PotionEffect> potionEffects) {
        for (PotionEffect potionEffect : potionEffects) {
            if (potionEffect != null) {
                player.addPotionEffect(potionEffect);
            }
        }
    }

}
