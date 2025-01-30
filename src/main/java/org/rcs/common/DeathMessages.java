package org.rcs.common;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.rcs.config.ConfigManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class DeathMessages implements Listener {

    private final ConfigManager config = new ConfigManager();

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getPlayer();
        Player killer = player.getKiller();
        String message;
        if (killer != null) {
            message = player.getName() + " " + config.getRandomDeathMessage() + " " + killer.getName();
        } else {
            message = player.getName() + " " + config.getSuicideMessage();
        }
        Component componentMessage = Component.text(message).color(TextColor.color(0xFFFFFF));
        Bukkit.broadcast(componentMessage);
    }
    
}
