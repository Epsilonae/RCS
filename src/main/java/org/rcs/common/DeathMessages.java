package org.rcs.common;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.rcs.config.ConfigManager;

import net.kyori.adventure.text.Component;

public class DeathMessages implements Listener {

    private final ConfigManager config = new ConfigManager();

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getPlayer();
        Player killer = player.getKiller();
        String message;
        if (killer != null) {
            message = "§c" + player.getName() + "§7 " + config.getRandomDeathMessage() + "§6 " + killer.getName();
        } else {
            message = "§c" + player.getName() + "§7 " + config.getSuicideMessage();
        }
        Component componentMessage = Component.text(message);
        Bukkit.broadcast(componentMessage);
    }
    
}
