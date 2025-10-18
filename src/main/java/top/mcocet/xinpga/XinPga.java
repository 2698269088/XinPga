// XinPga.java
package top.mcocet.xinpga;

import org.geysermc.mcprotocollib.auth.GameProfile;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.event.EventHandler;
import xin.bbtt.mcbot.event.Listener;
import xin.bbtt.mcbot.events.LoginSuccessEvent;
import xin.bbtt.mcbot.plugin.Plugin;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class XinPga implements Plugin, Listener {

    /* -------- 单例 -------- */
    public static XinPga INSTANCE;

    /* -------- 配置项 -------- */
    private XinPgaConfig config;

    /* -------- 运行时 -------- */
    private final Path configPath;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> task;
    private final Random random = new Random();

    public XinPga() {
        INSTANCE = this;
        this.configPath = Path.of("plugin", "XinPga", "config.json");
        this.config = new XinPgaConfig(configPath);
    }

    /* -------- 生命周期 -------- */
    @Override
    public void onLoad() { /* empty */ }

    @Override
    public void onEnable() {
        outLog("XinPga 插件已启用");
        outLog("XinPga 版本: v1.4");
        loadConfig();
        Bot.Instance.getPluginManager().events().registerEvents(this, this);
        Bot.Instance.getPluginManager().registerCommand(new XpaCommand(), new XpaCommandExecutor(), this);
        if (config.isEnabled()) startScheduler();
    }


    @Override
    public void onDisable() {
        stopScheduler();
    }

    @Override
    public void onUnload() { /* empty */ }

    /* -------- 事件监听 -------- */
    @EventHandler
    public void onLogin(LoginSuccessEvent event) {
        if (config.isEnabled()) startScheduler();
    }

    /* -------- 定时任务 -------- */
    private void startScheduler() {
        if (task != null && !task.isDone()) return;

        // 根据发送模式决定调度方式
        if (config.getSendMode() == SendMode.PRIVATE) {
            // 私聊模式使用固定速率调度
            task = scheduler.scheduleAtFixedRate(this::sendPrivateMessages, 0, config.getPrivateMessageInterval(), TimeUnit.SECONDS);
        } else {
            // 公告模式保持原有逻辑
            task = scheduler.scheduleAtFixedRate(this::sendOnce, 0, config.getIntervalSeconds(), TimeUnit.SECONDS);
        }
    }

    // 原有的公告发送方法（修改为发送多条消息）
    private void sendOnce() {
        List<String> messages = config.getMessages();
        if (messages.isEmpty()) {
            return;
        }

        // 使用单独的线程来发送多条消息，避免阻塞调度器
        new Thread(() -> {
            try {
                for (int i = 0; i < messages.size(); i++) {
                    String message = messages.get(i);
                    if (config.isAppendRandom()) {
                        message += " " + randomString(config.getRandomLength());
                    }
                    Bot.Instance.sendChatMessage(message);

                    // 如果不是最后一条消息，则等待指定间隔
                    if (i < messages.size() - 1) {
                        Thread.sleep(config.getMessageInterval() * 1000L);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // 私聊发送方法
    // 私聊发送方法
    private void sendPrivateMessages() {
        List<String> messages = config.getMessages();
        if (messages.isEmpty()) {
            return;
        }

        // 获取下一个要发送消息的玩家
        String currentPlayer = PrivateMessageSender.getNextPlayer();
        if (currentPlayer != null) {
            // 发送所有消息给这个玩家
            PrivateMessageSender.sendPrivateMessagesToPlayer(currentPlayer, messages, false, 0);
        }
    }

    // 获取当前应该发送消息的玩家（不改变索引）
    private String getCurrentPlayer() {
        // 获取当前在线玩家列表
        Map<UUID, GameProfile> players = Bot.Instance.players;

        List<String> onlinePlayers = players.values().stream()
                .map(GameProfile::getName)
                .collect(Collectors.toList());

        // 如果没有其他玩家在线，则不发送
        if (onlinePlayers.isEmpty()) {
            return null;
        }

        // 过滤掉黑名单中的玩家
        List<String> nonBlacklistedPlayers = onlinePlayers.stream()
                .filter(player -> !config.isPlayerBlacklisted(player))
                .collect(Collectors.toList());

        // 如果没有非黑名单玩家在线，则不发送
        if (nonBlacklistedPlayers.isEmpty()) {
            return null;
        }

        // 更新缓存的玩家列表（当列表发生变化时）
        if (!PrivateMessageSender.cachedPlayerList.equals(nonBlacklistedPlayers)) {
            PrivateMessageSender.cachedPlayerList = new ArrayList<>(nonBlacklistedPlayers);
            // PrivateMessageSender.currentPlayerIndex.set(0); // 重置索引
        }

        // 获取当前玩家
        int index = PrivateMessageSender.currentPlayerIndex.get();
        if (index >= PrivateMessageSender.cachedPlayerList.size()) {
            // PrivateMessageSender.currentPlayerIndex.set(0);
            index = 0;
        }

        if (index < PrivateMessageSender.cachedPlayerList.size()) {
            return PrivateMessageSender.cachedPlayerList.get(index);
        }
        return null;
    }

    // 发送剩余消息给指定玩家
    private void sendRemainingMessagesToPlayer(String playerName, List<String> messages) {
        new Thread(() -> {
            try {
                for (int i = 0; i < messages.size(); i++) {
                    String message = messages.get(i);
                    // 私聊模式不添加随机字符串
                    Bot.Instance.sendCommand("msg " + playerName + " " + message);
                    getLogger().info("已发送私聊消息给玩家：" + playerName + " 内容: " + message);

                    // 如果不是最后一条消息，则等待指定间隔
                    if (i < messages.size() - 1) {
                        Thread.sleep(config.getMessageInterval() * 1000L);
                    }
                }
                getLogger().info("已发送私聊消息给玩家：" + playerName);

                // 所有消息发送完成后，移动到下一个玩家
                PrivateMessageSender.currentPlayerIndex.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }


    private void stopScheduler() {
        if (task != null) {
            task.cancel(true); // 改为 true，立即中断正在进行的任务
        }
    }

    public String randomString(int len) {
        return random.ints(len, 'a', 'z' + 1)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /* -------- 配置读写 -------- */
    public void loadConfig() {
        try {
            config.loadConfig();
        } catch (Exception e) {
            throw new RuntimeException("[XinPga] 无法加载配置", e);
        }
    }

    public void saveConfig() {
        try {
            config.saveConfig();
        } catch (Exception e) {
            throw new RuntimeException("[XinPga] 无法保存配置", e);
        }
    }

    /* -------- 供命令调用 -------- */
    public boolean isRunning() {
        return task != null && !task.isDone();
    }

    public void cmdStart() {
        config.setEnabled(true);
        saveConfig();
        startScheduler();
        outLog("[XinPga] 已启动定时发送");
    }

    public void cmdStop() {
        config.setEnabled(false);
        saveConfig();
        stopScheduler();
        outLog("[XinPga] 已停止定时发送");
    }

    public void cmdString(String txt) {
        // 为了兼容性，将单条消息替换为消息列表
        config.setMessages(List.of(txt));
        saveConfig();
        outLog("[XinPga] 发送内容已改为: " + txt);
    }

    // 新增添加消息的命令方法
    public void cmdAddMessage(String message) {
        config.addMessage(message);
        saveConfig();
        outLog("[XinPga] 已添加消息: " + message);
    }

    // 新增移除消息的命令方法
    public void cmdRemoveMessage(String message) {
        config.removeMessage(message);
        saveConfig();
        outLog("[XinPga] 已移除消息: " + message);
    }

    // 新增列出所有消息的命令方法
    public void cmdListMessages() {
        List<String> messages = config.getMessages();
        if (messages.isEmpty()) {
            outLog("[XinPga] 消息列表为空");
        } else {
            outLog("[XinPga] 消息列表:");
            for (int i = 0; i < messages.size(); i++) {
                outLog((i + 1) + ". " + messages.get(i));
            }
        }
    }

    public void cmdTime(int sec) {
        config.setIntervalSeconds(sec);
        saveConfig();
        outLog("[XinPga] 发送间隔已改为: " + sec + " 秒");
        if (isRunning()) {
            stopScheduler();
            startScheduler();
        }
    }

    // 添加设置发送模式的命令方法
    public void cmdSendMode(String mode) {
        try {
            XinPga.SendMode sendMode = XinPga.SendMode.valueOf(mode.toUpperCase());
            config.setSendMode(sendMode);
            saveConfig();
            outLog("[XinPga] 发送模式已改为: " + mode);

            // 如果正在运行，重启任务以应用新模式
            if (isRunning()) {
                stopScheduler();
                startScheduler();
            }
        } catch (IllegalArgumentException e) {
            outError("[XinPga] 无效的发送模式: " + mode + "，有效值为: PUBLIC, PRIVATE");
        }
    }

    // 添加设置私聊间隔的命令方法
    public void cmdPrivateInterval(int seconds) {
        config.setPrivateMessageInterval(seconds);
        saveConfig();
        outLog("[XinPga] 私聊发送间隔已改为: " + seconds + " 秒");

        // 如果正在运行且是私聊模式，重启任务以应用新间隔
        if (isRunning() && config.getSendMode() == XinPga.SendMode.PRIVATE) {
            stopScheduler();
            startScheduler();
        }
    }

    // 添加设置消息间隔的命令方法
    public void cmdMessageInterval(int seconds) {
        config.setMessageInterval(seconds);
        saveConfig();
        outLog("[XinPga] 消息发送间隔已改为: " + seconds + " 秒");
    }

    // 添加黑名单管理方法
    public void cmdAddToBlacklist(String playerName) {
        config.addToBlacklist(playerName);
        saveConfig();
        outLog("[XinPga] 已将玩家 " + playerName + " 添加到私聊黑名单");
    }

    public void cmdRemoveFromBlacklist(String playerName) {
        config.removeFromBlacklist(playerName);
        saveConfig();
        outLog("[XinPga] 已将玩家 " + playerName + " 从私聊黑名单中移除");
    }

    public void cmdListBlacklist() {
        List<String> blacklist = config.getPrivateMessageBlacklist();
        if (blacklist.isEmpty()) {
            outLog("[XinPga] 私聊黑名单为空");
        } else {
            outLog("[XinPga] 私聊黑名单玩家列表: " + String.join(", ", blacklist));
        }
    }

    public void cmdReload() {
        loadConfig();
        outLog("[XinPga] 配置文件已重载");
        if (config.isEnabled() && !isRunning()) startScheduler();
        if (!config.isEnabled() && isRunning()) stopScheduler();
    }

    // 信息日志
    public void outLog(String log){
        getLogger().info( log);
    }

    // 错误日志
    public void outError(String log){
        getLogger().error( log);
    }

    // 警告日志
    public void outWarn(String log){
        getLogger().warn( log);
    }

    // 发送模式枚举
    public enum SendMode {
        PUBLIC,   // 公告模式
        PRIVATE   // 私聊模式
    }

    public XinPgaConfig getConfig() {
        return config;
    }
}
