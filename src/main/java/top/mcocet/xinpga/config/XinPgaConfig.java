package top.mcocet.xinpga.config;

import com.google.gson.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import top.mcocet.xinpga.XinPga;

public class XinPgaConfig {
    private final Path configPath; // 配置文件路径

    private boolean enabled = true; // 默认启用
    private int intervalSeconds = 30; // 默认30秒
    private List<String> messages = new ArrayList<>(); // 宣传消息
    private boolean appendRandom = true; // 是否添加随机字符串
    private int randomLength = 3; // 随机字符串的长度
    private XinPga.SendMode sendMode = XinPga.SendMode.PUBLIC; // 发送模式
    private int privateMessageInterval = 5; // 私聊消息的间隔
    private List<String> privateMessageBlacklist = new ArrayList<>(); // 私聊黑名单
    private int messageInterval = 1; // 发送消息的间隔
    private List<String> administrators = new ArrayList<>(); // 管理员列表
    private boolean remoteCommandEnabled = true; // 远程命令是否启用
    private boolean remoteCommandAdminEnabled = false; // 远程命令的admin功能是否启用
    private boolean randomSendingEnabled = false; // 是否启用随机发送功能
    private boolean greetingEnabled = false; // 是否启用问候语功能
    private String greetingFormat = "hi，你好#name#，"; // 问候语格式
    
    // 多线程发送功能的配置项
    private boolean multiThreadEnabled = false; // 是否启用多线程发送功能
    private List<String> multiThreadMessages = new ArrayList<>(); // 多线程发送的独立消息列表
    private int multiThreadDuration = 30; // 多线程发送的持续时间（秒）
    private int multiThreadInterval = 2; // 多线程发送的消息间隔（秒）
    private int multiThreadCheckInterval = 5; // 多线程发送检查的间隔（秒）
    
    // 数字替换功能的配置项
    private boolean numberReplacementEnabled = false; // 是否启用多线程数字替换功能
    private boolean mainNumberReplacementEnabled = false; // 是否启用主发送模式数字替换功能
    private int minConsecutiveNumbers = 5; // 最少连续数字数量，达到此数量才进行替换
    
    // 同步更新多线程消息的配置项
    private boolean syncMultiThreadMessages = false; // 是否同步更新多线程消息列表
    
    // 随机发送间隔的配置项
    private boolean randomIntervalEnabled = false; // 是否启用随机发送间隔
    private int mainIntervalDeviation = 5; // 主线程发送间隔偏差（秒）
    private int mainMessageIntervalDeviation = 3; // 主线程消息间发送间隔偏差（秒）
    private int multiThreadIntervalDeviation = 5; // 多线程发送间隔偏差（秒）
    private int multiThreadMessageIntervalDeviation = 3; // 多线程消息间发送间隔偏差（秒）

    public XinPgaConfig(Path configPath) {
        this.configPath = configPath;
        this.messages.add("你好啊");
        // 初始化多线程消息列表
        this.multiThreadMessages.add("多线程消息1");
        this.multiThreadMessages.add("多线程消息2");
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

        // 加载随机发送设置
        if (root.has("randomSendingEnabled")) {
            randomSendingEnabled = root.get("randomSendingEnabled").getAsBoolean();
        }
        
        // 加载问候语设置
        if (root.has("greetingEnabled")) {
            greetingEnabled = root.get("greetingEnabled").getAsBoolean();
        }
        
        if (root.has("greetingFormat")) {
            greetingFormat = root.get("greetingFormat").getAsString();
        }
        
        // 加载多线程发送功能配置
        if (root.has("multiThreadEnabled")) {
            multiThreadEnabled = root.get("multiThreadEnabled").getAsBoolean();
        }
        
        if (root.has("multiThreadMessages") && root.get("multiThreadMessages").isJsonArray()) {
            multiThreadMessages = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("multiThreadMessages")) {
                multiThreadMessages.add(element.getAsString());
            }
        }
        
