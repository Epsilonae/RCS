package org.rcs.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.rcs.Main;
import org.rcs.common.BossbarManager;
import org.rcs.common.MapVoid;
import org.rcs.config.ConfigManager;
import org.rcs.inventory.InventoryGUI;
import org.rcs.roles.EquipRole;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.TitlePart;

public class GameManager {

    private static final int RESPAWN_INTERVAL = 10;
    private static final long GAME_LOOP_TICK_INTERVAL = 20L;

    private final Component SYSTEM_PREFIX = Component.text("[RCS]").color(TextColor.color(0xFFAA00));
    private final Component GAME_STARTED_MESSAGE = Component.text("The game has successfully started.").color(TextColor.color(0x55FF55));
    private final Component GAME_STOPPED_MESSAGE = Component.text("The game has been stopped.").color(TextColor.color(0xFF5555));
    

    public final Map<String, User> usersMapList = new HashMap<>();
    public List<String> rolesAvailable;

    private boolean gameOn;
    private int gameTime;
    private BukkitRunnable gameTask;

    private final int inventoryTime;
    private final ConfigManager config = new ConfigManager();;
    private final World overworld = Bukkit.getWorld(config.getWorldName());
    private final MapVoid mapVoid;
    private BossbarManager bossbarManager;

    public GameManager() {
        this.mapVoid = new MapVoid();
        this.inventoryTime = config.getInventoryInterval();
        overworld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        overworld.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        resetGameState();
    }

    private void resetGameState() {
        this.gameOn = false;
        this.gameTime = 0;
        this.rolesAvailable = config.getRoles();
    }

    public void assignPlayer(User user) {
        addRoleBack(user);
        user.setGamemode(GameMode.SPECTATOR);
        user.setRole(assignRandomRole());
        user.userPrefix(user.getRole() + " ");
        respawnAfterDeath(user);
    }

    public void resetPlayer(User user) {
        if (user != null && user.getPlayer() != null) {
            Player player = user.getPlayer();
            player.setGameMode(GameMode.SPECTATOR);
            player.clearTitle();
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            player.getInventory().clear();
            addRoleBack(user);
            user.userUpdateGoal();
            user.resetRole();
            user.userPrefix("");
            user.userHeader("");
            user.userFooter("");
        }
    }

    private void respawnAfterDeath(User user) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = user.getPlayer();
                if (player.getHealth() > 0.0) {
                    respawnPlayer(user);
                    player.setHealth(20);
                    player.setFoodLevel(20);
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    public void respawnPlayer(User user) {
        Player player = user.getPlayer();
        AtomicInteger countdown = new AtomicInteger(RESPAWN_INTERVAL);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameOn) {
                    cancel();
                    return;
                }

                int currentCount = countdown.get();
                if (currentCount > 0) {
                    player.sendTitlePart(TitlePart.TITLE, Component.text(String.valueOf(currentCount)).color(TextColor.color(0xFF0000)));
                    player.sendTitlePart(TitlePart.SUBTITLE, Component.text("Spawning").color(TextColor.color(0xFF6600)));
                    countdown.decrementAndGet();
                } else {
                    player.clearTitle();
                    user.randomTP();
                    user.setGamemode(GameMode.SURVIVAL);
                    user.userUpdateGoal();
                    EquipRole equiping = new EquipRole(config);
                    equiping.equipRoleToPlayer(player, user.getRole());
                    cancel();
                }

            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    public void startGame() {
        double length = overworld.getWorldBorder().getSize();
        if (5 >= length || length >= 320) return;
        resetGameState();
        this.bossbarManager = new BossbarManager("Next Chest", 0.0f);
        this.bossbarManager.showToAll();
        this.mapVoid.startLayer();
        Component message = Component.join(JoinConfiguration.separator(Component.text(" ")), SYSTEM_PREFIX, GAME_STARTED_MESSAGE);
        Bukkit.broadcast(message);
        initializePlayers();

        this.gameOn = true;
        startGameLoop();
    }

    private void initializePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            User user = new User(player);
            assignPlayer(user);
            usersMapList.put(user.getName(), user);
        }
    }

    public void endGame() {
        if (!gameOn) return;
        resetGameState();
        
        String winner = usersMapList.values().stream()
            .filter(User::isWinner)
            .map(User::getName)
            .findFirst()
            .orElse("No one");
    
        Bukkit.getOnlinePlayers().forEach(player -> {
            User user = usersMapList.get(player.getName());
            if (user != null) {
                resetPlayer(user);
                user.userFooter(""); 
            }
            player.sendTitlePart(TitlePart.TITLE, Component.text(winner).color(TextColor.color(0xFF0000)));
            player.sendTitlePart(TitlePart.SUBTITLE, Component.text("Won").color(TextColor.color(0xFF6600)));
        });
    
        mapVoid.resetLayer();
        bossbarManager.reset();
        usersMapList.clear();
        stopGameLoop();
        
        Component message = Component.join(JoinConfiguration.separator(Component.text(" ")), SYSTEM_PREFIX, GAME_STOPPED_MESSAGE);
        Bukkit.broadcast(message);
    }
    

    private void startGameLoop() {
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameOn) {
                    cancel();
                    return;
                }

                if (usersMapList.size() < 2) {
                    cancel();
                    endGame();
                    return;
                }

                handleInventoryEvents();
                handleBossbarProgress();
                handleVoidLayer();

                gameTime++;
            }
        };
        gameTask.runTaskTimer(Main.getInstance(), 0L, GAME_LOOP_TICK_INTERVAL);
    }

    private void handleInventoryEvents() {
        if ((gameTime + 1) % inventoryTime == 0) {
            bossbarManager.addProgress(-1.0f);
            for (Player player : Bukkit.getOnlinePlayers()) {
                InventoryGUI inventory = new InventoryGUI();
                inventory.initializeInventory();
                player.openInventory(inventory.getInventory());
            }
        }
    }

    private void handleBossbarProgress() {
        bossbarManager.addProgress(1.0f / inventoryTime);
    }

    private void handleVoidLayer() {
        if (gameTime % config.getVoidInterval() == 0) {
            mapVoid.newLayer();
            for (User user : usersMapList.values()) {
                user.userHeader("Current Y-level : " + user.getYLevel());
                user.userFooter("Minimum Y-level : " + mapVoid.getHeight());
            }
        }
    }

    private void stopGameLoop() {
        if (gameTask != null) {
            gameTask.cancel();
        }
    }

    public String assignRandomRole() {
        if (rolesAvailable.isEmpty()) {
            return "";
        }
        int index = ThreadLocalRandom.current().nextInt(rolesAvailable.size());
        return rolesAvailable.remove(index);
    }

    public void addRoleBack(User user) {
        String role = user.getRole();
        if (role != null && !role.isEmpty() && !rolesAvailable.contains(role)) {
            rolesAvailable.add(role);
        }
    }

    public boolean isGameOn() {
        return this.gameOn;
    }

    public int getGameTime() {
        return this.gameTime;
    }
}
