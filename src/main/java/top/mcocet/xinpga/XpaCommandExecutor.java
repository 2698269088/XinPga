package top.mcocet.xinpga;

import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.TabExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import top.mcocet.xinpga.PrivateMessageCommand;

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
                    if (sec <= 0) {
                        log.warn("时间必须大于0秒！");
                        return;
                    }
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
    // 该方法已迁移到PrivateMessageCommand中
    public List<String> onCommandWithOutput(Command cmd, String label, String[] args) {
        List<String> output = new ArrayList<>();

        // 暂停私聊宣传任务
        XinPga.INSTANCE.isSuspended = true; // 标记任务暂停
        XinPga.INSTANCE.stopScheduler();    // 停止调度器
        output.add("信息：任务已暂停，开始执行远程命令");

        // 执行远程命令的逻辑
        try {
            // 添加对私聊消息的捕获逻辑
            PrivateMessageCommand pmc = new PrivateMessageCommand();
            output.addAll(pmc.pmcWithOutput(cmd, label, args));
        } catch (Exception e) {
            output.add("错误：命令执行失败: " + e.getMessage());
        } finally {
            // 恢复私聊宣传任务
            if (XinPga.INSTANCE.getConfig().isEnabled()) { // 如果任务之前是启用的
                XinPga.INSTANCE.startScheduler();       // 重新启动调度器
                XinPga.INSTANCE.isSuspended = false;    // 清除暂停标志
                output.add("信息：任务恢复，远程命令执行完成");
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
