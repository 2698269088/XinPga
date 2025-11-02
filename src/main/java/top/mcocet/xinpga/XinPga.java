// XinPga.java
package top.mcocet.xinpga;

import org.geysermc.mcprotocollib.auth.GameProfile;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.event.EventHandler;
import xin.bbtt.mcbot.event.Listener;
import xin.bbtt.mcbot.events.LoginSuccessEvent;
import xin.bbtt.mcbot.plugin.Plugin;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class XinPga implements Plugin, Listener {
    public volatile boolean isRunning = false; // 是否运行

    /* -------- 单例 -------- */
    public static XinPga INSTANCE;

    /* -------- 配置项 -------- */
    private XinPgaConfig config;

    /* -------- 运行时 -------- */
    private final Path configPath;
    private ScheduledExecutorService scheduler; // ✅ 移除了 final，允许重新创建
    private ScheduledFuture<?> task;
    private final Random random = new Random();

    public XinPga() {
        INSTANCE = this;
        this.configPath = Path.of("plugin", "XinPga", "config.json");
        this.config = new XinPgaConfig(configPath);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(); // ✅ 在构造函数中初始化
    }

    /* -------- 生命周期 -------- */
    @Override
    public void onLoad() { /* empty */ }

    @Override
    public void onEnable() {
        outLog("XinPga 插件已启用");
        outLog("XinPga 版本: v1.5");
        loadConfig();
        Bot.Instance.getPluginManager().events().registerEvents(this, this);
        Bot.Instance.getPluginManager().registerCommand(new XpaCommand(), new XpaCommandExecutor(), this);

        // ✅ 确保线程池可用
        ensureSchedulerAvailable();

        if (config.isEnabled()) startScheduler();
    }

    @Override
    public void onDisable() {
        stopScheduler(); // 只停止任务，不关闭线程池
        outLog("XinPga 插件已关闭");
    }

    @Override
    public void onUnload() {
        // ✅ 只在卸载时关闭线程池
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        outLog("XinPga 插件已卸载");
    }

    // ✅ 添加线程池状态检查方法
    private boolean isSchedulerAvailable() {
        return scheduler != null && !scheduler.isShutdown() && !scheduler.isTerminated();
    }

    // ✅ 确保线程池可用
    private void ensureSchedulerAvailable() {
        if (!isSchedulerAvailable()) {
            outLog("线程池不可用，重新创建...");
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }
    }

    // 登录成功时启动调度器
    @EventHandler
    public void onLogin(LoginSuccessEvent event) {
        PrivateMessageSender.setBotName(Bot.Instance.getProtocol().getProfile().getName());
        if (config.isEnabled()) startScheduler();
    }

    // 启动调度器
    private void startScheduler() {
        // ✅ 先停止现有任务
        stopScheduler();

        // ✅ 确保线程池可用
        ensureSchedulerAvailable();

        isRunning = true;
        PrivateMessageSender.updateOnlinePlayerList();

        try {
            if (config.getSendMode() == SendMode.PRIVATE) {
                task = scheduler.scheduleWithFixedDelay(this::sendPrivateMessages, 0, config.getPrivateMessageInterval(), TimeUnit.SECONDS);
            } else {
                task = scheduler.scheduleAtFixedRate(this::sendOnce, 0, config.getIntervalSeconds(), TimeUnit.SECONDS);
            }
            outLog("调度器已启动，模式: " + config.getSendMode());
        } catch (RejectedExecutionException e) {
            outError("启动调度器失败，线程池不可用: " + e.getMessage());
            // ✅ 尝试恢复
            ensureSchedulerAvailable();
            // 重新尝试
            if (config.getSendMode() == SendMode.PRIVATE) {
                task = scheduler.scheduleWithFixedDelay(this::sendPrivateMessages, 0, config.getPrivateMessageInterval(), TimeUnit.SECONDS);
            } else {
                task = scheduler.scheduleAtFixedRate(this::sendOnce, 0, config.getIntervalSeconds(), TimeUnit.SECONDS);
            }
            outLog("调度器恢复成功");
        }
    }

    // 原有的公告发送方法（修改为发送多条消息）
    private void sendOnce() {
        List<String> messages = config.getMessages();
        if (messages.isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                for (int i = 0; i < messages.size(); i++) {
                    // 在每次循环开始都检查停止标志
                    if (!isRunning) {
                        return;
                    }

                    String message = messages.get(i);
                    if (i == messages.size() - 1) {
                        PrivateMessageSender.updateOnlinePlayerList();
                    }
                    if (config.isAppendRandom()) {
                        message += " " + randomString(config.getRandomLength());
                    }
                    Bot.Instance.sendChatMessage(message);

                    // 可中断的等待
                    if (i < messages.size() - 1) {
                        long waitTime = config.getMessageInterval() * 1000L;
                        long startTime = System.currentTimeMillis();
                        while (isRunning && (System.currentTimeMillis() - startTime) < waitTime) {
                            Thread.sleep(100);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // 私聊发送方法
    private void sendPrivateMessages() {
        List<String> messages = config.getMessages();
        if (messages.isEmpty()) {
            return;
        }
        String currentPlayer = PrivateMessageSender.getNextPlayer();
        if (currentPlayer != null) {
            PrivateMessageSender.sendPrivateMessagesToPlayer(currentPlayer, messages, false, 0);
        }
    }

    // 添加调试方法
    public void cmdDebugPlayerList() {
        PrivateMessageSender.printPlayerListStatus();
        outLog("当前Bot名称: " + Bot.Instance.getProtocol().getProfile().getName());

        // 显示当前在线玩家
        Map<UUID, GameProfile> players = Bot.Instance.players;
        List<String> onlinePlayers = players.values().stream()
                .map(GameProfile::getName)
                .collect(Collectors.toList());
        outLog("当前在线玩家: " + String.join(", ", onlinePlayers));

        // 显示黑名单玩家
        List<String> blacklist = config.getPrivateMessageBlacklist();
        outLog("黑名单玩家: " + String.join(", ", blacklist));
    }

    // 添加管理员管理方法
    public void cmdAddAdministrator(String playerName) {
        config.addAdministrator(playerName);
        saveConfig();
        outLog("信息：已将玩家 " + playerName + " 添加到管理员列表");
    }

    public void cmdRemoveAdministrator(String playerName) {
        config.removeAdministrator(playerName);
        saveConfig();
        outLog("信息：已将玩家 " + playerName + " 从管理员列表中移除");
    }

    public void cmdListAdministrators() {
        List<String> admins = config.getAdministrators();
        if (admins.isEmpty()) {
            outLog("信息：管理员列表为空");
        } else {
            outLog("信息：管理员列表: " + String.join(", ", admins));
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
        }

        // 获取当前玩家
        int index = PrivateMessageSender.currentPlayerIndex.get();
        if (index >= PrivateMessageSender.cachedPlayerList.size()) {
            index = 0;
        }

        if (index < PrivateMessageSender.cachedPlayerList.size()) {
            return PrivateMessageSender.cachedPlayerList.get(index);
        }
        return null;
    }

    // 处理私聊命令
    @EventHandler
    public void onPrivateMessage(xin.bbtt.mcbot.events.PrivateChatEvent event) {
        // 获取发送者名称和消息内容
        String playerName = event.getSender().getName();
        String message = event.getMessage();

        // 检查玩家是否是管理员
        if (config.isAdministrator(playerName)) {
            // 检查消息是否以命令关键字开头
            if (message.startsWith("#command xpa ") || message.startsWith("#cmd xpa ")) {
                // 提取命令部分
                String command = message.substring(message.indexOf("xpa ") + 4);
                outLog("收到来自管理员 " + playerName + " 的命令: " + command);

                // 执行命令
                executeAdminCommand(playerName, command);
            }
        }
    }

    // 执行管理员命令
    private void executeAdminCommand(String playerName, String command) {
        String[] args = command.split(" ");

        try {
            // 创建命令执行器并执行命令，捕获输出
            XpaCommandExecutor executor = new XpaCommandExecutor();
            List<String> output = executor.onCommandWithOutput(new Command() {
                @Override
                public String getName() {
                    return "xpa";
                }

                @Override
                public String getUsage() {
                    return "/xpa [command]";
                }

                @Override
                public String[] getAliases() {
                    return new String[0];
                }

                @Override
                public String getDescription() {
                    return "XinPga 定时宣传";
                }
            }, "xpa", args);

            // 将输出结果通过私聊发送给管理员
            for (String line : output) {
                Bot.Instance.sendCommand("msg " + playerName + " " + line);
            }

            outLog("管理员 " + playerName + " 执行命令: " + command);
        } catch (Exception e) {
            String errorMsg = "管理员 " + playerName + " 执行命令 '" + command + "' 时出错: " + e.getMessage();
            Bot.Instance.sendCommand("msg " + playerName + " 命令执行出错，请检查命令格式");
            outError(errorMsg);
        } catch (Throwable t) {
            String errorMsg = "管理员 " + playerName + " 执行命令 '" + command + "' 时发生严重错误: " + t.getMessage();
            Bot.Instance.sendCommand("msg " + playerName + " 命令执行发生严重错误");
            outError(errorMsg);
        }
    }

    public void cmdString(int index, String txt) {
        List<String> messages = new ArrayList<>(config.getMessages());
        if (index >= 0 && index < messages.size()) {
            messages.set(index, txt);
            config.setMessages(messages);
            saveConfig();
            outLog("信息：第 " + (index + 1) + " 条发送内容已改为: " + txt);
        } else {
            outError("错误：编号超出范围，当前共有 " + messages.size() + " 条消息");
            outWarn("提示：请使用 xpa listmessages 命令列出所有发送消息");
        }
    }

    private void stopScheduler() {
        isRunning = false; // 先设置停止标志
        if (task != null) {
            task.cancel(true);
            task = null;
        }
        // 等待一小段时间确保任务停止
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
            throw new RuntimeException("错误：无法加载配置", e);
        }
    }

    public void cmdUpdatePlayerList() {
        PrivateMessageSender.updateOnlinePlayerList();
        outLog("信息：已手动更新在线玩家列表");
    }

    public void saveConfig() {
        try {
            config.saveConfig();
        } catch (Exception e) {
            throw new RuntimeException("错误：无法保存配置", e);
        }
    }

    /* -------- 供命令调用 -------- */
    public boolean isRunning() {
        return task != null && !task.isDone();
    }

    public void cmdStart() {
        if (isRunning) {
            outLog("任务：任务已经在运行中！");
            return;
        }
        config.setEnabled(true);
        saveConfig();
        startScheduler();
        outLog("任务：已启动定时发送");
    }

    public void cmdStop() {
        if (!isRunning) {
            outLog("任务：任务未运行！");
            return;
        }
        config.setEnabled(false);
        saveConfig();
        stopScheduler();
        outLog("任务：已停止定时发送");
    }

    public void cmdString(String txt) {
        // 为了兼容性，将单条消息替换为消息列表
        config.setMessages(List.of(txt));
        saveConfig();
        outLog("信息：发送内容已改为: " + txt);
    }

    // 新增添加消息的命令方法
    public void cmdAddMessage(String message) {
        config.addMessage(message);
        saveConfig();
        outLog("信息：已添加消息: " + message);
    }

    // 新增移除消息的命令方法
    public void cmdRemoveMessage(String message) {
        config.removeMessage(message);
        saveConfig();
        outLog("信息：已移除消息: " + message);
    }

    // 新增列出所有消息的命令方法
    public void cmdListMessages() {
        List<String> messages = config.getMessages();
        if (messages.isEmpty()) {
            outLog("信息：消息列表为空");
        } else {
            outLog("信息：消息列表:");
            for (int i = 0; i < messages.size(); i++) {
                outLog((i + 1) + ". " + messages.get(i));
            }
        }
    }

    public void cmdTime(int sec) {
        config.setIntervalSeconds(sec);
        saveConfig();
        outLog("信息：发送间隔已改为: " + sec + " 秒");
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
            outLog("信息：发送模式已改为: " + mode);

            // 如果正在运行，重启任务以应用新模式
            if (isRunning()) {
                stopScheduler();
                startScheduler();
            }
        } catch (IllegalArgumentException e) {
            outError("警告：无效的发送模式: " + mode + "，有效值为: PUBLIC, PRIVATE");
        }
    }

    // 添加设置私聊间隔的命令方法
    public void cmdPrivateInterval(int seconds) {
        config.setPrivateMessageInterval(seconds);
        saveConfig();
        outLog("信息：私聊发送间隔已改为: " + seconds + " 秒");

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
        outLog("信息：消息发送间隔已改为: " + seconds + " 秒");
    }

    // 添加黑名单管理方法
    public void cmdAddToBlacklist(String playerName) {
        config.addToBlacklist(playerName);
        saveConfig();
        PrivateMessageSender.forceUpdate(); // 强制刷新缓存
        outLog("信息：已将玩家 " + playerName + " 添加到私聊黑名单");
    }

    public void cmdRemoveFromBlacklist(String playerName) {
        config.removeFromBlacklist(playerName);
        saveConfig();
        PrivateMessageSender.forceUpdate(); // 强制刷新缓存
        outLog("信息：已将玩家 " + playerName + " 从私聊黑名单中移除");
    }

    public void cmdListBlacklist() {
        List<String> blacklist = config.getPrivateMessageBlacklist();
        if (blacklist.isEmpty()) {
            outLog("信息：私聊黑名单为空");
        } else {
            outLog("信息：私聊黑名单玩家列表: " + String.join(", ", blacklist));
        }
    }

    public void cmdReload() {
        // ✅ 先停止当前任务
        stopScheduler();

        loadConfig();
        PrivateMessageSender.forceUpdate(); // 强制刷新缓存
        outLog("信息：配置文件已重载");

        // ✅ 重新启动任务（如果需要）
        if (config.isEnabled()) {
            startScheduler();
        }
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