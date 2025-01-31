package org.rcs.common;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.rcs.config.ConfigManager;

public class MapVoid {

    private final int VOID_HEIGHT_OFFSET = 65;
    
    private final World world;
    private final ConfigManager config = new ConfigManager();
    private final int RESET_HEIGHT = -128;

    private int currentHeight;

    public MapVoid() {
        this.world = Bukkit.getWorld(config.getWorldName());
        this.currentHeight = RESET_HEIGHT + VOID_HEIGHT_OFFSET;

        if (world != null) {
            world.setVoidDamageAmount(1.0f);
        } else {
            throw new IllegalStateException("The world " + config.getWorldName() + " was not found.");
        }
    }

    public void startLayer() {
        this.currentHeight = 0;
    }

    public void newLayer() {
        int maxVoidHeight = config.getVoidMax() + VOID_HEIGHT_OFFSET;

        if (currentHeight < maxVoidHeight) {
            if (world != null) {
                world.setVoidDamageMinBuildHeightOffset(currentHeight);
                currentHeight++;
            }
        }
    }

    public int getHeight() {
        return currentHeight - VOID_HEIGHT_OFFSET;
    }

    public void resetLayer() {
        if (world != null) {
            world.setVoidDamageMinBuildHeightOffset(-128);
            world.setVoidDamageAmount(4.0f);
            this.currentHeight = RESET_HEIGHT;
        }
    }
    
}
