package top.mcocet.xinpga;

import top.mcocet.xinpga.service.PrivateMessageSender;
import top.mcocet.xinpga.service.MessageScheduler;
import top.mcocet.xinpga.service.CommandService;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.event.EventHandler;
import xin.bbtt.mcbot.event.Listener;
import xin.bbtt.mcbot.events.LoginSuccessEvent;
import xin.bbtt.mcbot.plugin.Plugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import top.mcocet.xinpga.command.XpaCommand;
import top.mcocet.xinpga.command.XpaCommandExecutor;
import top.mcocet.xinpga.config.XinPgaConfig;

public class XinPga implements Plugin, Listener {
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

    @Override
    public void onLoad() {
        getLogger().info("XinPga 插件已加载");
    }

    @Override
    public void onEnable() {
        getLogger().info("XinPga 插件已启用");
        getLogger().info("XinPga 版本: v1.5.2.2");

        loadConfig();

        Bot.Instance.getPluginManager().events().registerEvents(this, this);
        Bot.Instance.getPluginManager().registerCommand(new XpaCommand(), new XpaCommandExecutor(), this);

        this.scheduler = new MessageScheduler(this, config);
        this.commandService = new CommandService(this, config, scheduler);

        if (config.isEnabled()) {
            scheduler.start();
        }
    }

    @Override
    public void onDisable() {
        if (scheduler != null) {
            scheduler.stop();
        }
        getLogger().info("XinPga 插件已关闭");
    }

    @Override
    public void onUnload() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        getLogger().info("XinPga 插件已卸载");
    }

    @EventHandler
    public void onLogin(LoginSuccessEvent event) {
        PrivateMessageSender.setBotName(Bot.Instance.getProtocol().getProfile().getName());
        if (config.isEnabled() && !isRunning) {
            scheduler.start();
        }
    }

    // 在 XinPga.java 文件中找到 randomString 方法，将其替换为以下内容：

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
            getLogger().error("无法加载配置文件: " + e.getMessage());
            try {
                config = new XinPgaConfig(configPath);
                config.saveConfig();
                getLogger().info("已创建默认配置文件");
            } catch (Exception ex) {
                throw new RuntimeException("无法创建默认配置文件: " + ex.getMessage(), ex);
            }
        }
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
        return Bot.Instance.getProtocol().getProfile().getName();
    }

    // 命令方法
    public void cmdStart() {
        if (commandService != null) {
            commandService.handleStart();
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdStop() {
        if (commandService != null) {
            commandService.handleStop();
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdString(int index, String text) {
        if (commandService != null) {
            commandService.handleString(index, text);
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdAddMessage(String message) {
        if (commandService != null) {
            commandService.handleAddMessage(message);
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdRemoveMessage(String message) {
        if (commandService != null) {
            commandService.handleRemoveMessage(message);
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdListMessages() {
        if (commandService != null) {
            commandService.handleListMessages();
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdTime(int seconds) {
        if (commandService != null) {
            commandService.handleTime(seconds);
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdSendMode(String mode) {
        if (commandService != null) {
            commandService.handleSendMode(mode);
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdPrivateInterval(int seconds) {
        if (commandService != null) {
            commandService.handlePrivateInterval(seconds);
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdMessageInterval(int seconds) {
        if (commandService != null) {
            commandService.handleMessageInterval(seconds);
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdAddToBlacklist(String playerName) {
        if (commandService != null) {
            commandService.handleAddToBlacklist(playerName);
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdRemoveFromBlacklist(String playerName) {
        if (commandService != null) {
            commandService.handleRemoveFromBlacklist(playerName);
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdListBlacklist() {
        if (commandService != null) {
            commandService.handleListBlacklist();
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdAddAdministrator(String playerName) {
        if (commandService != null) {
            commandService.handleAddAdministrator(playerName);
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdRemoveAdministrator(String playerName) {
        if (commandService != null) {
            commandService.handleRemoveAdministrator(playerName);
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdListAdministrators() {
        if (commandService != null) {
            commandService.handleListAdministrators();
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdReload() {
        if (commandService != null) {
            commandService.handleReload();
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdUpdatePlayerList() {
        if (commandService != null) {
            commandService.handleUpdatePlayerList();
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void cmdDebugPlayerList() {
        if (commandService != null) {
            commandService.handleDebugPlayerList();
        } else {
            getLogger().error("CommandService 未初始化");
        }
    }

    public void outLog(String log) {
        getLogger().info(log);
    }

    public void outError(String log) {
        getLogger().error(log);
    }

    public void outWarn(String log) {
        getLogger().warn(log);
    }

    public enum SendMode {
        PUBLIC, PRIVATE
    }
}