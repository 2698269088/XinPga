package top.mcocet.xinpga;

import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.TabExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
                if (args.length < 3) {
                    log.info("用法: /xpa string <编号> <新文本>");
                    return;
                }
                try {
                    int index = Integer.parseInt(args[1]) - 1; // 编号从1开始，数组索引从0开始
                    String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    XinPga.INSTANCE.cmdString(index, text);
                } catch (NumberFormatException e) {
                    log.warn("编号必须是整数！");
                }
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
            case "updateplayerlist" -> XinPga.INSTANCE.cmdUpdatePlayerList();
            case "debug" -> XinPga.INSTANCE.cmdDebugPlayerList();
            case "admin" -> {
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
            default -> log.warn("未知子命令: " + args[0] + "！用法: " + cmd.getUsage());
        }
    }

    // 添加一个可以捕获输出的命令执行方法
    // 仅应用在私聊输出的方法
    public List<String> onCommandWithOutput(Command cmd, String label, String[] args) {
        List<String> output = new ArrayList<>();

        if (args.length == 0) {
            output.add("用法: " + cmd.getUsage());
            return output;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                XinPga.INSTANCE.cmdStart();
                output.add("任务：已启动定时发送");
            }
            case "stop" -> {
                XinPga.INSTANCE.cmdStop();
                output.add("任务：已停止定时发送");
            }
            case "string" -> {
                if (args.length < 3) {
                    output.add("用法: /xpa string <编号> <新文本>");
                } else {
                    try {
                        int index = Integer.parseInt(args[1]) - 1; // 编号从1开始，数组索引从0开始
                        String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                        XinPga.INSTANCE.cmdString(index, text);
                        output.add("信息：第 " + args[1] + " 条发送内容已改为: " + text);
                    } catch (NumberFormatException e) {
                        output.add("编号必须是整数！");
                    }
                }
            }
            case "addmessage" -> {
                if (args.length < 2) {
                    output.add("用法: /xpa addmessage <消息内容>");
                } else {
                    String message = String.join(" ", args).substring(args[0].length() + 1);
                    XinPga.INSTANCE.cmdAddMessage(message);
                    output.add("信息：已添加消息: " + message);
                }
            }
            case "removemessage" -> {
                if (args.length < 2) {
                    output.add("用法: /xpa removemessage <消息内容>");
                } else {
                    String message = String.join(" ", args).substring(args[0].length() + 1);
                    XinPga.INSTANCE.cmdRemoveMessage(message);
                    output.add("信息：已移除消息: " + message);
                }
            }
            case "listmessages" -> {
                List<String> messages = XinPga.INSTANCE.getConfig().getMessages();
                if (messages.isEmpty()) {
                    output.add("信息：消息列表为空");
                } else {
                    output.add("信息：消息列表:");
                    for (int i = 0; i < messages.size(); i++) {
                        output.add((i + 1) + ". " + messages.get(i));
                    }
                }
            }
            case "time" -> {
                if (args.length < 2) {
                    output.add("用法: /xpa time <秒>");
                } else {
                    try {
                        int sec = Integer.parseInt(args[1]);
                        XinPga.INSTANCE.cmdTime(sec);
                        output.add("信息：发送间隔已改为: " + sec + " 秒");
                    } catch (NumberFormatException e) {
                        output.add("时间必须是整数秒！");
                    }
                }
            }
            case "mode" -> {
                if (args.length < 2) {
                    output.add("用法: /xpa mode <PUBLIC|PRIVATE>");
                } else {
                    try {
                        XinPga.SendMode mode = XinPga.SendMode.valueOf(args[1].toUpperCase());
                        XinPga.INSTANCE.cmdSendMode(args[1]);
                        output.add("信息：发送模式已改为: " + args[1]);
                    } catch (IllegalArgumentException e) {
                        output.add("无效的发送模式: " + args[1] + "，有效值为: PUBLIC, PRIVATE");
                    }
                }
            }
            case "privateinterval" -> {
                if (args.length < 2) {
                    output.add("用法: /xpa privateinterval <秒>");
                } else {
                    try {
                        int sec = Integer.parseInt(args[1]);
                        XinPga.INSTANCE.cmdPrivateInterval(sec);
                        output.add("信息：私聊发送间隔已改为: " + sec + " 秒");
                    } catch (NumberFormatException e) {
                        output.add("时间必须是整数秒！");
                    }
                }
            }
            case "messageinterval" -> {
                if (args.length < 2) {
                    output.add("用法: /xpa messageinterval <秒>");
                } else {
                    try {
                        int sec = Integer.parseInt(args[1]);
                        XinPga.INSTANCE.cmdMessageInterval(sec);
                        output.add("信息：消息发送间隔已改为: " + sec + " 秒");
                    } catch (NumberFormatException e) {
                        output.add("时间必须是整数秒！");
                    }
                }
            }
            case "reload" -> {
                XinPga.INSTANCE.cmdReload();
                output.add("信息：配置文件已重载");
            }
            case "help" -> output.add("无法在远程命令下使用该功能！");
            case "updateplayerlist" -> {
                XinPga.INSTANCE.cmdUpdatePlayerList();
                output.add("信息：已手动更新在线玩家列表");
            }
            case "debug" -> {
                XinPga.INSTANCE.cmdDebugPlayerList();
                output.add("信息：已显示插件调试信息");
            }
            case "blacklist" -> {
                if (args.length < 2) {
                    output.add("用法: /xpa blacklist add <玩家名> | remove <玩家名> | list");
                } else {
                    switch (args[1].toLowerCase()) {
                        case "add" -> {
                            if (args.length < 3) {
                                output.add("用法: /xpa blacklist add <玩家名>");
                            } else {
                                XinPga.INSTANCE.cmdAddToBlacklist(args[2]);
                                output.add("信息：已将玩家 " + args[2] + " 添加到私聊黑名单");
                            }
                        }
                        case "remove" -> {
                            if (args.length < 3) {
                                output.add("用法: /xpa blacklist remove <玩家名>");
                            } else {
                                XinPga.INSTANCE.cmdRemoveFromBlacklist(args[2]);
                                output.add("信息：已将玩家 " + args[2] + " 从私聊黑名单中移除");
                            }
                        }
                        case "list" -> {
                            List<String> blacklist = XinPga.INSTANCE.getConfig().getPrivateMessageBlacklist();
                            if (blacklist.isEmpty()) {
                                output.add("信息：私聊黑名单为空");
                            } else {
                                output.add("信息：私聊黑名单玩家列表: " + String.join(", ", blacklist));
                            }
                        }
                        default -> output.add("未知的黑名单子命令！用法: /xpa blacklist add <玩家名> | remove <玩家名> | list");
                    }
                }
            }
            case "admin" -> {
                output.add("无法在远程命令下使用该功能！");
            }
            default -> {
                output.add("未知子命令: " + args[0]);
            }
        }
        return output;
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

    private List<String> showHelpOutput() {
        List<String> output = new ArrayList<>();
        output.add("=== XinPga 插件帮助 ===");
        output.add("/xpa start - 启动定时发送");
        output.add("/xpa stop - 停止定时发送");
        output.add("/xpa string <编号> <文本> - 设置发送内容");
        output.add("/xpa addmessage <消息> - 添加消息到发送列表");
        output.add("/xpa removemessage <消息> - 从发送列表移除消息");
        output.add("/xpa listmessages - 列出所有发送消息");
        output.add("/xpa time <秒> - 设置公告发送间隔");
        output.add("/xpa mode <PUBLIC|PRIVATE> - 设置发送模式");
        output.add("/xpa privateinterval <秒> - 设置私聊发送间隔");
        output.add("/xpa messageinterval <秒> - 设置消息间发送间隔");
        output.add("/xpa updateplayerlist - 手动更新在线玩家列表");
        output.add("/xpa blacklist add <玩家名> - 添加玩家到私聊黑名单");
        output.add("/xpa blacklist remove <玩家名> - 从私聊黑名单移除玩家");
        output.add("/xpa blacklist list - 列出私聊黑名单");
        output.add("/xpa admin add <玩家名> - 添加玩家到管理员列表");
        output.add("/xpa admin remove <玩家名> - 从管理员列表移除玩家");
        output.add("/xpa admin list - 列出管理员");
        output.add("/xpa reload - 重载配置文件");
        output.add("/xpa debug - 显示插件信息");
        output.add("/xpa help - 显示此帮助信息");
        return output;
    }

    @Override
    public List<String> onTabComplete(Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return List.of("start", "stop", "string", "addmessage", "removemessage", "listmessages", "time", "reload", "move", "mode", "privateinterval", "messageinterval", "help", "blacklist", "admin", "updateplayerlist");
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
