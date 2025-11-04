package top.mcocet.xinpga.config;

import com.google.gson.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import top.mcocet.xinpga.XinPga;

public class XinPgaConfig {
    private final Path configPath;

    private boolean enabled = true;
    private int intervalSeconds = 30;
    private List<String> messages = new ArrayList<>();
    private boolean appendRandom = true;
    private int randomLength = 3;
    private XinPga.SendMode sendMode = XinPga.SendMode.PUBLIC;
    private int privateMessageInterval = 5;
    private List<String> privateMessageBlacklist = new ArrayList<>();
    private int messageInterval = 1;
    private List<String> administrators = new ArrayList<>();

    public XinPgaConfig(Path configPath) {
        this.configPath = configPath;
        this.messages.add("你好啊");
    }

    public void loadConfig() throws IOException {
        if (Files.notExists(configPath)) {
            // 确保目录存在
            Files.createDirectories(configPath.getParent());
            createDefaultConfig();
        }

        JsonObject root = JsonParser.parseString(Files.readString(configPath)).getAsJsonObject();
        enabled = root.get("enabled").getAsBoolean();
        intervalSeconds = root.get("intervalSeconds").getAsInt();

        if (root.has("messages") && root.get("messages").isJsonArray()) {
            messages = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("messages")) {
                messages.add(element.getAsString());
            }
        }

        appendRandom = root.get("appendRandomString").getAsBoolean();
        randomLength = root.get("randomLength").getAsInt();

        if (root.has("sendMode")) {
            try {
                sendMode = XinPga.SendMode.valueOf(root.get("sendMode").getAsString().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        privateMessageInterval = root.has("privateMessageInterval") ?
                root.get("privateMessageInterval").getAsInt() : 5;

        messageInterval = root.has("messageInterval") ?
                root.get("messageInterval").getAsInt() : 1;

        if (root.has("privateMessageBlacklist") && root.get("privateMessageBlacklist").isJsonArray()) {
            privateMessageBlacklist = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("privateMessageBlacklist")) {
                privateMessageBlacklist.add(element.getAsString());
            }
        }

        if (root.has("administrators") && root.get("administrators").isJsonArray()) {
            administrators = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("administrators")) {
                administrators.add(element.getAsString());
            }
        }
    }

    public void saveConfig() throws IOException {
        // 确保目录存在
        Files.createDirectories(configPath.getParent());

        JsonObject root = new JsonObject();
        root.addProperty("enabled", enabled);
        root.addProperty("intervalSeconds", intervalSeconds);

        JsonArray messagesArray = new JsonArray();
        messages.forEach(m -> messagesArray.add(m));
        root.add("messages", messagesArray);

        root.addProperty("appendRandomString", appendRandom);
        root.addProperty("randomLength", randomLength);
        root.addProperty("sendMode", sendMode.name());
        root.addProperty("privateMessageInterval", privateMessageInterval);
        root.addProperty("messageInterval", messageInterval);

        JsonArray blacklistArray = new JsonArray();
        privateMessageBlacklist.forEach(b -> blacklistArray.add(b));
        root.add("privateMessageBlacklist", blacklistArray);

        JsonArray adminArray = new JsonArray();
        administrators.forEach(a -> adminArray.add(a));
        root.add("administrators", adminArray);

        Files.writeString(configPath, new GsonBuilder().setPrettyPrinting().create().toJson(root));
    }

    private void createDefaultConfig() throws IOException {
        // 确保目录存在
        Files.createDirectories(configPath.getParent());

        JsonObject def = new JsonObject();
        def.addProperty("enabled", true);
        def.addProperty("intervalSeconds", 30);

        JsonArray defaultMessages = new JsonArray();
        defaultMessages.add("你好啊");
        defaultMessages.add("本宣传工具基于xinbot框架制作，已在GitHub开源。（github.com/2698269088/XinPga）");
        def.add("messages", defaultMessages);

        def.addProperty("appendRandomString", true);
        def.addProperty("randomLength", 3);
        def.addProperty("sendMode", "PUBLIC");
        def.addProperty("privateMessageInterval", 5);
        def.addProperty("messageInterval", 1);

        JsonArray defaultBlacklist = new JsonArray();
        defaultBlacklist.add("e_2");
        def.add("privateMessageBlacklist", defaultBlacklist);

        JsonArray defaultAdmins = new JsonArray();
        def.add("administrators", defaultAdmins);

        Files.writeString(configPath, new GsonBuilder().setPrettyPrinting().create().toJson(def));
    }

    // Getters & Setters 保持不变
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = new ArrayList<>(messages);
    }

    public void addMessage(String message) {
        if (!messages.contains(message)) {
            messages.add(message);
        }
    }

    public void removeMessage(String message) {
        messages.remove(message);
    }

    public boolean isAppendRandom() {
        return appendRandom;
    }

    public void setAppendRandom(boolean appendRandom) {
        this.appendRandom = appendRandom;
    }

    public int getRandomLength() {
        return randomLength;
    }

    public void setRandomLength(int randomLength) {
        this.randomLength = randomLength;
    }

    public XinPga.SendMode getSendMode() {
        return sendMode;
    }

    public void setSendMode(XinPga.SendMode sendMode) {
        this.sendMode = sendMode;
    }

    public int getPrivateMessageInterval() {
        return privateMessageInterval;
    }

    public void setPrivateMessageInterval(int privateMessageInterval) {
        this.privateMessageInterval = privateMessageInterval;
    }

    public int getMessageInterval() {
        return messageInterval;
    }

    public void setMessageInterval(int messageInterval) {
        this.messageInterval = messageInterval;
    }

    public List<String> getPrivateMessageBlacklist() {
        return privateMessageBlacklist;
    }

    public void setPrivateMessageBlacklist(List<String> privateMessageBlacklist) {
        this.privateMessageBlacklist = new ArrayList<>(privateMessageBlacklist);
    }

    public void addToBlacklist(String playerName) {
        if (!privateMessageBlacklist.contains(playerName)) {
            privateMessageBlacklist.add(playerName);
        }
    }

    public void removeFromBlacklist(String playerName) {
        privateMessageBlacklist.remove(playerName);
    }

    public boolean isPlayerBlacklisted(String playerName) {
        return privateMessageBlacklist.contains(playerName);
    }

    public List<String> getAdministrators() {
        return administrators;
    }

    public void setAdministrators(List<String> administrators) {
        this.administrators = new ArrayList<>(administrators);
    }

    public void addAdministrator(String playerName) {
        if (!administrators.contains(playerName)) {
            administrators.add(playerName);
        }
    }

    public void removeAdministrator(String playerName) {
        administrators.remove(playerName);
    }

    public boolean isAdministrator(String playerName) {
        return administrators.contains(playerName);
    }
}