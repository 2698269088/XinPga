package top.mcocet.xinpga;

import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.TabExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class XpaCommandExecutor extends TabExecutor {

    private static final Logger log = LoggerFactory.getLogger(XpaCommandExecutor.class);

    @Override
    public void onCommand(Command cmd, String label, String[] args) {
        if (args.length == 0) {
            log.info("用法: " + cmd.getUsage());
            return;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> XinPga.INSTANCE.cmdStart();
            case "stop" -> XinPga.INSTANCE.cmdStop();
            case "string" -> {
                if (args.length < 2) {
                    log.info("用法: /xpa string <新文本>");
                    return;
                }
                XinPga.INSTANCE.cmdString(String.join(" ", args).substring(args[0].length() + 1));
            }
            case "addmessage" -> {
                if (args.length < 2) {
                    log.info("用法: /xpa addmessage <消息内容>");
                    return;
                }
                XinPga.INSTANCE.cmdAddMessage(String.join(" ", args).substring(args[0].length() + 1));
            }
            case "removemessage" -> {
                if (args.length < 2) {
                    log.info("用法: /xpa removemessage <消息内容>");
                    return;
                }
                XinPga.INSTANCE.cmdRemoveMessage(String.join(" ", args).substring(args[0].length() + 1));
            }
            case "listmessages" -> XinPga.INSTANCE.cmdListMessages();
            case "time" -> {
                if (args.length < 2) {
                    log.info("用法: /xpa time <秒>");
                    return;
                }
                try {
                    int sec = Integer.parseInt(args[1]);
                    XinPga.INSTANCE.cmdTime(sec);
                } catch (NumberFormatException e) {
                    log.warn("时间必须是整数秒！");
                }
            }
            case "mode" -> {
                if (args.length < 2) {
                    log.info("用法: /xpa mode <PUBLIC|PRIVATE>");
                    return;
                }
                XinPga.INSTANCE.cmdSendMode(args[1]);
            }
            case "privateinterval" -> {
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
            case "messageinterval" -> {
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
            case "reload" -> XinPga.INSTANCE.cmdReload();
            case "help" -> showHelp();
            case "blacklist" -> {
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
            default -> log.warn("未知子命令！用法: " + cmd.getUsage());
        }
    }

    private void showHelp() {
        log.info("=== XinPga 插件帮助 ===");
        log.info("/xpa start - 启动定时发送");
        log.info("/xpa stop - 停止定时发送");
        log.info("/xpa string <文本> - 设置发送内容（兼容旧命令）");
        log.info("/xpa addmessage <消息> - 添加消息到发送列表");
        log.info("/xpa removemessage <消息> - 从发送列表移除消息");
        log.info("/xpa listmessages - 列出所有发送消息");
        log.info("/xpa time <秒> - 设置公告发送间隔");
        log.info("/xpa mode <PUBLIC|PRIVATE> - 设置发送模式");
        log.info("/xpa privateinterval <秒> - 设置私聊发送间隔");
        log.info("/xpa messageinterval <秒> - 设置消息间发送间隔");
        log.info("/xpa blacklist add <玩家名> - 添加玩家到私聊黑名单");
        log.info("/xpa blacklist remove <玩家名> - 从私聊黑名单移除玩家");
        log.info("/xpa blacklist list - 列出私聊黑名单");
        log.info("/xpa reload - 重载配置文件");
        log.info("/xpa help - 显示此帮助信息");
    }

    @Override
    public List<String> onTabComplete(Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return List.of("start", "stop", "string", "addmessage", "removemessage", "listmessages", "time", "reload", "move", "mode", "privateinterval", "messageinterval", "help", "blacklist");
        } else if (args.length == 2 && args[0].equals("move")) {
            return List.of("x", "z");
        } else if (args.length == 2 && args[0].equals("mode")) {
            return List.of("PUBLIC", "PRIVATE");
        } else if (args.length == 2 && args[0].equals("blacklist")) {
            return List.of("add", "remove", "list");
        }
        return List.of();
    }
}
