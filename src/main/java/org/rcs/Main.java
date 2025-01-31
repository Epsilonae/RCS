package org.rcs;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.rcs.commands.InfoCommand;
import org.rcs.commands.SetmapCommand;
import org.rcs.commands.StartCommand;
import org.rcs.commands.StopCommand;
import org.rcs.common.DeathMessages;
import org.rcs.config.ConfigManager;
import org.rcs.game.GameListener;
import org.rcs.game.GameManager;

public class Main extends JavaPlugin {

    private static Plugin instance;
    private ConfigManager configManager;
    public static GameManager game;

    @Override
    public void onEnable() {
        configManager = new ConfigManager();
        configManager.initializeConfig();
        game = new GameManager();

        instance = this;

        registerCommands();

        registerEvents();

        getLogger().info("Plugin enabled");
    }

    @Override
    public void onDisable() {
        game.endGame();
        getLogger().info("Plugin disabled");
    }

    @SuppressWarnings("null")
    private void registerCommands() {
        getCommand("info").setExecutor(new InfoCommand());
        getCommand("startgame").setExecutor(new StartCommand());
        getCommand("stopgame").setExecutor(new StopCommand());
        getCommand("setmap").setExecutor(new SetmapCommand());
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new GameListener(), this);
        getServer().getPluginManager().registerEvents(new DeathMessages(), this);
    }

    public static Plugin getInstance() {
        return instance;
    }
}
