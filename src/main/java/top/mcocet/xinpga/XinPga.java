package top.mcocet.xinpga;

import top.mcocet.xinpga.service.PrivateMessageSender;
import top.mcocet.xinpga.service.MessageScheduler;
import top.mcocet.xinpga.service.CommandService;
import top.mcocet.xinpga.service.MultiThreadAnnouncementSender; // 导入新的多线程发送器
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.LangManager;
import xin.bbtt.mcbot.event.EventHandler;
import xin.bbtt.mcbot.event.Listener;
import xin.bbtt.mcbot.events.LoginSuccessEvent;
import xin.bbtt.mcbot.plugin.Plugin;
import xin.bbtt.mcbot.events.PrivateChatEvent;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.command.Command;
import top.mcocet.xinpga.command.XpaCommandExecutor;
import top.mcocet.xinpga.config.XinPgaConfig;

public class XinPga implements Plugin, Listener {
    private static final Logger logger = LoggerFactory.getLogger("XinPgaMain");
    public volatile boolean isRunning = false;
    public volatile boolean isSuspended = false;

    public static XinPga INSTANCE;

    private XinPgaConfig config;
    private MessageScheduler scheduler;
    private CommandService commandService;
    private final Path configPath = Paths.get("plugin", "XinPga", "config.json");
    private final Random random = new Random();

    public XinPga() {
        INSTANCE = this;
    }

    public String getName() {
        return ("XinPga");
    }

    public String getVersion() {
        return "1.9.4";
    }

    @Override
    public void onLoad() {
        LangManager.initLang(XinPga.class.getClassLoader());
        logger.info(LangManager.get("xinpga.plugin.loaded"));
    }

    @Override
    public void onEnable() {
        logger.info(LangManager.get("xinpga.plugin.enabled"));
        logger.info(LangManager.get("xinpga.plugin.version", getVersion()));

        loadConfig();

        Bot.INSTANCE.getPluginManager().events().registerEvents(this, this);
        Bot.INSTANCE.getPluginManager().registerCommand(new Command("xpa", new String[]{"xpa", "xinpga"}, "xinpga.command.description", "/xpa start|stop|forcestop|string <编号> <文本>|addmessage <消息>|removemessage <消息>|listmessages|time <秒>|mode <PUBLIC|PRIVATE>|privateinterval <秒>|messageinterval <秒>|randomsending <on|off>|greeting <enable|disable|format> [格式]|reload|debug|blacklist add <玩家名>|blacklist remove <玩家名>|blacklist list|admin add <玩家名>|admin remove <玩家名>|admin list|updateplayerlist|debug"), new XpaCommandExecutor(), this);

        this.scheduler = new MessageScheduler(this, config);
        this.commandService = new CommandService(this, config, scheduler);

        if (config.isEnabled()) {
            scheduler.start();
        }
        
        // 设置多线程发送器的bot名称
        MultiThreadAnnouncementSender.setBotName(Bot.INSTANCE.getProtocol().getProfile().getName());
    }

    @Override
    public void onDisable() {
        if (scheduler != null) {
            scheduler.stop();
        }
        logger.info(LangManager.get("xinpga.plugin.disabled"));
    }

