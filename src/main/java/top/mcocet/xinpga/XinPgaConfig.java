package top.mcocet.xinpga;

import com.google.gson.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XinPgaConfig {
    private final Path configPath;

    // 配置项
    private boolean enabled;
    private int intervalSeconds;
    private List<String> messages; // 修改为消息列表
    private boolean appendRandom;
    private int randomLength;
    private XinPga.SendMode sendMode;
    private int privateMessageInterval;
    private List<String> privateMessageBlacklist; // 新增黑名单配置
    private int messageInterval; // 每条消息之间的发送间隔

    public XinPgaConfig(Path configPath) {
        this.configPath = configPath;
        // 设置默认值
        this.enabled = true;
        this.intervalSeconds = 30;
        this.messages = new ArrayList<>(); // 初始化消息列表
        this.messages.add("你好啊"); // 默认消息
        this.appendRandom = true;
        this.randomLength = 3;
        this.sendMode = XinPga.SendMode.PUBLIC;
        this.privateMessageInterval = 5;
        this.privateMessageBlacklist = new ArrayList<>();
        this.messageInterval = 1; // 默认每条消息间隔1秒
    }

    public void loadConfig() throws IOException {
        if (Files.notExists(configPath)) {
            Files.createDirectories(configPath.getParent());
            createDefaultConfig();
        }

        JsonObject root = JsonParser.parseString(Files.readString(configPath)).getAsJsonObject();
        enabled = root.get("enabled").getAsBoolean();
        intervalSeconds = root.get("intervalSeconds").getAsInt();

        // 读取消息列表
        if (root.has("messages") && root.get("messages").isJsonArray()) {
            messages = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("messages")) {
                messages.add(element.getAsString());
            }
        } else {
            messages = new ArrayList<>();
            messages.add("你好啊");
        }

        appendRandom = root.get("appendRandomString").getAsBoolean();
        randomLength = root.get("randomLength").getAsInt();

        // 读取发送模式配置
        if (root.has("sendMode")) {
            try {
                sendMode = XinPga.SendMode.valueOf(root.get("sendMode").getAsString().toUpperCase());
            } catch (IllegalArgumentException e) {
                sendMode = XinPga.SendMode.PUBLIC; // 默认为公告模式
            }
        } else {
            sendMode = XinPga.SendMode.PUBLIC;
        }

        // 读取私聊间隔配置
        privateMessageInterval = root.has("privateMessageInterval") ?
                root.get("privateMessageInterval").getAsInt() : 5;

        // 读取消息间隔配置
        messageInterval = root.has("messageInterval") ?
                root.get("messageInterval").getAsInt() : 1;

        // 读取黑名单配置
        if (root.has("privateMessageBlacklist") && root.get("privateMessageBlacklist").isJsonArray()) {
            privateMessageBlacklist = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("privateMessageBlacklist")) {
                privateMessageBlacklist.add(element.getAsString());
            }
        } else {
            privateMessageBlacklist = new ArrayList<>();
        }
    }

    public void saveConfig() throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("enabled", enabled);
        root.addProperty("intervalSeconds", intervalSeconds);

        // 保存消息列表
        JsonArray messagesArray = new JsonArray();
        for (String message : messages) {
            messagesArray.add(message);
        }
        root.add("messages", messagesArray);

        root.addProperty("appendRandomString", appendRandom);
        root.addProperty("randomLength", randomLength);
        root.addProperty("sendMode", sendMode.name());
        root.addProperty("privateMessageInterval", privateMessageInterval);
        root.addProperty("messageInterval", messageInterval);

        // 保存黑名单配置
        JsonArray blacklistArray = new JsonArray();
        for (String player : privateMessageBlacklist) {
            blacklistArray.add(player);
        }
        root.add("privateMessageBlacklist", blacklistArray);

        Files.writeString(configPath, new GsonBuilder().setPrettyPrinting().create().toJson(root));
    }

    private void createDefaultConfig() throws IOException {
        JsonObject def = new JsonObject();
        def.addProperty("enabled", true);
        def.addProperty("intervalSeconds", 30);

        // 添加默认消息列表
        JsonArray defaultMessages = new JsonArray();
        defaultMessages.add("你好啊");
        def.add("messages", defaultMessages);

        def.addProperty("appendRandomString", true);
        def.addProperty("randomLength", 3);
        def.addProperty("sendMode", "PUBLIC");
        def.addProperty("privateMessageInterval", 5);
        def.addProperty("messageInterval", 1);

        // 添加默认黑名单
        JsonArray defaultBlacklist = new JsonArray();
        defaultBlacklist.add("e_2");
        def.add("privateMessageBlacklist", defaultBlacklist);

        Files.writeString(configPath, new GsonBuilder().setPrettyPrinting().create().toJson(def));
    }

    // Getters and Setters
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

    // 黑名单相关方法
    public List<String> getPrivateMessageBlacklist() {
        return privateMessageBlacklist;
    }

    public void setPrivateMessageBlacklist(List<String> blacklist) {
        this.privateMessageBlacklist = new ArrayList<>(blacklist);
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
}
