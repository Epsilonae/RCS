package org.rcs.commands;

import java.util.List;
import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.rcs.config.ConfigManager;

public class InfoCommand implements CommandExecutor {

    private final String SYSTEM_PREFIX = "§6[RCS]§r ";
    private final String ROLE_NOT_FOUND_MESSAGE = SYSTEM_PREFIX + "§cThis role does not exist.";
    private final String NO_ROLES_MESSAGE = SYSTEM_PREFIX + "§cNo role available.";
    private final String AVAILABLE_ROLE_MESSAGE = SYSTEM_PREFIX + "Available roles : ";
    private final String PERMISSION_ERROR_MESSAGE = SYSTEM_PREFIX + "§cYou do not have permission to execute this command.";

    private final ConfigManager config = new ConfigManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("rcscommand.use")) {
            sender.sendMessage(PERMISSION_ERROR_MESSAGE);
            return true;
        }

        if (args.length != 1) {
            return false;
        }

        String argument = args[0].toLowerCase();
        switch (argument) {
            case "list" -> showRoleList(sender);
            default -> showRoleInfo(sender, argument);
        }
        return true;
    }

    private void showRoleList(CommandSender sender) {
        List<String> roles = config.getRoles();
        if (roles.isEmpty()) {
            sender.sendMessage(NO_ROLES_MESSAGE);
        } else {
            sender.sendMessage(AVAILABLE_ROLE_MESSAGE + String.join(", ", roles));
        }
    }

    private void showRoleInfo(CommandSender sender, String role) {
        List<String> roles = config.getRoles();
        Optional<String> matchedRole = roles.stream()
                                             .filter(r -> r.equalsIgnoreCase(role))
                                             .findFirst();
        if (matchedRole.isPresent()) {
            String description = config.getRoleDescription(matchedRole.get());
            sender.sendMessage(SYSTEM_PREFIX + matchedRole.get() + " - " + description);
        } else {
            sender.sendMessage(ROLE_NOT_FOUND_MESSAGE);
        }
    }
}
