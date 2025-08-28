package com.ssomar.aichatmoderation;

import com.ssomar.aichatmoderation.categories.Category;
import com.ssomar.aichatmoderation.commands.ACMCommands;
import com.ssomar.aichatmoderation.listeners.OnChatListener;
import com.ssomar.aichatmoderation.scheduler.BukkitSchedulerHook;
import com.ssomar.aichatmoderation.scheduler.RegionisedSchedulerHook;
import com.ssomar.aichatmoderation.scheduler.SchedulerHook;
import com.ssomar.aichatmoderation.utils.StringConverter;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class AIChatModeration extends JavaPlugin {

    public static AIChatModeration plugin;
    public static SchedulerHook schedulerHook;

    private boolean configEnabled = true;
    private boolean logsFlaggedMessages = true;
    private String loggedMessageFormat = "";
    private boolean printAPIErrors = false;
    private boolean checkAfterMessageHasBeenSent;
    private Map<String, Category> categories = new HashMap<>();
    private List<String> providers = new ArrayList<>();


    private List<String> blackListedWords = new ArrayList<>();
    private boolean blacklistedWordsHideMessage = true;
    private List<String> blacklistedWordsCommands = new ArrayList<>();

    private String sendFlaggedMessagesToPlayersWithPermission = "";

    public static boolean isFolia;

    @Override
    public void onEnable() {
        plugin = this;

        int pluginId = 25300;
        Metrics metrics = new Metrics(this, pluginId);

        getLogger().info("Version of the server: " + Bukkit.getServer().getVersion());

        isFolia = hasClass("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");

        if (isFolia) {
            getLogger().info("Detected Folia server. Using RegionisedSchedulerHook.");
        }

        if (isFolia) schedulerHook = new RegionisedSchedulerHook(plugin);
        else schedulerHook = new BukkitSchedulerHook(plugin);

        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Load configuration
        loadConfig();

        // Register commands
        getCommand("acm").setExecutor(new ACMCommands(this));

        // Register events
        getServer().getPluginManager().registerEvents(new OnChatListener(this), this);

        getLogger().info("AI Minecraft Plugin enabled! (by Ssomar)");
    }

    public static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("AI Minecraft Plugin disabled! (by Ssomar)");
    }

    public void loadConfig() {
        // Reload config from disk
        reloadConfig();

        // Get configuration values
        configEnabled = getConfig().getBoolean("enabled", true);
        logsFlaggedMessages = getConfig().getBoolean("logs-flagged-messages", true);
        loggedMessageFormat = getConfig().getString("logged-message-format", "Flagged message: Player:[%player%] - Message:[%message%] - Category:[%category%] - Confidence[%confidence%]");
        printAPIErrors = getConfig().getBoolean("print-api-errors", false);
        checkAfterMessageHasBeenSent = getConfig().getBoolean("check-after-message-has-been-sent", false);
        if(getConfig().contains("categories")) {
            for(String key : getConfig().getConfigurationSection("categories").getKeys(false)) {
                ConfigurationSection categorySection = getConfig().getConfigurationSection("categories." + key);
                Category category = new Category(categorySection);
                categories.put(key, category);
            }
        }
        providers = new ArrayList<>();
        if(getConfig().contains("provider")) {
            String provider = getConfig().getString("provider");
            if(provider != null) {
                providers.add(provider);
            }
        }
        else {
            providers = getConfig().getStringList("providers");
        }
        if (getConfig().contains("blacklisted-words")) {
            blackListedWords = getConfig().getStringList("blacklisted-words");
            // Convert to lowercase
            blackListedWords.replaceAll(String::toLowerCase);
        } else {
            getLogger().warning("No blacklisted words found in the config. You can add some if you want (blacklisted-words).");
        }
        blacklistedWordsHideMessage = getConfig().getBoolean("blacklisted-words-hide-message", true);
        if (getConfig().contains("blacklisted-words-commands")) {
            blacklistedWordsCommands = getConfig().getStringList("blacklisted-words-commands");
        }
        // colored messages
        for (int i = 0; i < blacklistedWordsCommands.size(); i++) {
            String cmd = blacklistedWordsCommands.get(i);
            String coloredCmd = StringConverter.coloredString(cmd);
            blacklistedWordsCommands.set(i, coloredCmd);
        }

        sendFlaggedMessagesToPlayersWithPermission = getConfig().getString("send-flagged-messages-to-players-with-permission", "");
    }
}