    @Override
    public void onUnload() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        logger.info(LangManager.get("xinpga.plugin.unloaded"));
    }

    @EventHandler
    public void onLogin(LoginSuccessEvent event) {
        PrivateMessageSender.setBotName(Bot.INSTANCE.getProtocol().getProfile().getName());
        MultiThreadAnnouncementSender.setBotName(Bot.INSTANCE.getProtocol().getProfile().getName());
        if (config.isEnabled() && !isRunning) {
            scheduler.start();
        }
    }

    public String randomString(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        return random.ints(len, 0, chars.length())
                .mapToObj(chars::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    public void loadConfig() {
        try {
            config = new XinPgaConfig(configPath);
            config.loadConfig();
        } catch (Exception e) {
            logger.error(LangManager.get("xinpga.config.load.error", e.getMessage()));
            try {
                config = new XinPgaConfig(configPath);
                config.saveConfig();
                logger.info(LangManager.get("xinpga.config.create.success"));
            } catch (Exception ex) {
                throw new RuntimeException(LangManager.get("xinpga.config.create.error", ex.getMessage()), ex);
            }
        }
    }

    // 处理私聊消息事件 - 监听远程命令
    @EventHandler
    public void onPrivateMessage(xin.bbtt.mcbot.events.PrivateChatEvent event) {
        try {
            // 检查远程命令是否启用
            if (!config.isRemoteCommandEnabled()) {
                return; // 远程命令已禁用，忽略
            }

            String playerName = event.getSender().getName();
            String message = event.getMessage().trim();

            // 检查玩家是否是管理员
            if (!config.isAdministrator(playerName)) {
                return; // 非管理员，忽略
            }

            // 检查消息是否以命令关键字开头
            String commandPrefix = null;
            if (message.startsWith("#command xpa ")) {
                commandPrefix = "#command xpa ";
            } else if (message.startsWith("#cmd xpa ")) {
                commandPrefix = "#cmd xpa ";
            }

            if (commandPrefix != null) {
                // 提取命令部分
                String command = message.substring(commandPrefix.length());
                outLog(LangManager.get("xinpga.remote.command.received", playerName, command));

                // 异步执行命令，避免阻塞事件线程
                CompletableFuture.runAsync(() -> {
                    executeRemoteCommand(playerName, command);
                });
            }
        } catch (Exception e) {
            outError(LangManager.get("xinpga.remote.command.private.message.error", e.getMessage()));
        }
    }

     // 执行远程命令并返回结果
    private void executeRemoteCommand(String playerName, String command) {
        List<String> output = new ArrayList<>();

        try {
            // 暂停公告任务
            if (isRunning && getConfig().getSendMode() == SendMode.PUBLIC) {
                isSuspended = true;
                output.add(LangManager.get("xinpga.remote.command.task.suspended"));
                
                // 如果多线程发送也在运行，也暂停它
                if (MultiThreadAnnouncementSender.isMultiThreadRunning()) {
                    output.add(LangManager.get("xinpga.remote.command.multithread.suspended"));
                }
            }

            // 执行命令
            String[] args = command.split("\\s+");
            if (args.length > 0) {
                XpaCommandExecutor executor = new XpaCommandExecutor();
                List<String> commandOutput = executor.onCommandWithOutput(new Command("xpa", new String[]{"xpa", "xinpga"}, "xinpga.command.description", "/xpa start|stop|forcestop|string <编号> <文本>|addmessage <消息>|removemessage <消息>|listmessages|time <秒>|mode <PUBLIC|PRIVATE>|privateinterval <秒>|messageinterval <秒>|randomsending <on|off>|greeting <enable|disable|format> [格式]|reload|debug|blacklist add <玩家名>|blacklist remove <玩家名>|blacklist list|admin add <玩家名>|admin remove <玩家名>|admin list|updateplayerlist|debug"), "xpa", args);
                output.addAll(commandOutput);
            } else {
                output.add(LangManager.get("xinpga.remote.command.format.error"));
            }

        } catch (Exception e) {
            output.add(LangManager.get("xinpga.remote.command.execute.error", e.getMessage()));
            outError(LangManager.get("xinpga.remote.command.execute.failed", e.getMessage()));
        } finally {
            // 恢复公告任务
            if (isSuspended && getConfig().getSendMode() == SendMode.PUBLIC) {
                isSuspended = false;
                output.add(LangManager.get("xinpga.remote.command.task.resumed"));
            }
        }

        // 通过私聊发送所有输出结果给管理员
        sendCommandResultsToAdmin(playerName, output);
    }

     // 通过私聊发送命令结果给管理员
    private void sendCommandResultsToAdmin(String playerName, List<String> results) {
        if (results.isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                for (String line : results) {
                    if (!line.trim().isEmpty()) {
                        // 添加延迟，避免消息发送过快
                        Thread.sleep(200);
                        Bot.INSTANCE.sendCommand("msg " + playerName + " " + line);
                    }
                }
                outLog(LangManager.get("xinpga.remote.command.result.sent", playerName));
            } catch (Exception e) {
                outError(LangManager.get("xinpga.remote.command.send.result.failed", e.getMessage()));
            }
        }).start();
    }

    public XinPgaConfig getConfig() {
        return config;
    }

    public MessageScheduler getScheduler() {
        return scheduler;
    }

    public CommandService getCommandService() {
        return commandService;
    }

    public String getBotName() {
        return Bot.INSTANCE.getProtocol().getProfile().getName();
    }

    // 命令方法

    public void cmdForceStop(){
        if (scheduler != null) {
            scheduler.forceStop();
        }
        // 强制设置运行状态为false
        isRunning = false;
        
        // 同时停止多线程发送功能
        MultiThreadAnnouncementSender.stopMultiThreadSending();
    }
    public void cmdStart() {
        if (commandService != null) {
            commandService.handleStart();
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdStop() {
        if (commandService != null) {
            commandService.handleStop();
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
        
        // 同时停止多线程发送功能
        MultiThreadAnnouncementSender.stopMultiThreadSending();
    }

    public void cmdString(int index, String text) {
        if (commandService != null) {
            commandService.handleString(index, text);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdAddMessage(String message) {
        if (commandService != null) {
            commandService.handleAddMessage(message);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdRemoveMessage(String message) {
        if (commandService != null) {
            commandService.handleRemoveMessage(message);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdListMessages() {
        if (commandService != null) {
            commandService.handleListMessages();
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdTime(int seconds) {
        if (commandService != null) {
            commandService.handleTime(seconds);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdSendMode(String mode) {
        if (commandService != null) {
            commandService.handleSendMode(mode);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdPrivateInterval(int seconds) {
        if (commandService != null) {
            commandService.handlePrivateInterval(seconds);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdMessageInterval(int seconds) {
        if (commandService != null) {
            commandService.handleMessageInterval(seconds);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdAddToBlacklist(String playerName) {
        if (commandService != null) {
            commandService.handleAddToBlacklist(playerName);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdRemoveFromBlacklist(String playerName) {
        if (commandService != null) {
            commandService.handleRemoveFromBlacklist(playerName);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdListBlacklist() {
        if (commandService != null) {
            commandService.handleListBlacklist();
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdAddAdministrator(String playerName) {
        if (commandService != null) {
            commandService.handleAddAdministrator(playerName);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdRemoveAdministrator(String playerName) {
        if (commandService != null) {
            commandService.handleRemoveAdministrator(playerName);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdListAdministrators() {
        if (commandService != null) {
            commandService.handleListAdministrators();
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdReload() {
        if (commandService != null) {
            commandService.handleReload();
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdUpdatePlayerList() {
        if (commandService != null) {
            commandService.handleUpdatePlayerList();
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdDebugPlayerList() {
        if (commandService != null) {
            commandService.handleDebugPlayerList();
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdSetRandomSending(boolean enabled) {
        if (commandService != null) {
            commandService.handleSetRandomSending(enabled);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdSetGreetingEnabled(boolean enabled) {
        if (commandService != null) {
            commandService.handleSetGreetingEnabled(enabled);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdSetGreetingFormat(String format) {
        if (commandService != null) {
            commandService.handleSetGreetingFormat(format);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void outLog(String log) {
        logger.info(log);
    }

    public void outError(String log) {
        logger.error(log);
    }

    public void outWarn(String log) {
        logger.warn(log);
    }

    public enum SendMode {
        PUBLIC, PRIVATE
    }
    
    // 多线程发送功能相关的命令方法
    public void cmdStartMultiThread() {
        if (getConfig().getSendMode() != SendMode.PRIVATE) {
            outWarn(LangManager.get("xinpga.multithread.mode.warning"));
            return;
        }
        
        if (!isRunning) {
            outWarn(LangManager.get("xinpga.multithread.not.started.warning"));
            return;
        }
        
        MultiThreadAnnouncementSender.startMultiThreadSending();
    }

    public void cmdStopMultiThread() {
        MultiThreadAnnouncementSender.stopMultiThreadSending();
    }
    
    // 数字替换功能相关的命令方法
    public void cmdSetNumberReplacementEnabled(boolean enabled) {
        getConfig().setNumberReplacementEnabled(enabled);
        try {
            getConfig().saveConfig();
            outLog(LangManager.get("xinpga.number.replace.set.success", LangManager.get(enabled ? "xinpga.status.enabled" : "xinpga.status.disabled")));
        } catch (Exception e) {
            outError(LangManager.get("xinpga.config.save.error", e.getMessage()));
        }
    }
    
    // 主发送模式数字替换功能相关的命令方法
    public void cmdSetMainNumberReplacementEnabled(boolean enabled) {
        getConfig().setMainNumberReplacementEnabled(enabled);
        try {
            getConfig().saveConfig();
            outLog(LangManager.get("xinpga.number.replace.set.success", LangManager.get(enabled ? "xinpga.status.enabled" : "xinpga.status.disabled")));
        } catch (Exception e) {
            outError(LangManager.get("xinpga.config.save.error", e.getMessage()));
        }
    }

    public void cmdSetMinConsecutiveNumbers(int minConsecutiveNumbers) {
        if (minConsecutiveNumbers <= 0) {
            outWarn(LangManager.get("xinpga.number.replace.min.error"));
            return;
        }
        getConfig().setMinConsecutiveNumbers(minConsecutiveNumbers);
        try {
            getConfig().saveConfig();
            outLog(LangManager.get("xinpga.number.replace.min.set.success", minConsecutiveNumbers));
        } catch (Exception e) {
            outError(LangManager.get("xinpga.config.save.error", e.getMessage()));
        }
    }

    public void cmdSetRandomIntervalEnabled(boolean enabled) {
        getConfig().setRandomIntervalEnabled(enabled);
        try {
            getConfig().saveConfig();
            outLog(LangManager.get("xinpga.random.interval.set.success", LangManager.get(enabled ? "xinpga.status.enabled" : "xinpga.status.disabled")));
        } catch (Exception e) {
            outError(LangManager.get("xinpga.config.save.error", e.getMessage()));
        }
    }
    
    // 多线程发送间隔相关的命令方法
    public void cmdMultiThreadInterval(int seconds) {
        getConfig().setMultiThreadInterval(seconds);
        try {
            getConfig().saveConfig();
            outLog(LangManager.get("xinpga.multithread.interval.set.success", seconds));
        } catch (Exception e) {
            outError(LangManager.get("xinpga.config.save.error", e.getMessage()));
        }
    }
    
    // 同步更新多线程消息相关的命令方法
    public void cmdSetSyncMultiThreadMessages(boolean enabled) {
        getConfig().setSyncMultiThreadMessages(enabled);
        try {
            getConfig().saveConfig();
            outLog(LangManager.get("xinpga.sync.messages.set.success", LangManager.get(enabled ? "xinpga.status.enabled" : "xinpga.status.disabled")));
        } catch (Exception e) {
            outError(LangManager.get("xinpga.config.save.error", e.getMessage()));
        }
    }
    
    // 多线程消息相关的命令方法
    public void cmdMultiThreadString(int index, String text) {
        if (commandService != null) {
            commandService.handleMultiThreadString(index, text);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdMultiThreadAddMessage(String message) {
        if (commandService != null) {
            commandService.handleMultiThreadAddMessage(message);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdMultiThreadRemoveMessage(String message) {
        if (commandService != null) {
            commandService.handleMultiThreadRemoveMessage(message);
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }

    public void cmdMultiThreadListMessages() {
        if (commandService != null) {
            commandService.handleMultiThreadListMessages();
        } else {
            logger.error(LangManager.get("xinpga.command.service.not.initialized"));
        }
    }
}