package top.mcocet.xinpga.service;

import top.mcocet.xinpga.XinPga;
import top.mcocet.xinpga.config.XinPgaConfig;
import xin.bbtt.mcbot.LangManager;

import java.util.List;

public class CommandService {
    private final XinPga xinPga;
    private final XinPgaConfig config;
    private final MessageScheduler scheduler;

    public CommandService(XinPga xinPga, XinPgaConfig config, MessageScheduler scheduler) {
        this.xinPga = xinPga;
        this.config = config;
        this.scheduler = scheduler;
    }

    public void handleStart() {
        if (xinPga.isRunning) {
            xinPga.outLog(LangManager.get("xinpga.command.already.running"));
            return;
        }
        config.setEnabled(true);
        saveConfig();
        scheduler.start();
        xinPga.outLog(LangManager.get("xinpga.command.started"));
    }

    public void handleStop() {
        if (!xinPga.isRunning) {
            xinPga.outLog(LangManager.get("xinpga.command.not.running"));
            return;
        }
        config.setEnabled(false);
        saveConfig();
        scheduler.stop();
        xinPga.outLog(LangManager.get("xinpga.command.stopped"));
    }

    public void handleString(int index, String text) {
        List<String> messages = config.getMessages();
        if (index >= 0 && index < messages.size()) {
            messages.set(index, text);
            config.setMessages(messages);
            
            // 如果启用了同步更新多线程消息列表，则同步更新
            if (config.isSyncMultiThreadMessages()) {
                List<String> multiThreadMessages = config.getMultiThreadMessages();
                if (index < multiThreadMessages.size()) {
                    multiThreadMessages.set(index, text);
                    config.setMultiThreadMessages(multiThreadMessages);
                }
            }
            
            saveConfig();
            xinPga.outLog(LangManager.get("xinpga.command.string.updated", index + 1, text));
        } else {
            xinPga.outError(LangManager.get("xinpga.command.string.index.error", messages.size()));
        }
    }

    public void handleAddMessage(String message) {
        config.addMessage(message);
        
        // 如果启用了同步更新多线程消息列表，则同步添加到多线程消息列表
        if (config.isSyncMultiThreadMessages()) {
            config.addMultiThreadMessage(message);
        }
        
        saveConfig();
        xinPga.outLog(LangManager.get("xinpga.command.message.added", message));
    }

    public void handleRemoveMessage(String message) {
        config.removeMessage(message);
        saveConfig();
        xinPga.outLog(LangManager.get("xinpga.command.message.removed", message));
    }

    public void handleListMessages() {
        List<String> messages = config.getMessages();
        if (messages.isEmpty()) {
            xinPga.outLog(LangManager.get("xinpga.command.list.empty"));
        } else {
            xinPga.outLog(LangManager.get("xinpga.command.list.header"));
            for (int i = 0; i < messages.size(); i++) {
                xinPga.outLog((i + 1) + ". " + messages.get(i));
            }
        }
        
        // 总是显示多线程消息列表，不管同步设置如何
        List<String> multiThreadMessages = config.getMultiThreadMessages();
        if (multiThreadMessages.isEmpty()) {
            xinPga.outLog(LangManager.get("xinpga.command.multithread.list.empty"));
        } else {
            xinPga.outLog(LangManager.get("xinpga.command.multithread.list.header"));
            for (int i = 0; i < multiThreadMessages.size(); i++) {
                xinPga.outLog((i + 1) + ". " + multiThreadMessages.get(i));
            }
        }
    }

    public void handleTime(int seconds) {
        config.setIntervalSeconds(seconds);
        saveConfig();
        xinPga.outLog(LangManager.get("xinpga.command.time.updated", seconds));
        if (xinPga.isRunning) {
            scheduler.stop();
            scheduler.start();
        }
    }

    public void handleSendMode(String mode) {
        try {
            XinPga.SendMode sendMode = XinPga.SendMode.valueOf(mode.toUpperCase());
            config.setSendMode(sendMode);
            saveConfig();
            xinPga.outLog(LangManager.get("xinpga.command.mode.updated", mode));

            if (xinPga.isRunning) {
                scheduler.stop();
                scheduler.start();
            }
        } catch (IllegalArgumentException e) {
            xinPga.outError(LangManager.get("xinpga.command.mode.invalid", mode, "PUBLIC, PRIVATE"));
        }
    }

    public void handlePrivateInterval(int seconds) {
        config.setPrivateMessageInterval(seconds);
        saveConfig();
        xinPga.outLog(LangManager.get("xinpga.command.private.interval.updated", seconds));

        if (xinPga.isRunning && config.getSendMode() == XinPga.SendMode.PRIVATE) {
            scheduler.stop();
            scheduler.start();
        }
    }

    public void handleMessageInterval(int seconds) {
        config.setMessageInterval(seconds);
        saveConfig();
        xinPga.outLog(LangManager.get("xinpga.command.message.interval.updated", seconds));
    }

    public void handleAddToBlacklist(String playerName) {
        config.addToBlacklist(playerName);
        saveConfig();
        PrivateMessageSender.forceUpdate();
        xinPga.outLog(LangManager.get("xinpga.command.blacklist.added", playerName));
    }

    public void handleRemoveFromBlacklist(String playerName) {
        config.removeFromBlacklist(playerName);
        saveConfig();
        PrivateMessageSender.forceUpdate();
        xinPga.outLog(LangManager.get("xinpga.command.blacklist.removed", playerName));
    }

