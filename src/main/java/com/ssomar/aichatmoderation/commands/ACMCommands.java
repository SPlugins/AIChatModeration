package com.ssomar.aichatmoderation.commands;

import com.ssomar.aichatmoderation.AIChatModeration;
import com.ssomar.aichatmoderation.listeners.DebugManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ACMCommands implements CommandExecutor, TabExecutor {

    private final AIChatModeration plugin;

    public ACMCommands(AIChatModeration plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /acm reload | debug");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("acm.reload")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            plugin.loadConfig();
            sender.sendMessage("§aConfig reloaded.");
            return true;
        }
        else if (subCommand.equalsIgnoreCase("debug")) {
            if(!(sender instanceof Player)) {
                sender.sendMessage("§cThis command can only be used by a player.");
                return true;
            }
            Player player = (Player) sender;

            if (!sender.hasPermission("acm.debug")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            List<UUID> players = DebugManager.getInstance().getDebugPlayers();
            if(players.contains(player.getUniqueId())) {
                players.remove(player.getUniqueId());
                sender.sendMessage("§cYou are no longer in debug mode.");
            } else {
                players.add(player.getUniqueId());
                sender.sendMessage("§aYou are now in debug mode.");
            }
            return true;
        }
        return true;
    }

    // auto completion
    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        if (command.getName().equalsIgnoreCase("acm")) {
            if (args.length == 1) {
                return Arrays.asList("reload", "debug");
            }
        }
        return Collections.emptyList();
    }
}

