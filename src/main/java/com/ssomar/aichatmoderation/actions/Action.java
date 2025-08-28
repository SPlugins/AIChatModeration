package com.ssomar.aichatmoderation.actions;

import com.ssomar.aichatmoderation.AIChatModeration;
import com.ssomar.aichatmoderation.utils.StringConverter;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter@Setter
public class Action {

    private String id;

    private double confidence;
    private boolean hideMessage;
    private List<String> commands;

    public Action() {
        this.id = "default";
        this.confidence = 1;
        this.hideMessage = true;
        this.commands = new java.util.ArrayList<>();
    }

    public Action(ConfigurationSection section) {
        this.id = section.getName();
        this.confidence = section.getDouble("confidence", 0.85);
        this.hideMessage = section.getBoolean("hideMessage", true);
        this.commands = section.getStringList("commands");
        for (int i = 0; i < this.commands.size(); i++) {
            this.commands.set(i, StringConverter.coloredString(this.commands.get(i)));
        }
    }

    public void runCommands(@NotNull Player player) {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (String command : commands) {
                    command = command.replace("%player%", player.getName());
                    command = command.replace("%player_uuid%", player.getUniqueId().toString());

                    if(command.startsWith("SEND_MESSAGE")){
                        command = command.replace("SEND_MESSAGE", "");
                        player.sendMessage(command);
                        continue;
                    }

                    Bukkit.dispatchCommand(console, command);
                }
            }
        };
        AIChatModeration.schedulerHook.runEntityTask(runnable, null, player, 0);
    }
}