    public void handleListBlacklist() {
        List<String> blacklist = config.getPrivateMessageBlacklist();
        if (blacklist.isEmpty()) {
            xinPga.outLog(LangManager.get("xinpga.command.blacklist.empty"));
        } else {
            xinPga.outLog(LangManager.get("xinpga.command.blacklist.header", String.join(", ", blacklist)));
        }
    }

    public void handleAddAdministrator(String playerName) {
        config.addAdministrator(playerName);
        saveConfig();
        xinPga.outLog(LangManager.get("xinpga.command.admin.added", playerName));
    }

    public void handleRemoveAdministrator(String playerName) {
        config.removeAdministrator(playerName);
        saveConfig();
        xinPga.outLog(LangManager.get("xinpga.command.admin.removed", playerName));
    }

    public void handleListAdministrators() {
        List<String> admins = config.getAdministrators();
        if (admins.isEmpty()) {
            xinPga.outLog(LangManager.get("xinpga.command.admin.empty"));
        } else {
            xinPga.outLog(LangManager.get("xinpga.command.admin.header", String.join(", ", admins)));
        }
    }

    public void handleReload() {
        scheduler.stop();
        xinPga.loadConfig();
        if (config.getSendMode() == XinPga.SendMode.PRIVATE) {
            PrivateMessageSender.forceUpdate();
        }
        xinPga.outLog(LangManager.get("xinpga.command.reload"));

        if (config.isEnabled()) {
            scheduler.start();
        }
    }

    public void handleUpdatePlayerList() {
        PrivateMessageSender.updateOnlinePlayerList();
        xinPga.outLog(LangManager.get("xinpga.command.playerlist.updated"));
    }

    public void handleDebugPlayerList() {
        PrivateMessageSender.printPlayerListStatus();
        xinPga.outLog(LangManager.get("xinpga.command.bot.name", xinPga.getBotName()));
    }

    public void handleSetRandomSending(boolean enabled) {
        config.setRandomSendingEnabled(enabled);
        saveConfig();
        xinPga.outLog(LangManager.get("xinpga.command.random.sending.updated", LangManager.get(enabled ? "xinpga.status.enabled" : "xinpga.status.disabled")));
        
        // 如果正在运行，重启调度器以应用更改
        if (xinPga.isRunning) {
            scheduler.stop();
            scheduler.start();
        }
    }

    public void handleSetGreetingEnabled(boolean enabled) {
        config.setGreetingEnabled(enabled);
        saveConfig();
        xinPga.outLog(LangManager.get("xinpga.command.greeting.updated", LangManager.get(enabled ? "xinpga.status.enabled" : "xinpga.status.disabled")));
    }

    public void handleSetGreetingFormat(String format) {
        config.setGreetingFormat(format);
        saveConfig();
        xinPga.outLog(LangManager.get("xinpga.command.greeting.format.updated", format));
    }
    
    public void handleMultiThreadInterval(int seconds) {
        config.setMultiThreadInterval(seconds);
        saveConfig();
        xinPga.outLog(LangManager.get("xinpga.multithread.interval.set.success", seconds));
    }
    
    // 多线程消息处理方法
    public void handleMultiThreadString(int index, String text) {
        List<String> messages = config.getMultiThreadMessages();
        if (index >= 0 && index < messages.size()) {
            messages.set(index, text);
            config.setMultiThreadMessages(messages);
            saveConfig();
            xinPga.outLog(LangManager.get("xinpga.command.multithread.string.updated", index + 1, text));
        } else {
            xinPga.outError(LangManager.get("xinpga.command.multithread.string.index.error", messages.size()));
        }
    }

    public void handleMultiThreadAddMessage(String message) {
        config.addMultiThreadMessage(message);
        saveConfig();
        xinPga.outLog(LangManager.get("xinpga.command.multithread.message.added", message));
    }

    public void handleMultiThreadRemoveMessage(String message) {
        boolean removed = config.getMultiThreadMessages().remove(message);
        if (removed) {
            saveConfig();
            xinPga.outLog(LangManager.get("xinpga.command.multithread.message.removed", message));
        } else {
            xinPga.outLog(LangManager.get("xinpga.command.multithread.message.not.found", message));
        }
    }

    public void handleMultiThreadListMessages() {
        List<String> messages = config.getMultiThreadMessages();
        if (messages.isEmpty()) {
            xinPga.outLog(LangManager.get("xinpga.command.multithread.list.empty"));
        } else {
            xinPga.outLog(LangManager.get("xinpga.command.multithread.list.header"));
            for (int i = 0; i < messages.size(); i++) {
                xinPga.outLog((i + 1) + ". " + messages.get(i));
            }
        }
    }

    public void handleSetSyncMultiThreadMessages(boolean enabled) {
        config.setSyncMultiThreadMessages(enabled);
        saveConfig();
        xinPga.outLog(LangManager.get("xinpga.command.sync.updated", LangManager.get(enabled ? "xinpga.status.enabled" : "xinpga.status.disabled")));
    }
    
    private void saveConfig() {
        try {
            config.saveConfig();
        } catch (Exception e) {
            throw new RuntimeException(LangManager.get("xinpga.config.save.error.generic"), e);
        }
    }
}