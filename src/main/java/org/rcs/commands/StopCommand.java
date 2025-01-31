package org.rcs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.rcs.Main;

public class StopCommand implements CommandExecutor {

    private final String SYSTEM_PREFIX = "§6[RCS] ";
    private final String NO_GAME_IN_PROGRESS_MESSAGE = SYSTEM_PREFIX + "§cNo game is currently started.";
    private final String INTERNAL_ERROR_MESSAGE = SYSTEM_PREFIX + "§cInternal error, game instance not found.";
    private final String PERMISSION_ERROR_MESSAGE = SYSTEM_PREFIX + "§cYou do not have permission to execute this command.";

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

        if (!Main.game.isGameOn()) {
            sender.sendMessage(NO_GAME_IN_PROGRESS_MESSAGE);
            return true;
        }

        Main.game.endGame();
        return true;
    }
}
