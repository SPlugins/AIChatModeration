package com.ssomar.aichatmoderation.listeners;

import com.ssomar.aichatmoderation.AIChatModeration;
import com.ssomar.aichatmoderation.actions.Action;
import com.ssomar.aichatmoderation.categories.Category;
import com.ssomar.aichatmoderation.utils.StringConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class OnChatListener implements Listener {

    private AIChatModeration plugin;
    private final JSONParser jsonParser = new JSONParser();

    public OnChatListener(AIChatModeration plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {
        if (plugin.isConfigEnabled()) {
            Player player = e.getPlayer();
            // Bypass permission
            if (!player.hasPermission("acm.bypass")) {


                if (plugin.isCheckAfterMessageHasBeenSent()) {
                    BukkitRunnable runnable = new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Check the message after it has been sent
                            checkMessage(e);
                        }
                    };
                    AIChatModeration.schedulerHook.runEntityTask(runnable, null, player, 0);
                } else {
                    // Check the message before it has been sent
                    checkMessage(e);
                }

            }
        }
        //long end = System.currentTimeMillis();
        //System.out.println("Chat event END >>"+(end-start));
    }

    private void checkMessage(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        String uncoloredMessage = StringConverter.decoloredString(message);
        String uncoloredMessageLower;
        List<String> blackListedWords = plugin.getBlackListedWords();
        if (!blackListedWords.isEmpty()) {
            uncoloredMessageLower = uncoloredMessage.toLowerCase();
            for (String blackListedWord : blackListedWords) {
                if (uncoloredMessageLower.contains(blackListedWord)) {

                    if (plugin.isBlacklistedWordsHideMessage()) e.setCancelled(true);

                    Action action = new Action();
                    action.setCommands(plugin.getBlacklistedWordsCommands());
                    action.runCommands(e.getPlayer());

                    sendDebugMessage("&7&oDebug Message: \n&cMessage contains blacklisted word: " + blackListedWord);
                    return;
                }
            }
        }

        for (String provider : plugin.getProviders()) {
            try {
                URL url = new URL("https://api.ssomar.com/api/moderation");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-api-key", "no-key");
                conn.setRequestProperty("provider", provider);
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(500);
                conn.setDoOutput(true);

                // Create JSON request body
                String jsonInputString = "{\"input\": \"" + uncoloredMessage.replace("\"", "\\\"") + "\"}";
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                //long getResponse = System.currentTimeMillis();
                //System.out.println("Chat event GET RESPONSE >>"+(getResponse-start));

                // Process response
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    //long getResponsecode = System.currentTimeMillis();
                    //System.out.println("Chat event GET RESPONSE CODE>>"+(getResponsecode-start));
                    String response = readInputStream(conn.getInputStream());

                    //long readResponse = System.currentTimeMillis();
                    //System.out.println("Chat event GET READ RESPONSE>>"+(readResponse-start));

                    JSONObject jsonResponse = (JSONObject) jsonParser.parse(response);

                    // Cache the response
                    //responseCache.put(uncoloredMessage, new CachedResponse(jsonResponse));

                    // Process the moderation result
                    if(processModerationResult(jsonResponse, e)){
                        break;
                    }
                }
                else {
                    sendDebugMessage("&7&oDebug Message: \n&cModeration API returned an error: " + responseCode);
                }
            } catch (Exception ex) {
                if(plugin.isPrintAPIErrors()) ex.printStackTrace();
            }
        }
    }

    // True if the message was flagged and action was taken
    private boolean processModerationResult(JSONObject jsonResponse, AsyncPlayerChatEvent e) {
        try {
            //boolean flagged = (boolean) jsonResponse.getOrDefault("flagged", false);
            //if (flagged) {
                JSONObject categoriesFlagged = (JSONObject) jsonResponse.get("categoriesFlagged");
                JSONObject categoriesConfidence = (JSONObject) jsonResponse.get("categoriesConfidence");

                Map<String, Category> categories = plugin.getCategories();

                for (Object key : categoriesConfidence.keySet()) {
                    String category = (String) key;
                    if (categories.containsKey(category) && categories.get(category).isDetection()) {
                        Category categoryObj = categories.get(category);
                        double confidence = (double) categoriesConfidence.getOrDefault(category, 0.0);
                        Action action = categoryObj.getAction(confidence);

                        if (action != null) {
                            if (plugin.isLogsFlaggedMessages()) {

                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                String formattedDate = LocalDateTime.now().format(formatter);

                                String logMessage = plugin.getLoggedMessageFormat()
                                        //time date YYYY-MM-DD HH:mm:ss
                                        .replace("%time%", formattedDate)
                                        .replace("%player%", e.getPlayer().getName())
                                        .replace("%message%", e.getMessage())
                                        .replace("%category%", category)
                                        .replace("%confidence%", String.valueOf(confidence));
                                plugin.getLogger().info(logMessage);
                                File pluginFolder = plugin.getDataFolder();

                                // Create a logs file or append to it
                                File logFile = new File(pluginFolder, "logs.txt");
                                if (!logFile.exists()) {
                                    logFile.createNewFile();
                                }
                                try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                                    writer.write(logMessage);
                                    writer.newLine();
                                } catch (IOException ex) {
                                    plugin.getLogger().warning("Failed to write to log file: " + ex.getMessage());
                                }

                                if(!plugin.getSendFlaggedMessagesToPlayersWithPermission().isEmpty()){
                                    try {
                                        for(Player player : Bukkit.getServer().getOnlinePlayers()){
                                            if(player.hasPermission(plugin.getSendFlaggedMessagesToPlayersWithPermission())){
                                                player.sendMessage(StringConverter.coloredString(logMessage));
                                            }
                                        }
                                    }
                                    catch (Exception | Error ex) {}
                                }
                            }

                            if (action.isHideMessage()) {
                                e.setCancelled(true);
                            }
                            // Synchronize back to the main thread for Bukkit API operations

                            BukkitRunnable runnable = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    action.runCommands(e.getPlayer());
                                }
                            };
                            AIChatModeration.schedulerHook.runEntityTask(runnable, null, e.getPlayer(), 0);
                            return true;
                        }
                    }
                }
                sendDebugMessage(categoriesFlagged, categoriesConfidence);
            //}
        } catch (Exception ex) {
            if(plugin.isPrintAPIErrors()) System.err.println("Error processing moderation result: " + ex.getMessage());
            if(plugin.isPrintAPIErrors()) ex.printStackTrace();
        }
        return false;
    }

    public void sendDebugMessage(JSONObject categoriesFlagged, JSONObject categoriesConfidence) {
        StringBuilder message = new StringBuilder();
        message.append("&7&oDebug Message: \n");
        message.append("&7&oCategories: \n");
        for (Object key : categoriesConfidence.keySet()) {
            String category = (String) key;
            double confidence = (double) categoriesConfidence.getOrDefault(category, 0.0);
            message.append("&7&o- &e").append(category).append(" &7&oConfidence: &e").append(confidence).append("\n");
        }
        String messageStr = StringConverter.coloredString(message.toString());
        // Send the message to all players in the debug list
        sendDebugMessage(messageStr);
    }

    public void sendDebugMessage(String message) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> players = DebugManager.getInstance().getDebugOnlinePlayers();
                if (players.isEmpty()) return;

                for (Player player : DebugManager.getInstance().getDebugOnlinePlayers()) {
                    if (player != null && player.isOnline()) {
                        player.sendMessage(StringConverter.coloredString(message));
                    }
                }
            }
        };
        AIChatModeration.schedulerHook.runAsyncTask(runnable, 1);
    }


    private String readInputStream(InputStream is) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
