package top.mcocet.xinpga;

import xin.bbtt.mcbot.command.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrivateMessageCommand {
    public List<String> pmcWithOutput(Command cmd, String label, String[] args) {
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
                output.add("无法在远程命令下使用该功能！");
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
                output.add("未知子命令: " + args[0]+"请查看GitHub中的命令帮助或在控制台执行help命令");
            }
        }
        return output;
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
}
