package org.rcs.common;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class BossbarManager {

    private final BossBar bossbar;
    private final Set<Player> playersWithBossbar = new HashSet<>();
    private float progress;

    public BossbarManager(String title, float initialProgress) {
        this.progress = Math.max(0.0f, Math.min(initialProgress, 1.0f));
        this.bossbar = BossBar.bossBar(
            Component.text(title).color(TextColor.color(0xFFD700)), 
            this.progress, 
            BossBar.Color.RED, 
            BossBar.Overlay.PROGRESS
        );
    }

    private void showToPlayer(Player player) {
        if (playersWithBossbar.add(player)) {
            player.showBossBar(bossbar);
        }
    }

    public void showToAll() {
        Bukkit.getOnlinePlayers().forEach(this::showToPlayer);
    }

    public void hideFromAll() {
        playersWithBossbar.forEach(player -> player.hideBossBar(bossbar));
        playersWithBossbar.clear();
    }

    public void addProgress(float newProgress) {
        progress = Math.min(1.0f, Math.max(0.0f, progress + newProgress));
        bossbar.progress(progress);
    }

    public void setProgress(float newProgress) {
        bossbar.progress(newProgress);
    }

    public void reset() {
        hideFromAll();
        progress = 0.0f;
        if (bossbar != null) {
            bossbar.progress(progress);
        }
    }
}
