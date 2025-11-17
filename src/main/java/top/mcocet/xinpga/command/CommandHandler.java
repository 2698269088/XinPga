package top.mcocet.xinpga.command;

import xin.bbtt.mcbot.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import top.mcocet.xinpga.XinPga;

public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    public void handleCommand(Command cmd, String label, String[] args) {
        switch (args[0].toLowerCase()) {
            case "start" -> XinPga.INSTANCE.cmdStart();
            case "stop" -> XinPga.INSTANCE.cmdStop();
            case "string" -> handleStringCommand(args);
            case "addmessage" -> handleAddMessageCommand(args);
            case "removemessage" -> handleRemoveMessageCommand(args);
            case "listmessages" -> XinPga.INSTANCE.cmdListMessages();
            case "time" -> handleTimeCommand(args);
            case "mode" -> handleModeCommand(args);
            case "privateinterval" -> handlePrivateIntervalCommand(args);
            case "messageinterval" -> handleMessageIntervalCommand(args);
            case "reload" -> XinPga.INSTANCE.cmdReload();
            case "help" -> showHelp();
            case "updateplayerlist" -> XinPga.INSTANCE.cmdUpdatePlayerList();
            case "debug" -> XinPga.INSTANCE.cmdDebugPlayerList();
            case "admin" -> handleAdminCommand(args);
            case "blacklist" -> handleBlacklistCommand(args);
            default -> log.warn("未知子命令: " + args[0] + "！用法: " + cmd.getUsage());
        }
    }


    private void handleStringCommand(String[] args) {
        if (args.length < 3) {
            log.info("用法: /xpa string <编号> <新文本>");
            return;
        }
        try {
            int index = Integer.parseInt(args[1]) - 1;
            String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            XinPga.INSTANCE.cmdString(index, text);
        } catch (NumberFormatException e) {
            log.warn("编号必须是整数！");
        }
    }

    private void handleAddMessageCommand(String[] args) {
        if (args.length < 2) {
            log.info("用法: /xpa addmessage <消息内容>");
            return;
        }
        XinPga.INSTANCE.cmdAddMessage(String.join(" ", args).substring(args[0].length() + 1));
    }

    private void handleRemoveMessageCommand(String[] args) {
        if (args.length < 2) {
            log.info("用法: /xpa removemessage <消息内容>");
            return;
        }
        XinPga.INSTANCE.cmdRemoveMessage(String.join(" ", args).substring(args[0].length() + 1));
    }

    private void handleTimeCommand(String[] args) {
        if (args.length < 2) {
            log.info("用法: /xpa time <秒>");
            return;
        }
        try {
            int sec = Integer.parseInt(args[1]);
            if (sec <= 0) {
                log.warn("时间必须大于0秒！");
                return;
            }
            XinPga.INSTANCE.cmdTime(sec);
        } catch (NumberFormatException e) {
            log.warn("时间必须是整数秒！");
        }
    }

    private void handleModeCommand(String[] args) {
        if (args.length < 2) {
            log.info("用法: /xpa mode <PUBLIC|PRIVATE>");
            return;
        }
        XinPga.INSTANCE.cmdSendMode(args[1]);
    }

    private void handlePrivateIntervalCommand(String[] args) {
        if (args.length < 2) {
            log.info("用法: /xpa privateinterval <秒>");
            return;
        }
        try {
            int sec = Integer.parseInt(args[1]);
            XinPga.INSTANCE.cmdPrivateInterval(sec);
        } catch (NumberFormatException e) {
            log.warn("时间必须是整数秒！");
        }
    }

    private void handleMessageIntervalCommand(String[] args) {
        if (args.length < 2) {
            log.info("用法: /xpa messageinterval <秒>");
            return;
        }
        try {
            int sec = Integer.parseInt(args[1]);
            XinPga.INSTANCE.cmdMessageInterval(sec);
        } catch (NumberFormatException e) {
            log.warn("时间必须是整数秒！");
        }
    }

    private void handleAdminCommand(String[] args) {
        // 检查远程命令的admin功能是否启用
        if (!XinPga.INSTANCE.getConfig().isRemoteCommandAdminEnabled()) {
            log.warn("远程命令的admin功能已被禁用，请在配置文件中启用");
            return;
        }

        if (args.length < 2) {
            log.info("用法: /xpa admin add <玩家名> | remove <玩家名> | list");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "add" -> {
                if (args.length < 3) {
                    log.info("用法: /xpa admin add <玩家名>");
                    return;
                }
                XinPga.INSTANCE.cmdAddAdministrator(args[2]);
            }
            case "remove" -> {
                if (args.length < 3) {
                    log.info("用法: /xpa admin remove <玩家名>");
                    return;
                }
                XinPga.INSTANCE.cmdRemoveAdministrator(args[2]);
            }
            case "list" -> XinPga.INSTANCE.cmdListAdministrators();
            default -> log.warn("未知的管理员子命令！用法: /xpa admin add <玩家名> | remove <玩家名> | list");
        }
    }

    private void handleBlacklistCommand(String[] args) {
        if (args.length < 2) {
            log.info("用法: /xpa blacklist add <玩家名> | remove <玩家名> | list");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "add" -> {
                if (args.length < 3) {
                    log.info("用法: /xpa blacklist add <玩家名>");
                    return;
                }
                XinPga.INSTANCE.cmdAddToBlacklist(args[2]);
            }
            case "remove" -> {
                if (args.length < 3) {
                    log.info("用法: /xpa blacklist remove <玩家名>");
                    return;
                }
                XinPga.INSTANCE.cmdRemoveFromBlacklist(args[2]);
            }
            case "list" -> XinPga.INSTANCE.cmdListBlacklist();
            default -> log.warn("未知的黑名单子命令！用法: /xpa blacklist add <玩家名> | remove <玩家名> | list");
        }
    }

    private void showHelp() {
        log.info("=== XinPga 插件帮助 ===");
        log.info("/xpa start - 启动定时发送");
        log.info("/xpa stop - 停止定时发送");
        log.info("/xpa string <编号> <文本> - 设置发送内容");
        log.info("/xpa addmessage <消息> - 添加消息到发送列表");
        log.info("/xpa removemessage <消息> - 从发送列表移除消息");
        log.info("/xpa listmessages - 列出所有发送消息");
        log.info("/xpa time <秒> - 设置公告发送间隔");
        log.info("/xpa mode <PUBLIC|PRIVATE> - 设置发送模式");
        log.info("/xpa privateinterval <秒> - 设置私聊发送间隔");
        log.info("/xpa messageinterval <秒> - 设置消息间发送间隔");
        log.info("/xpa updateplayerlist - 手动更新在线玩家列表");
        log.info("/xpa blacklist add <玩家名> - 添加玩家到私聊黑名单");
        log.info("/xpa blacklist remove <玩家名> - 从私聊黑名单移除玩家");
        log.info("/xpa blacklist list - 列出私聊黑名单");
        log.info("/xpa admin add <玩家名> - 添加玩家到管理员列表");
        log.info("/xpa admin remove <玩家名> - 从管理员列表移除玩家");
        log.info("/xpa admin list - 列出管理员");
        log.info("/xpa reload - 重载配置文件");
        log.info("/xpa debug - 显示插件信息");
        log.info("/xpa help - 显示此帮助信息");
    }
}