        if (root.has("multiThreadDuration")) {
            multiThreadDuration = root.get("multiThreadDuration").getAsInt();
        }
        
        if (root.has("multiThreadInterval")) {
            multiThreadInterval = root.get("multiThreadInterval").getAsInt();
        }
        
        if (root.has("multiThreadCheckInterval")) {
            multiThreadCheckInterval = root.get("multiThreadCheckInterval").getAsInt();
        }
        
        // 加载数字替换功能配置
        if (root.has("numberReplacementEnabled")) {
            numberReplacementEnabled = root.get("numberReplacementEnabled").getAsBoolean();
        }
        
        // 加载主发送模式数字替换功能配置
        if (root.has("mainNumberReplacementEnabled")) {
            mainNumberReplacementEnabled = root.get("mainNumberReplacementEnabled").getAsBoolean();
        }
        
        if (root.has("minConsecutiveNumbers")) {
            minConsecutiveNumbers = root.get("minConsecutiveNumbers").getAsInt();
        }
        
        // 加载随机发送间隔配置
        if (root.has("randomIntervalEnabled")) {
            randomIntervalEnabled = root.get("randomIntervalEnabled").getAsBoolean();
        }
        
        if (root.has("mainIntervalDeviation")) {
            mainIntervalDeviation = root.get("mainIntervalDeviation").getAsInt();
        }
        
        if (root.has("mainMessageIntervalDeviation")) {
            mainMessageIntervalDeviation = root.get("mainMessageIntervalDeviation").getAsInt();
        }
        
        if (root.has("multiThreadIntervalDeviation")) {
            multiThreadIntervalDeviation = root.get("multiThreadIntervalDeviation").getAsInt();
        }
        
        if (root.has("multiThreadMessageIntervalDeviation")) {
            multiThreadMessageIntervalDeviation = root.get("multiThreadMessageIntervalDeviation").getAsInt();
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
        
        // 保存随机发送设置
        root.addProperty("randomSendingEnabled", randomSendingEnabled);
        
        // 保存问候语设置
        root.addProperty("greetingEnabled", greetingEnabled);
        root.addProperty("greetingFormat", greetingFormat);
        
        // 保存多线程发送功能配置
        root.addProperty("multiThreadEnabled", multiThreadEnabled);
        
        JsonArray multiThreadMessagesArray = new JsonArray();
        multiThreadMessages.forEach(m -> multiThreadMessagesArray.add(m));
        root.add("multiThreadMessages", multiThreadMessagesArray);
        
        root.addProperty("multiThreadDuration", multiThreadDuration);
        root.addProperty("multiThreadInterval", multiThreadInterval);
        root.addProperty("multiThreadCheckInterval", multiThreadCheckInterval);
        
        // 保存数字替换功能配置
        root.addProperty("numberReplacementEnabled", numberReplacementEnabled);
        root.addProperty("mainNumberReplacementEnabled", mainNumberReplacementEnabled);
        root.addProperty("minConsecutiveNumbers", minConsecutiveNumbers);
        
        // 保存同步更新多线程消息配置
        root.addProperty("syncMultiThreadMessages", syncMultiThreadMessages);
        
        // 保存随机发送间隔配置
        root.addProperty("randomIntervalEnabled", randomIntervalEnabled);
        root.addProperty("mainIntervalDeviation", mainIntervalDeviation);
        root.addProperty("mainMessageIntervalDeviation", mainMessageIntervalDeviation);
        root.addProperty("multiThreadIntervalDeviation", multiThreadIntervalDeviation);
        root.addProperty("multiThreadMessageIntervalDeviation", multiThreadMessageIntervalDeviation);

        Files.writeString(configPath, new GsonBuilder().setPrettyPrinting().create().toJson(root));
    }

