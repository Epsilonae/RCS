package org.rcs.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.rcs.Main;
import org.rcs.config.ConfigManager;

public class StartCommand implements CommandExecutor {

    private final String SYSTEM_PREFIX = "§6[RCS] ";
    private final String ALREADY_STARTED_MESSAGE = SYSTEM_PREFIX + "§cThe game is already started.";
    private final String GAME_STARTED_MESSAGE = SYSTEM_PREFIX + "§aThe game has successfully started.";
    private final String SETMAP_ERROR_MESSAGE = SYSTEM_PREFIX + "§cCannot start the game. Use command §7/RCSsetmap§c before starting the game.";
    private final String NOT_ENOUGH_PLAYER_MESSAGE = SYSTEM_PREFIX + "§cNot enough players to start.";
    private final String NOT_ENOUGH_ROLE_MESSAGE = SYSTEM_PREFIX + "§cNot enough roles to start.";
    private final String TOO_MUCH_MESSAGE = SYSTEM_PREFIX + "§cToo many players to start. %s roles for %d players.";
    private final String INTERNAL_ERROR_MESSAGE = SYSTEM_PREFIX + "§cInternal error, game instance not found.";
    private final String PERMISSION_ERROR_MESSAGE = SYSTEM_PREFIX + "§cYou do not have permission to execute this command.";

    private final ConfigManager configManager = new ConfigManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("rcscommand.admin")) {
            sender.sendMessage(PERMISSION_ERROR_MESSAGE);
            return true;
        }

        if (Main.game == null) {
            sender.sendMessage(INTERNAL_ERROR_MESSAGE);
            return false;
        }

        int nbPlayer = Bukkit.getOnlinePlayers().size();
        int nbRole = configManager.getSize();

        if (nbRole <= 2) {
            sender.sendMessage(NOT_ENOUGH_ROLE_MESSAGE);
            return true;
        }

        if (nbPlayer < 2) {
            sender.sendMessage(NOT_ENOUGH_PLAYER_MESSAGE);
            return true;
        }

        if (nbPlayer > nbRole) {
            sender.sendMessage(String.format(TOO_MUCH_MESSAGE, nbRole, nbPlayer));
            return true;
        }

        if (Main.game.isGameOn()) {
            sender.sendMessage(ALREADY_STARTED_MESSAGE);
            return true;
        }

        Main.game.startGame();
        if (Main.game.isGameOn()) {
            sender.sendMessage(GAME_STARTED_MESSAGE);
        } else {
            sender.sendMessage(SETMAP_ERROR_MESSAGE);
        }
        return true;
    }
}
