package org.rcs.game;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.rcs.config.ConfigManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class User {

    private final TextColor PREFIX_COLOR = TextColor.color(0xFFFFFF);
    private final TextColor FOOTER_COLOR = TextColor.color(0x808080);
    private final TextColor HEADER_COLOR = TextColor.color(0xC0C0C0);
    
    private final Player player;
    private final String name;
    private final ConfigManager config;
    private final World overworld;
    private final int win;
    private final Set<String> roleKilled;

    private String role = "";

    public User(Player player) {
        this.player = player;
        this.name = player.getName();
        this.config = new ConfigManager();
        this.overworld = Bukkit.getWorld("world");
        this.win = config.getRoles().size();
        this.roleKilled = new HashSet<>();

        if (overworld == null) {
            throw new IllegalStateException("The world 'world' was not found.");
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getName() {
        return this.name;
    }

    public int getYLevel() {
        return (int) player.getY();
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
        sendColoredMessage("§6[RCS] §cYour role is : §r" + role);
        sendColoredMessage("§6[RCS] §cDescription : §r" + config.getRoleDescription(role));
    }

    public void resetRole() {
        this.role = "";
    }

    public void addKill(User killed) {
        if (killed.getRole() != null && !killed.getRole().isEmpty()) {
            roleKilled.add(killed.getRole());
        }
    }

    public int roleSize() {
        return roleKilled.size();
    }

    public void resetKills() {
        roleKilled.clear();
    }

    public boolean isWinner() {
        return roleSize() == win;
    }

    public float progress() {
        return (float) roleSize() / win;
    }

    public void userUpdateGoal() {
        player.setLevel(roleSize());
        player.setExp(progress());
    }

    public void setGamemode(GameMode gamemode) {
        player.setGameMode(gamemode);
    }

    public void userPrefix(String role) {
        Component prefix = createColoredComponent(role, PREFIX_COLOR);
        player.playerListName(prefix.append(Component.text(player.getName())));
    }

    public void userHeader(String contenu) {
        Component header = createColoredComponent(contenu, HEADER_COLOR);
        player.sendPlayerListHeader(header);
    }

    public void userFooter(String contenu) {
        Component footer = createColoredComponent(contenu, FOOTER_COLOR);
        player.sendPlayerListFooter(footer);
    }

    private void sendColoredMessage(String message) {
        player.sendMessage(Component.text(message));
    }

    private Component createColoredComponent(String text, TextColor color) {
        return Component.text(text).color(color);
    }

    public void randomTP() {
        double worldBorderSize = overworld.getWorldBorder().getSize();
        double centerX = overworld.getWorldBorder().getCenter().getX();
        double centerZ = overworld.getWorldBorder().getCenter().getZ();

        int maxAttempts = 10;
        Location randomLocation = null;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            double randomX = centerX + (Math.random() * worldBorderSize - worldBorderSize / 2);
            double randomZ = centerZ + (Math.random() * worldBorderSize - worldBorderSize / 2);

            int y = overworld.getHighestBlockYAt((int) randomX, (int) randomZ);
            randomLocation = new Location(overworld, Math.floor(randomX) + 0.5, y, Math.floor(randomZ) + 0.5);

            if (randomLocation.getBlock().getType().isSolid()) {
                break;
            }
        }

        if (randomLocation != null) {
            player.teleport(randomLocation.add(0, 1, 0));
        }
    }

}
