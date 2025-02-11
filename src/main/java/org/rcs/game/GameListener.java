package org.rcs.game;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.rcs.Main;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;

public class GameListener implements Listener {

    private final GameManager game = Main.game;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (game == null || !game.isGameOn()) return;
        Player player = event.getPlayer();
        User user = getUser(player);
        if (user != null) {
            user.userHeader("Current Y-level : " + user.getYLevel());
        }
    }

    @EventHandler
    public void onPickupEXP(PlayerPickupExperienceEvent event) {
        if (game == null || !game.isGameOn()) return;
        event.getExperienceOrb().setExperience(0);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            if (game.isGameOn()) {
                player.removePotionEffect(PotionEffectType.DARKNESS);
                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 20, 0));
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (game == null || !game.isGameOn()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Player killer = player.getKiller();

        User killed = getUser(player);
        if (killed == null) return;

        if (killer != null) {
            User userKiller = getUser(killer);
            if (userKiller != null) {
                userKiller.addKill(killed);
                userKiller.userUpdateGoal();
                if (userKiller.isWinner()) {
                    game.endGame();
                    return;
                }
            }
        }
        game.assignPlayer(killed);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (game == null || !game.isGameOn()) return;

        Player player = event.getPlayer();
        player.setExp(0f);
        player.setLevel(0);
        User user = getUser(player);
        if (user != null && user.getRole() != null && !user.getRole().isEmpty()) {
            game.usersMapList.remove(user.getName());
            game.rolesAvailable.add(user.getRole());
        }
    }

    private User getUser(Player player) {
        return (game != null) ? game.usersMapList.get(player.getName()) : null;
    }

}
