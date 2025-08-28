package com.ssomar.aichatmoderation.categories;

import com.ssomar.aichatmoderation.actions.Action;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
public class Category {

    private String id;

    private boolean detection;
    private Map<String, Action> actions;

    public Category(ConfigurationSection config) {
        this.id = config.getName();
        this.detection = config.getBoolean("detection", true);
        this.actions = new LinkedHashMap<>();
        if (config.contains("actions")) {
            ConfigurationSection actionsSection = config.getConfigurationSection("actions");
            for (String actionId : actionsSection.getKeys(false)) {
                ConfigurationSection actionSection = actionsSection.getConfigurationSection(actionId);
                Action action = new Action(actionSection);
                this.actions.put(actionId, action);
            }
            actions = getSortedActionsByConfidenceDesc();
        }
    }

    // To sort it by the getConfidence() method in descending order:
    public Map<String, Action> getSortedActionsByConfidenceDesc() {
        // Convert map to a list of entries
        List<Map.Entry<String, Action>> entryList = new ArrayList<>(actions.entrySet());

        // Sort the list using a comparator that compares Action objects by confidence in descending order
        Collections.sort(entryList, new Comparator<Map.Entry<String, Action>>() {
            @Override
            public int compare(Map.Entry<String, Action> entry1, Map.Entry<String, Action> entry2) {
                // For descending order, compare entry2 to entry1
                return Double.compare(entry2.getValue().getConfidence(), entry1.getValue().getConfidence());
            }
        });

        // Create a new LinkedHashMap to preserve the sorted order
        Map<String, Action> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Action> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    @Nullable
    public Action getAction(double confidence) {
        for (Action action : actions.values()) {
            if (action.getConfidence() <= confidence) {
                return action;
            }
        }
        return null;
    }
}
