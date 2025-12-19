package top.mcocet.xinpga.service;

import top.mcocet.xinpga.XinPga;
import top.mcocet.xinpga.config.XinPgaConfig;

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
            xinPga.outLog("任务：任务已经在运行中！");
            return;
        }
        config.setEnabled(true);
        saveConfig();
        scheduler.start();
        xinPga.outLog("任务：已启动定时发送");
    }

    public void handleStop() {
        if (!xinPga.isRunning) {
            xinPga.outLog("任务：任务未运行！");
            return;
        }
        config.setEnabled(false);
        saveConfig();
        scheduler.stop();
        xinPga.outLog("任务：已停止定时发送");
    }

    public void handleString(int index, String text) {
        List<String> messages = config.getMessages();
        if (index >= 0 && index < messages.size()) {
            messages.set(index, text);
            config.setMessages(messages);
            saveConfig();
            xinPga.outLog("信息：第 " + (index + 1) + " 条发送内容已改为: " + text);
        } else {
            xinPga.outError("错误：编号超出范围，当前共有 " + messages.size() + " 条消息");
        }
    }

    public void handleAddMessage(String message) {
        config.addMessage(message);
        saveConfig();
        xinPga.outLog("信息：已添加消息: " + message);
    }

    public void handleRemoveMessage(String message) {
        config.removeMessage(message);
        saveConfig();
        xinPga.outLog("信息：已移除消息: " + message);
    }

    public void handleListMessages() {
        List<String> messages = config.getMessages();
        if (messages.isEmpty()) {
            xinPga.outLog("信息：消息列表为空");
        } else {
            xinPga.outLog("信息：消息列表:");
            for (int i = 0; i < messages.size(); i++) {
                xinPga.outLog((i + 1) + ". " + messages.get(i));
            }
        }
    }

    public void handleTime(int seconds) {
        config.setIntervalSeconds(seconds);
        saveConfig();
        xinPga.outLog("信息：发送间隔已改为: " + seconds + " 秒");
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
            xinPga.outLog("信息：发送模式已改为: " + mode);

            if (xinPga.isRunning) {
                scheduler.stop();
                scheduler.start();
            }
        } catch (IllegalArgumentException e) {
            xinPga.outError("警告：无效的发送模式: " + mode + "，有效值为: PUBLIC, PRIVATE");
        }
    }

    public void handlePrivateInterval(int seconds) {
        config.setPrivateMessageInterval(seconds);
        saveConfig();
        xinPga.outLog("信息：私聊发送间隔已改为: " + seconds + " 秒");

        if (xinPga.isRunning && config.getSendMode() == XinPga.SendMode.PRIVATE) {
            scheduler.stop();
            scheduler.start();
        }
    }

    public void handleMessageInterval(int seconds) {
        config.setMessageInterval(seconds);
        saveConfig();
        xinPga.outLog("信息：消息发送间隔已改为: " + seconds + " 秒");
    }

    public void handleAddToBlacklist(String playerName) {
        config.addToBlacklist(playerName);
        saveConfig();
        PrivateMessageSender.forceUpdate();
        xinPga.outLog("信息：已将玩家 " + playerName + " 添加到私聊黑名单");
    }

    public void handleRemoveFromBlacklist(String playerName) {
        config.removeFromBlacklist(playerName);
        saveConfig();
        PrivateMessageSender.forceUpdate();
        xinPga.outLog("信息：已将玩家 " + playerName + " 从私聊黑名单中移除");
    }

    public void handleListBlacklist() {
        List<String> blacklist = config.getPrivateMessageBlacklist();
        if (blacklist.isEmpty()) {
            xinPga.outLog("信息：私聊黑名单为空");
        } else {
            xinPga.outLog("信息：私聊黑名单玩家列表: " + String.join(", ", blacklist));
        }
    }

    public void handleAddAdministrator(String playerName) {
        config.addAdministrator(playerName);
        saveConfig();
        xinPga.outLog("信息：已将玩家 " + playerName + " 添加到管理员列表");
    }

    public void handleRemoveAdministrator(String playerName) {
        config.removeAdministrator(playerName);
        saveConfig();
        xinPga.outLog("信息：已将玩家 " + playerName + " 从管理员列表中移除");
    }

    public void handleListAdministrators() {
        List<String> admins = config.getAdministrators();
        if (admins.isEmpty()) {
            xinPga.outLog("信息：管理员列表为空");
        } else {
            xinPga.outLog("信息：管理员列表: " + String.join(", ", admins));
        }
    }

    public void handleReload() {
        scheduler.stop();
        xinPga.loadConfig();
        if (config.getSendMode() == XinPga.SendMode.PRIVATE) {
            PrivateMessageSender.forceUpdate();
        }
        xinPga.outLog("信息：配置文件已重载");

        if (config.isEnabled()) {
            scheduler.start();
        }
    }

    public void handleUpdatePlayerList() {
        PrivateMessageSender.updateOnlinePlayerList();
        xinPga.outLog("信息：已手动更新在线玩家列表");
    }

    public void handleDebugPlayerList() {
        PrivateMessageSender.printPlayerListStatus();
        xinPga.outLog("当前Bot名称: " + xinPga.getBotName());
    }

    public void handleSetRandomSending(boolean enabled) {
        config.setRandomSendingEnabled(enabled);
        saveConfig();
        xinPga.outLog("信息：随机发送模式已" + (enabled ? "启用" : "禁用"));
        
        // 如果正在运行，重启调度器以应用更改
        if (xinPga.isRunning) {
            scheduler.stop();
            scheduler.start();
        }
    }

    public void handleSetGreetingEnabled(boolean enabled) {
        config.setGreetingEnabled(enabled);
        saveConfig();
        xinPga.outLog("信息：问候语功能已" + (enabled ? "启用" : "禁用"));
    }

    public void handleSetGreetingFormat(String format) {
        config.setGreetingFormat(format);
        saveConfig();
        xinPga.outLog("信息：问候语格式已设置为: " + format);
    }

    private void saveConfig() {
        try {
            config.saveConfig();
        } catch (Exception e) {
            throw new RuntimeException("错误：无法保存配置", e);
        }
    }
}