    private void createDefaultConfig() throws IOException {
        // 确保目录存在
        Files.createDirectories(configPath.getParent());

        // 基本配置
        JsonObject def = new JsonObject();
        def.addProperty("enabled", true);
        def.addProperty("intervalSeconds", 30);

        // 默认消息
        JsonArray defaultMessages = new JsonArray();
        defaultMessages.add("你好啊");
        defaultMessages.add("本宣传工具基于xinbot框架制作，已在GitHub开源。（github.com/2698269088/XinPga）");
        def.add("messages", defaultMessages);

        // 发送模式配置项
        def.addProperty("appendRandomString", true);
        def.addProperty("randomLength", 3);
        def.addProperty("sendMode", "PUBLIC");
        def.addProperty("privateMessageInterval", 5);
        def.addProperty("messageInterval", 1);

        // 私聊黑名单
        JsonArray defaultBlacklist = new JsonArray();
        defaultBlacklist.add("e_2");
        def.add("privateMessageBlacklist", defaultBlacklist);

        // 管理员列表
        JsonArray defaultAdmins = new JsonArray();
        def.add("administrators", defaultAdmins);

        // 远程命令配置项
        def.addProperty("remoteCommandEnabled", true);
        def.addProperty("remoteCommandAdminEnabled", false);
        
        // 随机发送功能配置项
        def.addProperty("randomSendingEnabled", false);
        
        // 问候语功能配置项
        def.addProperty("greetingEnabled", false);
        def.addProperty("greetingFormat", "hi，#name#，");
        
        // 多线程发送功能配置项
        def.addProperty("multiThreadEnabled", false);
        
        JsonArray defaultMultiThreadMessages = new JsonArray();
        defaultMultiThreadMessages.add("多线程消息1");
        defaultMultiThreadMessages.add("多线程消息2");
        def.add("multiThreadMessages", defaultMultiThreadMessages);
        
        def.addProperty("multiThreadDuration", 30);
        def.addProperty("multiThreadInterval", 2);
        def.addProperty("multiThreadCheckInterval", 5);
        
        // 数字替换功能配置项
        def.addProperty("numberReplacementEnabled", false);
        def.addProperty("mainNumberReplacementEnabled", false);
        def.addProperty("minConsecutiveNumbers", 5);
        
        // 随机发送间隔配置项
        def.addProperty("randomIntervalEnabled", false);
        def.addProperty("mainIntervalDeviation", 5);
        def.addProperty("mainMessageIntervalDeviation", 3);
        def.addProperty("multiThreadIntervalDeviation", 5);
        def.addProperty("multiThreadMessageIntervalDeviation", 3);

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

    public boolean isRemoteCommandEnabled() {
        return remoteCommandEnabled;
    }

    public void setRemoteCommandEnabled(boolean remoteCommandEnabled) {
        this.remoteCommandEnabled = remoteCommandEnabled;
    }

    public boolean isRemoteCommandAdminEnabled() {
        return remoteCommandAdminEnabled;
    }

    public void setRemoteCommandAdminEnabled(boolean remoteCommandAdminEnabled) {
        this.remoteCommandAdminEnabled = remoteCommandAdminEnabled;
    }
    
    // 随机发送功能相关方法
    public boolean isRandomSendingEnabled() {
        return randomSendingEnabled;
    }

    public void setRandomSendingEnabled(boolean randomSendingEnabled) {
        this.randomSendingEnabled = randomSendingEnabled;
    }
    
    // 问候语功能相关方法
    public boolean isGreetingEnabled() {
        return greetingEnabled;
    }

    public void setGreetingEnabled(boolean greetingEnabled) {
        this.greetingEnabled = greetingEnabled;
    }

    public String getGreetingFormat() {
        return greetingFormat;
    }

    public void setGreetingFormat(String greetingFormat) {
        this.greetingFormat = greetingFormat;
    }
    
    // 多线程发送功能相关方法
    public boolean isMultiThreadEnabled() {
        return multiThreadEnabled;
    }

    public void setMultiThreadEnabled(boolean multiThreadEnabled) {
        this.multiThreadEnabled = multiThreadEnabled;
    }

    public List<String> getMultiThreadMessages() {
        return multiThreadMessages;
    }

    public void setMultiThreadMessages(List<String> multiThreadMessages) {
        this.multiThreadMessages = new ArrayList<>(multiThreadMessages);
    }

    public void addMultiThreadMessage(String message) {
        if (!multiThreadMessages.contains(message)) {
            multiThreadMessages.add(message);
        }
    }

    public void removeMultiThreadMessage(String message) {
        multiThreadMessages.remove(message);
    }

    public int getMultiThreadDuration() {
        return multiThreadDuration;
    }

    public void setMultiThreadDuration(int multiThreadDuration) {
        this.multiThreadDuration = multiThreadDuration;
    }

    public int getMultiThreadInterval() {
        return multiThreadInterval;
    }

    public void setMultiThreadInterval(int multiThreadInterval) {
        this.multiThreadInterval = multiThreadInterval;
    }

    public int getMultiThreadCheckInterval() {
        return multiThreadCheckInterval;
    }

    public void setMultiThreadCheckInterval(int multiThreadCheckInterval) {
        this.multiThreadCheckInterval = multiThreadCheckInterval;
    }
    
    // 数字替换功能相关方法
    public boolean isNumberReplacementEnabled() {
        return numberReplacementEnabled;
    }

    public void setNumberReplacementEnabled(boolean numberReplacementEnabled) {
        this.numberReplacementEnabled = numberReplacementEnabled;
    }
    
    // 主发送模式数字替换功能相关方法
    public boolean isMainNumberReplacementEnabled() {
        return mainNumberReplacementEnabled;
    }

    public void setMainNumberReplacementEnabled(boolean mainNumberReplacementEnabled) {
        this.mainNumberReplacementEnabled = mainNumberReplacementEnabled;
    }

    public int getMinConsecutiveNumbers() {
        return minConsecutiveNumbers;
    }

    public void setMinConsecutiveNumbers(int minConsecutiveNumbers) {
        this.minConsecutiveNumbers = minConsecutiveNumbers;
    }
    
    // 同步更新多线程消息相关方法
    public boolean isSyncMultiThreadMessages() {
        return syncMultiThreadMessages;
    }

    public void setSyncMultiThreadMessages(boolean syncMultiThreadMessages) {
        this.syncMultiThreadMessages = syncMultiThreadMessages;
    }
    
    // 随机发送间隔相关方法
    public boolean isRandomIntervalEnabled() {
        return randomIntervalEnabled;
    }

    public void setRandomIntervalEnabled(boolean randomIntervalEnabled) {
        this.randomIntervalEnabled = randomIntervalEnabled;
    }

    public int getMainIntervalDeviation() {
        return mainIntervalDeviation;
    }

    public void setMainIntervalDeviation(int mainIntervalDeviation) {
        this.mainIntervalDeviation = mainIntervalDeviation;
    }

    public int getMainMessageIntervalDeviation() {
        return mainMessageIntervalDeviation;
    }

    public void setMainMessageIntervalDeviation(int mainMessageIntervalDeviation) {
        this.mainMessageIntervalDeviation = mainMessageIntervalDeviation;
    }

    public int getMultiThreadIntervalDeviation() {
        return multiThreadIntervalDeviation;
    }

    public void setMultiThreadIntervalDeviation(int multiThreadIntervalDeviation) {
        this.multiThreadIntervalDeviation = multiThreadIntervalDeviation;
    }

    public int getMultiThreadMessageIntervalDeviation() {
        return multiThreadMessageIntervalDeviation;
    }

    public void setMultiThreadMessageIntervalDeviation(int multiThreadMessageIntervalDeviation) {
        this.multiThreadMessageIntervalDeviation = multiThreadMessageIntervalDeviation;
    }
}