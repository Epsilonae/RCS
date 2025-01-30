package org.rcs.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetmapCommand implements CommandExecutor {

    private final String SYSTEM_PREFIX = "§6[RCS]§r ";
    private final String RESET_MESSAGE = SYSTEM_PREFIX + "§aThe game area has been reset.";
    private final String SUCCESS_MESSAGE = SYSTEM_PREFIX + "§aThe game area has been updated. Center = §e%s§a, §e%s§a, Side = §e%d";
    private final String INCORRECT_LENGTH_MESSAGE = SYSTEM_PREFIX + "§cLength must be between 5 and 320.";
    private final String INVALID_ARGUMENTS_MESSAGE = SYSTEM_PREFIX + "§cCoordinates and length must be valid numbers.";
    private final String WORLD_NOT_FOUND_MESSAGE = SYSTEM_PREFIX + "§cThe world 'world' was not found.";
    private final String PERMISSION_ERROR_MESSAGE = SYSTEM_PREFIX + "§cYou do not have permission to execute this command.";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("rcscommand.admin")) {
            sender.sendMessage(PERMISSION_ERROR_MESSAGE);
            return true;
        }
        
        World overworld = Bukkit.getWorld("world");
        if (overworld == null) {
            sender.sendMessage(WORLD_NOT_FOUND_MESSAGE);
            return false;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            resetWorldBorder(overworld, sender);
            return true;
        }

        if (args.length == 3) {
            return handleSetmapCommand(sender, args, overworld);
        }

        return false;
    }

    private boolean handleSetmapCommand(CommandSender sender, String[] args, World overworld) {
        try {
            int x = Integer.parseInt(args[0]);
            int z = Integer.parseInt(args[1]);
            
            int length = Integer.parseInt(args[2]);
            if (5 <= length && length <= 320) {
                Location center = new Location(overworld, x, 200, z);
                setWorldBorder(overworld, center, length);

                sender.sendMessage(String.format(SUCCESS_MESSAGE, x, z, length));
                return true;
            }
            sender.sendMessage(INCORRECT_LENGTH_MESSAGE);
            return true;
            
        } catch (NumberFormatException e) {
            sender.sendMessage(INVALID_ARGUMENTS_MESSAGE);
            return false;
        }
    }

    private void resetWorldBorder(World overworld, CommandSender sender) {
        overworld.getWorldBorder().reset();
        sender.sendMessage(RESET_MESSAGE);
    }

    private void setWorldBorder(World overworld, Location center, int length) {
        overworld.setSpawnLocation(center);
        overworld.setGameRule(GameRule.SPAWN_RADIUS, 0);

        overworld.getWorldBorder().setCenter(center);
        overworld.getWorldBorder().setSize(length);
        overworld.getWorldBorder().setDamageAmount(0);
    }
}
