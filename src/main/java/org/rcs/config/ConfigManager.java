package org.rcs.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ConfigManager {

    private static final Logger logger = Logger.getLogger(ConfigManager.class.getName());

    private final String ROLES_PATH = "roles.";
    private final String INVENTORY_PATH = "inventory.";
    private final String VOID_PATH = "void.";
    private final String DEATH_MESSAGE_PATH = "death_message.";
    private final String EMPTY_MESSAGE = "None. Please configure them in config.yml.";

    private final File configFile;
    private final FileConfiguration config;

    public ConfigManager() {
        this.configFile = new File("plugins/RCS", "config.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void initializeConfig() {
        if (!configFile.exists()) {
            createDefaultConfig();
        }
    }

    private void createDefaultConfig() {
        config.set(ROLES_PATH + "example.description", "Role example");
        config.set(ROLES_PATH + "example.armor.helmet", "IRON_HELMET");
        config.set(ROLES_PATH + "example.armor.chestplate", "IRON_CHESTPLATE");
        config.set(ROLES_PATH + "example.armor.leggings", "");
        config.set(ROLES_PATH + "example.armor.boots", "");
        config.set(ROLES_PATH + "example.items", List.of("IRON_SWORD:1", "GOLDEN_APPLE:5"));
        config.set(ROLES_PATH + "example.effects", List.of("SPEED:1"));

        config.set(INVENTORY_PATH + "interval", 300);
        config.set(INVENTORY_PATH + "items", List.of("DIRT:64"));

        config.set(VOID_PATH + "interval", 30);
        config.set(VOID_PATH + "max", 150);

        config.set(DEATH_MESSAGE_PATH + "suicide", "died by himself");
        config.set(DEATH_MESSAGE_PATH + "messages", List.of("was killed by"));
        
        saveConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Input/output error while saving the configuration file: {0}", e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while saving the configuration file: {0}", e.getMessage());
        }
    }

    private List<ItemStack> convertStringsToItems(List<String> itemNames) {
        List<ItemStack> items = new ArrayList<>();
        for (String itemName : itemNames) {
            if (itemName == null || itemName.isEmpty()) {
                continue;
            }
    
            String[] parts = itemName.split(":");
            String materialName = parts[0];
            int quantity = 1;
    
            if (parts.length > 1) {
                try {
                    quantity = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    logger.log(Level.WARNING, "Quantity '{0}' for the item '{1}' is invalid. Default quantity (1) will be used.", new Object[]{parts[1], materialName});
                }
            }
    
            Material material = Material.matchMaterial(materialName.toUpperCase());
            if (material == null) {
                logger.log(Level.WARNING, "'{0}' item is invalid in the configuration file.", materialName);
                items.add(new ItemStack(Material.AIR));
            } else {
                items.add(new ItemStack(material, quantity));
            }
        }
        return items;
    }

    public List<PotionEffect> convertStringsToPotionEffects(List<String> effectNames) {
        List<PotionEffect> potionEffects = new ArrayList<>();
        for (String effectName : effectNames) {
            if (effectName == null || effectName.isEmpty()) {
                continue;
            }

            String[] parts = effectName.split(":");
            String effectTypeName = parts[0];
            int amplifier = 0;

            if (parts.length > 1) {
                try {
                    amplifier = Integer.parseInt(parts[1])-1;
                } catch (NumberFormatException e) {
                    logger.log(Level.WARNING, "'{0}' amplifier for the effect '{1}' is invalid. Default amplifier (0) will be used.", new Object[]{parts[1], effectTypeName});
                }
            }

            PotionEffectType effectType = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(effectTypeName.toLowerCase()));
            if (effectType == null) {
                logger.log(Level.WARNING, "'{0}' potion effect is invalid in the configuration file.", effectTypeName);
            } else {
                potionEffects.add(new PotionEffect(effectType, -1, amplifier));
            }
        }
        return potionEffects;
    }

    public int getSize() {
        int roleSize = getRoles().size();
        if (getRoles().contains(EMPTY_MESSAGE)) {
            roleSize--;
        }
        return roleSize;
    }

    public List<String> getRoles() {
        List<String> roleNames = new ArrayList<>();
    
        if (config.contains("roles")) {
            ConfigurationSection rolesSection = config.getConfigurationSection("roles");
    
            if (rolesSection != null) {
                for (String roleName : rolesSection.getKeys(false)) {
                    if (!roleNames.contains(roleName) && !roleName.equalsIgnoreCase("example")) {
                        roleNames.add(roleName);
                    }
                }
            }
        }

        if (roleNames.isEmpty()) {
            roleNames.add(EMPTY_MESSAGE);
        }

        return roleNames;
    }

    public String getRoleDescription(String roleName) {
        return config.getString(ROLES_PATH + roleName + ".description");
    }

    private ItemStack getItemFromConfig(String path) {
        String materialName = config.getString(path);
        if (materialName == null || materialName.isEmpty()) {
            return new ItemStack(Material.AIR);
        }

        Material material = Material.matchMaterial(materialName.toUpperCase());
        if (material == null) {
            logger.log(Level.WARNING, "'{0}' item is invalid in the configuration file.", materialName);
            return new ItemStack(Material.AIR);
        }

        return new ItemStack(material);
    }

    public Map<String, ItemStack> getRoleArmor(String roleName) {
        Map<String, ItemStack> armor = new HashMap<>();
        armor.put("helmet", getItemFromConfig(ROLES_PATH + roleName + ".armor.helmet"));
        armor.put("chestplate", getItemFromConfig(ROLES_PATH + roleName + ".armor.chestplate"));
        armor.put("leggings", getItemFromConfig(ROLES_PATH + roleName + ".armor.leggings"));
        armor.put("boots", getItemFromConfig(ROLES_PATH + roleName + ".armor.boots"));
        return armor;
    }

    public List<ItemStack> getRoleItems(String roleName) {
        List<String> itemNames = config.getStringList(ROLES_PATH + roleName + ".items");
        return convertStringsToItems(itemNames);
    }

    public List<PotionEffect> getRoleEffect(String roleName) {
        List<String> potionEffects = config.getStringList(ROLES_PATH + roleName + ".effects");
        return convertStringsToPotionEffects(potionEffects);
    }

    public int getInventoryInterval() {
        int interval = config.getInt(INVENTORY_PATH + "interval");
        if (interval <= 15) {
            return 15;
        }
        return interval;
    }

    public List<ItemStack> getInventoryItems() {
        List<String> itemNames = config.getStringList(INVENTORY_PATH + "items");
        return convertStringsToItems(itemNames);
    }

    public int getVoidInterval() {
        int interval = config.getInt(VOID_PATH + "interval");
        if (interval <= 15) {
            return 15;
        }
        return interval;
    }

    public int getVoidMax() {
        int max = config.getInt(VOID_PATH + "max");
        if (max >= 319) {
            return 319;
        }
        return max;
    }

    public String getSuicideMessage() {
        return config.getString(DEATH_MESSAGE_PATH + "suicide");
    }

    public String getRandomDeathMessage() {
        List<String> deathMessages = config.getStringList(DEATH_MESSAGE_PATH + "messages");
        
        Random random = new Random();
        int randomIndex = random.nextInt(deathMessages.size());
        if (deathMessages.get(randomIndex).equalsIgnoreCase("")) {
            return "was killed by";
        }
        return deathMessages.get(randomIndex);
    }
    
}
