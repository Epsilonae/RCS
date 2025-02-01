package org.rcs.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class ConfigManager {

    private static final Logger logger = Logger.getLogger(ConfigManager.class.getName());

    private final String ROLES_PATH = "roles.";
    private final String INVENTORY_PATH = "inventory.";
    private final String VOID_PATH = "void.";
    private final String WORLD_PATH = "world.";
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
        config.set(ROLES_PATH + "example.description", "Role example, can be deleted");
        config.set(ROLES_PATH + "example.armor.helmet", "IRON_HELMET");
        config.set(ROLES_PATH + "example.armor.chestplate", "IRON_CHESTPLATE-PROTECTION:4,UNBREAKING:3");
        config.set(ROLES_PATH + "example.armor.leggings", "");
        config.set(ROLES_PATH + "example.armor.boots", "");
        config.set(ROLES_PATH + "example.items", List.of("IRON_SWORD","SPLASH_POTION:2-STRENGTH:1,8", "GOLDEN_APPLE:5"));
        config.set(ROLES_PATH + "example.effects", List.of("SPEED:1"));

        config.set(INVENTORY_PATH + "interval", 300);
        config.set(INVENTORY_PATH + "items", List.of("DIRT:64"));

        config.set(VOID_PATH + "interval", 30);
        config.set(VOID_PATH + "max", 150);

        config.set(WORLD_PATH + "name", "world");

        config.set(DEATH_MESSAGE_PATH + "suicide", "died by himself");
        config.set(DEATH_MESSAGE_PATH + "messages", List.of("was killed by"));        
        
        saveConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, String.format("Input/output error while saving the configuration file: %s", e.getMessage()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("An error occurred while saving the configuration file: %s", e.getMessage()));
        }
    }

    private List<ItemStack> convertStringsToItems(List<String> itemNames) {
        if (itemNames == null || itemNames.isEmpty()) {
            return Collections.emptyList();
        }

        return itemNames.stream()
                .filter(itemName -> itemName != null && !itemName.isEmpty())
                .map(this::parseItemStack)
                .collect(Collectors.toList());
    }

    private ItemStack parseItemStack(String itemName) {
        String[] parts = itemName.split("-");
        String[] quantityParts = parts[0].split(":");
        String materialName = quantityParts[0].toUpperCase();
        int quantity = parseInteger(quantityParts.length > 1 ? quantityParts[1] : null, 1);

        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            logger.log(Level.WARNING, String.format("%s item is invalid in the configuration file.", materialName));
            return new ItemStack(Material.AIR);
        }

        ItemStack itemStack = new ItemStack(material, quantity);
        ItemMeta meta = itemStack.getItemMeta();

        if (parts.length > 1 && !material.name().contains("POTION")) {
            String[] enchantmentsParts = parts[1].split(",");
            for (String enchantmentEntry : enchantmentsParts) {
                applyEnchantment(meta, enchantmentEntry);
                itemStack.setItemMeta(meta);
            }
        }

        if (material.name().contains("POTION")) {
            PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            PotionEffect potionEffect = parsePotionEffect(parts[1]);
            potionMeta.addCustomEffect(potionEffect, true);
            potionMeta.displayName(Component.text("Potion").color(TextColor.color(0xFFFFFF)));
            itemStack.setItemMeta(potionMeta);
        } 

        return itemStack;
    }

    private void applyEnchantment(ItemMeta meta, String enchantmentEntry) {
        String[] enchantSpecifics = enchantmentEntry.split(":");
        if (enchantSpecifics.length < 2) return;

        @SuppressWarnings("deprecation")
        Enchantment enchantmentType = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantSpecifics[0].toLowerCase()));
        int enchantmentLevel = parseInteger(enchantSpecifics[1], 1);
        if (enchantmentType != null) {
            meta.addEnchant(enchantmentType, enchantmentLevel, true);
        } else {
            logger.log(Level.WARNING, String.format("%s enchantment is invalid.", enchantSpecifics[0]));
        }
    }

    private int parseInteger(String value, int defaultValue) {
        if (value == null) return defaultValue;
        try {
            int parsedValue = Integer.parseInt(value);
            return Math.max(parsedValue, defaultValue);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, String.format("%s is not a valid number. Default value (%s) will be used.", value, defaultValue));
            return defaultValue;
        }
    }

    public List<PotionEffect> convertStringsToPotionEffects(List<String> effectNames) {
        if (effectNames == null || effectNames.isEmpty()) {
            return Collections.emptyList();
        }

        return effectNames.stream()
                .filter(effectName -> effectName != null && !effectName.isEmpty())
                .map(this::parsePotionEffect)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private PotionEffect parsePotionEffect(String effectName) {
        String[] parts = effectName.split(":");
        String effectTypeName = parts[0].toLowerCase();
    
        int amplifier = 0;
        int duration = -1;
    
        if (parts.length > 1) {
            String[] effectParams = parts[1].split(",");
            
            amplifier = parseInteger(effectParams[0], 0) - 1;
    
            if (effectParams.length > 1) {
                duration = parseInteger(effectParams[1], -1) * 20;
            }
        }
    
        PotionEffectType effectType = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(effectTypeName));
        if (effectType == null) {
            logger.log(Level.WARNING, String.format("%s potion effect is invalid in the configuration file.", effectTypeName));
            return null;
        }
    
        return new PotionEffect(effectType, duration, amplifier);
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

    public Map<String, ItemStack> getRoleArmor(String roleName) {
        Map<String, String> armorPaths = new HashMap<>();
        armorPaths.put("helmet", ROLES_PATH + roleName + ".armor.helmet");
        armorPaths.put("chestplate", ROLES_PATH + roleName + ".armor.chestplate");
        armorPaths.put("leggings", ROLES_PATH + roleName + ".armor.leggings");
        armorPaths.put("boots", ROLES_PATH + roleName + ".armor.boots");
    
        Map<String, ItemStack> armorMap = new HashMap<>();
    
        for (Map.Entry<String, String> entry : armorPaths.entrySet()) {
            String armorType = entry.getKey();
            String path = entry.getValue();
            String configValue = config.getString(path);
    
            if (configValue == null || configValue.isEmpty()) {
                armorMap.put(armorType, new ItemStack(Material.AIR));
            } else {
                ItemStack item = convertStringsToItems(Collections.singletonList(configValue)).get(0);
                armorMap.put(armorType, item);
            }
        }
    
        return armorMap;
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

    public String getWorldName() {
        return config.getString(WORLD_PATH + "name");
    }
    
}
