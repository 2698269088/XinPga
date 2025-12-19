package top.mcocet.xinpga.command;

import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.TabExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import top.mcocet.xinpga.XinPga;

public class XpaCommandExecutor extends TabExecutor {
    private static final Logger log = LoggerFactory.getLogger(XpaCommandExecutor.class);

    @Override
    public void onCommand(Command cmd, String label, String[] args) {
        if (args.length == 0) {
            log.info("用法: " + cmd.getUsage());
            return;
        }

        CommandHandler handler = new CommandHandler();
        handler.handleCommand(cmd, label, args);
    }

    @Override
    public List<String> onTabComplete(Command cmd, String label, String[] args) {
        TabCompleter completer = new TabCompleter();
        return completer.getCompletions(cmd, label, args);
    }

    public List<String> onCommandWithOutput(Command cmd, String label, String[] args) {
        List<String> output = new ArrayList<>();

        if (args.length == 0) {
            output.add("用法: " + cmd.getUsage());
            return output;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "start" -> {
                    XinPga.INSTANCE.cmdStart();
                    output.add("任务：已启动定时发送");
                }
                case "stop" -> {
                    XinPga.INSTANCE.cmdStop();
                    output.add("任务：已停止定时发送");
                }
                case "string" -> handleStringCommandWithOutput(args, output);
                case "addmessage" -> handleAddMessageCommandWithOutput(args, output);
                case "removemessage" -> handleRemoveMessageCommandWithOutput(args, output);
                case "listmessages" -> handleListMessagesCommandWithOutput(output);
                case "time" -> handleTimeCommandWithOutput(args, output);
                case "mode" -> handleModeCommandWithOutput(args, output);
                case "privateinterval" -> handlePrivateIntervalCommandWithOutput(args, output);
                case "messageinterval" -> handleMessageIntervalCommandWithOutput(args, output);
                case "reload" -> {
                    XinPga.INSTANCE.cmdReload();
                    output.add("信息：配置文件已重载");
                }
                case "updateplayerlist" -> {
                    XinPga.INSTANCE.cmdUpdatePlayerList();
                    output.add("信息：已手动更新在线玩家列表");
                }
                case "debug" -> handleDebugCommandWithOutput(output);
                case "blacklist" -> handleBlacklistCommandWithOutput(args, output);
                case "admin" -> handleAdminCommandWithOutput(args, output);
                case "help" -> output.addAll(showHelpOutput());
                case "forcestop" -> handleForceStopCommandWithOutput(args, output);
                case "randomsending" -> handleRandomSendingCommandWithOutput(args, output);
                case "greeting" -> handleGreetingCommandWithOutput(args, output);
                default -> output.add("未知子命令: " + args[0] + "！请使用 /xpa help 查看帮助");
            }
        } catch (Exception e) {
            output.add("错误：执行命令时发生异常: " + e.getMessage());
            log.error("执行命令时发生错误", e);
        }

