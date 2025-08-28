package com.ssomar.aichatmoderation.listeners;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DebugManager {

    @Getter
    private List<UUID> debugPlayers;

    private static DebugManager instance;

    public DebugManager() {
        this.debugPlayers = new ArrayList<>();
    }

    public static DebugManager getInstance() {
        if (instance == null) instance = new DebugManager();
        return instance;
    }

    public List<Player> getDebugOnlinePlayers() {
        List<Player> players = new java.util.ArrayList<>();
        for (UUID uuid : debugPlayers) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }
}
