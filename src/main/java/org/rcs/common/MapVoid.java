package org.rcs.common;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.rcs.config.ConfigManager;

public class MapVoid {

    private final int RESET_HEIGHT = -64;
    private final int VOID_HEIGHT_OFFSET = 66;
    
    private final World world;
    private final ConfigManager config;

    private int currentHeight;

    public MapVoid() {
        this.world = Bukkit.getWorld("world");
        this.config = new ConfigManager();
        this.currentHeight = RESET_HEIGHT;

        if (world != null) {
            world.setVoidDamageAmount(0.1f);
        } else {
            throw new IllegalStateException("The world 'world' was not found.");
        }
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
            world.setVoidDamageMinBuildHeightOffset(RESET_HEIGHT);
            this.currentHeight = RESET_HEIGHT;
        }
    }
    
}
