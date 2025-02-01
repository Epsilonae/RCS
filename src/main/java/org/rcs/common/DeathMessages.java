package org.rcs.common;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.rcs.config.ConfigManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextColor;

public class DeathMessages implements Listener {

    private final ConfigManager config = new ConfigManager();

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getPlayer();
        Player killer = player.getKiller();
        Component playerComponent = Component.text(player.getName()).color(TextColor.color(0xFF5555));
        if (killer != null) {
            Component killerComponent = Component.text(killer.getName()).color(TextColor.color(0x00AAAA));
            Component deathComponent = Component.text(config.getRandomDeathMessage()).color(TextColor.color(0xAAAAAA));
            Component message = Component.join(JoinConfiguration.separator(Component.text(" ")), playerComponent, deathComponent, killerComponent);
            Bukkit.broadcast(message);
        } else {
            Component suicideComponent = Component.text(config.getSuicideMessage()).color(TextColor.color(0xAAAAAA));
            Component message = Component.join(JoinConfiguration.separator(Component.text(" ")), playerComponent, suicideComponent);
            Bukkit.broadcast(message);
        }
        
        
    }
    
}