        return output;
    }

    // 处理 greeting 命令
    private void handleGreetingCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add("用法: /xpa greeting <enable|disable|format> [格式]");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "enable" -> {
                XinPga.INSTANCE.cmdSetGreetingEnabled(true);
                output.add("信息：已启用问候语功能");
            }
            case "disable" -> {
                XinPga.INSTANCE.cmdSetGreetingEnabled(false);
                output.add("信息：已禁用问候语功能");
            }
            case "format" -> {
                if (args.length < 3) {
                    output.add("用法: /xpa greeting format <格式>");
                    output.add("提示：格式中可以使用 #name# 来表示玩家名");
                    return;
                }
                
                String format = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                XinPga.INSTANCE.cmdSetGreetingFormat(format);
                output.add("信息：已设置问候语格式为: " + format);
            }
            default -> output.add("错误：参数必须是 enable、disable 或 format");
        }
    }

    // 处理 string 命令
    private void handleStringCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 3) {
            output.add("用法: /xpa string <编号> <新文本>");
        } else {
            try {
                int index = Integer.parseInt(args[1]) - 1;
                String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                XinPga.INSTANCE.cmdString(index, text);
                output.add("信息：第 " + args[1] + " 条发送内容已改为: " + text);
            } catch (NumberFormatException e) {
                output.add("错误：编号必须是整数！");
            } catch (IndexOutOfBoundsException e) {
                output.add("错误：编号超出范围！");
            }
        }
    }

    private void handleForceStopCommandWithOutput(String[] args, List<String> output){
        XinPga.INSTANCE.cmdForceStop();
        output.add("信息：已强制停止所有任务");
    }

    // 处理 randomSending 命令
    private void handleRandomSendingCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add("用法: /xpa randomSending <on|off>");
            return;
        }
        
        switch (args[1].toLowerCase()) {
            case "on":
                XinPga.INSTANCE.cmdSetRandomSending(true);
                output.add("信息：已启用随机发送模式");
                break;
            case "off":
                XinPga.INSTANCE.cmdSetRandomSending(false);
                output.add("信息：已禁用随机发送模式");
                break;
            default:
                output.add("错误：参数必须是 on 或 off");
                break;
        }
    }

    // 处理 addmessage 命令
    private void handleAddMessageCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add("用法: /xpa addmessage <消息内容>");
        } else {
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            XinPga.INSTANCE.cmdAddMessage(message);
            output.add("信息：已添加消息: " + message);
        }
    }

    // 处理 removemessage 命令
    private void handleRemoveMessageCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add("用法: /xpa removemessage <消息内容>");
        } else {
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            boolean removed = XinPga.INSTANCE.getConfig().getMessages().remove(message);
            if (removed) {
                try {
                    XinPga.INSTANCE.getConfig().saveConfig();
                    output.add("信息：已移除消息: " + message);
                } catch (IOException e) {
                    output.add("错误：保存配置文件失败: " + e.getMessage());
                    log.error("保存配置文件时发生错误", e);
                }
            } else {
                output.add("错误：未找到消息: " + message);
            }
        }
    }


    // 处理 listmessages 命令
    private void handleListMessagesCommandWithOutput(List<String> output) {
        List<String> messages = XinPga.INSTANCE.getConfig().getMessages();
        if (messages.isEmpty()) {
            output.add("信息：消息列表为空");
        } else {
            output.add("信息：消息列表 (" + messages.size() + " 条):");
            for (int i = 0; i < messages.size(); i++) {
                output.add((i + 1) + ". " + messages.get(i));
            }
        }
    }

    // 处理 time 命令
    private void handleTimeCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add("用法: /xpa time <秒>");
        } else {
            try {
                int sec = Integer.parseInt(args[1]);
                if (sec <= 0) {
                    output.add("错误：时间必须大于0秒！");
                    return;
                }
                XinPga.INSTANCE.cmdTime(sec);
                output.add("信息：发送间隔已改为: " + sec + " 秒");
            } catch (NumberFormatException e) {
                output.add("错误：时间必须是整数秒！");
            }
        }
    }

    // 处理 mode 命令
    private void handleModeCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add("用法: /xpa mode <PUBLIC|PRIVATE>");
        } else {
            try {
                XinPga.SendMode mode = XinPga.SendMode.valueOf(args[1].toUpperCase());
                XinPga.INSTANCE.cmdSendMode(args[1]);
                output.add("信息：发送模式已改为: " + args[1]);
            } catch (IllegalArgumentException e) {
                output.add("错误：无效的发送模式: " + args[1] + "，有效值为: PUBLIC, PRIVATE");
            }
        }
    }

    // 处理 privateinterval 命令
    private void handlePrivateIntervalCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add("用法: /xpa privateinterval <秒>");
        } else {
            try {
                int sec = Integer.parseInt(args[1]);
                if (sec <= 0) {
                    output.add("错误：时间必须大于0秒！");
                    return;
                }
                XinPga.INSTANCE.cmdPrivateInterval(sec);
                output.add("信息：私聊发送间隔已改为: " + sec + " 秒");
            } catch (NumberFormatException e) {
                output.add("错误：时间必须是整数秒！");
            }
        }
    }

    // 处理 messageinterval 命令
    private void handleMessageIntervalCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add("用法: /xpa messageinterval <秒>");
        } else {
            try {
                int sec = Integer.parseInt(args[1]);
                if (sec <= 0) {
                    output.add("错误：时间必须大于0秒！");
                    return;
                }
                XinPga.INSTANCE.cmdMessageInterval(sec);
                output.add("信息：消息发送间隔已改为: " + sec + " 秒");
            } catch (NumberFormatException e) {
                output.add("错误：时间必须是整数秒！");
            }
        }
    }

    // 处理 debug 命令
    private void handleDebugCommandWithOutput(List<String> output) {
        XinPga.INSTANCE.cmdDebugPlayerList();
        output.add("信息：调试信息已输出到控制台，请查看服务器日志");
    }

    // 处理 blacklist 命令
    private void handleBlacklistCommandWithOutput(String[] args, List<String> output) {
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
                        output.add("信息：私聊黑名单 (" + blacklist.size() + " 个玩家):");
                        output.add(String.join(", ", blacklist));
                    }
                }
                default -> output.add("错误：未知的黑名单子命令！用法: /xpa blacklist add <玩家名> | remove <玩家名> | list");
            }
        }
    }

    // 处理 admin 命令
    private void handleAdminCommandWithOutput(String[] args, List<String> output) {
        // 检查远程命令的admin功能是否启用
        if (!XinPga.INSTANCE.getConfig().isRemoteCommandAdminEnabled()) {
            output.add("错误：远程命令的admin功能已被禁用，请在配置文件中启用");
            return;
        }

        if (args.length < 2) {
            output.add("用法: /xpa admin add <玩家名> | remove <玩家名> | list");
        } else {
            switch (args[1].toLowerCase()) {
                case "add" -> {
                    if (args.length < 3) {
                        output.add("用法: /xpa admin add <玩家名>");
                    } else {
                        XinPga.INSTANCE.cmdAddAdministrator(args[2]);
                        output.add("信息：已将玩家 " + args[2] + " 添加到管理员列表");
                    }
                }
                case "remove" -> {
                    if (args.length < 3) {
                        output.add("用法: /xpa admin remove <玩家名>");
                    } else {
                        XinPga.INSTANCE.cmdRemoveAdministrator(args[2]);
                        output.add("信息：已将玩家 " + args[2] + " 从管理员列表中移除");
                    }
                }
                case "list" -> {
                    List<String> admins = XinPga.INSTANCE.getConfig().getAdministrators();
                    if (admins.isEmpty()) {
                        output.add("信息：管理员列表为空");
                    } else {
                        output.add("信息：管理员列表 (" + admins.size() + " 个管理员):");
                        output.add(String.join(", ", admins));
                    }
                }
                default -> output.add("错误：未知的管理员子命令！用法: /xpa admin add <玩家名> | remove <玩家名> | list");
            }
        }
    }

    // 显示帮助信息
    private List<String> showHelpOutput() {
        List<String> output = new ArrayList<>();
        output.add("=== XinPga 插件远程命令帮助 ===");
        output.add("#command xpa start - 启动定时发送");
        output.add("#command xpa stop - 停止定时发送");
        output.add("#command xpa string <编号> <文本> - 设置发送内容");
        output.add("#command xpa addmessage <消息> - 添加消息到发送列表");
        output.add("#command xpa removemessage <消息> - 从发送列表移除消息");
        output.add("#command xpa listmessages - 列出所有发送消息");
        output.add("#command xpa time <秒> - 设置公告发送间隔");
        output.add("#command xpa mode <PUBLIC|PRIVATE> - 设置发送模式");
        output.add("#command xpa privateinterval <秒> - 设置私聊发送间隔");
        output.add("#command xpa messageinterval <秒> - 设置消息间发送间隔");
        output.add("#command xpa randomSending <on|off> - 设置随机发送模式");
        output.add("#command xpa greeting <enable|disable> - 控制问候语开关");
        output.add("#command xpa greeting format [格式] - 修改问候语格式，以#name#做玩家占位符");
        output.add("#command xpa updateplayerlist - 手动更新在线玩家列表");
        output.add("#command xpa blacklist add <玩家名> - 添加玩家到私聊黑名单");
        output.add("#command xpa blacklist remove <玩家名> - 从私聊黑名单移除玩家");
        output.add("#command xpa blacklist list - 列出私聊黑名单");
        output.add("#command xpa admin add <玩家名> - 添加玩家到管理员列表");
        output.add("#command xpa admin remove <玩家名> - 从管理员列表移除玩家");
        output.add("#command xpa admin list - 列出管理员");
        output.add("#command xpa reload - 重载配置文件");
        output.add("#command xpa debug - 显示调试信息");
        output.add("#command xpa help - 显示此帮助信息");
        output.add("==============================");
        return output;
    }
}