package top.mcocet.xinpga.command;

import xin.bbtt.mcbot.LangManager;
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
    private static final Logger log = LoggerFactory.getLogger("XpaCommandExecutor");

    @Override
    public void onCommand(Command cmd, String label, String[] args) {
        if (args.length == 0) {
            log.info(LangManager.get("xinpga.command.usage", cmd.usage()));
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
            output.add(LangManager.get("xinpga.command.usage", cmd.usage()));
            return output;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "start" -> {
                    XinPga.INSTANCE.cmdStart();
                    output.add(LangManager.get("xinpga.command.start.success"));
                }
                case "stop" -> {
                    XinPga.INSTANCE.cmdStop();
                    output.add(LangManager.get("xinpga.command.stop.success"));
                }
                case "multistart" -> {
                    XinPga.INSTANCE.cmdStartMultiThread();
                    output.add(LangManager.get("xinpga.multithread.start.success"));
                }
                case "multistop" -> {
                    XinPga.INSTANCE.cmdStopMultiThread();
                    output.add(LangManager.get("xinpga.multithread.stop.success"));
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
                    output.add(LangManager.get("xinpga.command.reload.success"));
                }
                case "updateplayerlist" -> {
                    XinPga.INSTANCE.cmdUpdatePlayerList();
                    output.add(LangManager.get("xinpga.command.playerlist.update.success"));
                }
                case "debug" -> handleDebugCommandWithOutput(output);
                case "blacklist" -> handleBlacklistCommandWithOutput(args, output);
                case "admin" -> handleAdminCommandWithOutput(args, output);
                case "help" -> output.addAll(showHelpOutput());
                case "forcestop" -> handleForceStopCommandWithOutput(args, output);
                case "randomsending" -> handleRandomSendingCommandWithOutput(args, output);
                case "greeting" -> handleGreetingCommandWithOutput(args, output);
                case "numberreplacement" -> handleNumberReplacementCommandWithOutput(args, output);
                case "mainnumberreplacement" -> handleMainNumberReplacementCommandWithOutput(args, output);
                case "minconsecutive" -> handleMinConsecutiveCommandWithOutput(args, output);
                case "multistring" -> handleMultiThreadStringCommandWithOutput(args, output);
                case "multiaddmessage" -> handleMultiThreadAddMessageCommandWithOutput(args, output);
                case "multiremovemessage" -> handleMultiThreadRemoveMessageCommandWithOutput(args, output);
                case "multilistmessages" -> handleMultiThreadListMessagesCommandWithOutput(output);
                case "syncmultithread" -> handleSyncMultiThreadCommandWithOutput(args, output);
                case "multithreadinterval" -> handleMultiThreadIntervalCommandWithOutput(args, output);
                default -> output.add(LangManager.get("xinpga.command.unknown", args[0]));
            }
        } catch (Exception e) {
            output.add(LangManager.get("xinpga.command.error.generic", e.getMessage()));
            log.error(LangManager.get("xinpga.command.error.execution"), e);
        }

        return output;
    }

    // 处理 numberReplacement 命令
    private void handleNumberReplacementCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa numberreplacement <on|off>"));
            return;
        }
        
        switch (args[1].toLowerCase()) {
            case "on":
                XinPga.INSTANCE.cmdSetNumberReplacementEnabled(true);
                break;
            case "off":
                XinPga.INSTANCE.cmdSetNumberReplacementEnabled(false);
                break;
            default:
                output.add(LangManager.get("xinpga.command.mode.invalid", "numberreplacement", "on, off"));
                break;
        }
    }
    
    // 处理 mainNumberReplacement 命令
    private void handleMainNumberReplacementCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa mainnumberreplacement <on|off>"));
            return;
        }
        
        switch (args[1].toLowerCase()) {
            case "on":
                XinPga.INSTANCE.cmdSetMainNumberReplacementEnabled(true);
                break;
            case "off":
                XinPga.INSTANCE.cmdSetMainNumberReplacementEnabled(false);
                break;
            default:
                output.add(LangManager.get("xinpga.command.mode.invalid", "mainnumberreplacement", "on, off"));
                break;
        }
    }

    // 处理 minConsecutive 命令
    private void handleMinConsecutiveCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa minconsecutive <数字>"));
            return;
        }
        
        try {
            int minConsecutive = Integer.parseInt(args[1]);
            if (minConsecutive <= 0) {
                output.add(LangManager.get("xinpga.number.replace.min.error"));
                return;
            }
            XinPga.INSTANCE.cmdSetMinConsecutiveNumbers(minConsecutive);
        } catch (NumberFormatException e) {
            output.add(LangManager.get("xinpga.command.mode.invalid", "minconsecutive", LangManager.get("xinpga.command.usage", "整数")));
        }
    }
    
    // 处理 multistring 命令
    private void handleMultiThreadStringCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 3) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa multistring <编号> <新文本>"));
        } else {
            try {
                int index = Integer.parseInt(args[1]) - 1;
                String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                XinPga.INSTANCE.cmdMultiThreadString(index, text);
                output.add(LangManager.get("xinpga.command.multithread.string.updated", args[1], text));
            } catch (NumberFormatException e) {
                output.add(LangManager.get("xinpga.command.mode.invalid", "multistring", LangManager.get("xinpga.command.usage", "整数")));
            } catch (IndexOutOfBoundsException e) {
                output.add(LangManager.get("xinpga.command.string.index.error", XinPga.INSTANCE.getConfig().getMultiThreadMessages().size()));
            }
        }
    }

    // 处理 multiaddmessage 命令
    private void handleMultiThreadAddMessageCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa multiaddmessage <消息内容>"));
        } else {
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            XinPga.INSTANCE.cmdMultiThreadAddMessage(message);
            output.add(LangManager.get("xinpga.command.multithread.message.added", message));
        }
    }

    // 处理 multiremovemessage 命令
    private void handleMultiThreadRemoveMessageCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa multiremovemessage <消息内容>"));
        } else {
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            boolean removed = XinPga.INSTANCE.getConfig().getMultiThreadMessages().remove(message);
            if (removed) {
                try {
                    XinPga.INSTANCE.getConfig().saveConfig();
                    output.add(LangManager.get("xinpga.command.multithread.message.removed", message));
                } catch (IOException e) {
                    output.add(LangManager.get("xinpga.config.save.error", e.getMessage()));
                    log.error(LangManager.get("xinpga.config.save.error", e.getMessage()), e);
                }
            } else {
                output.add(LangManager.get("xinpga.command.multithread.message.not.found", message));
            }
        }
    }

    // 处理 multilistmessages 命令
    private void handleMultiThreadListMessagesCommandWithOutput(List<String> output) {
        List<String> messages = XinPga.INSTANCE.getConfig().getMultiThreadMessages();
        if (messages.isEmpty()) {
            output.add(LangManager.get("xinpga.command.multithread.list.empty"));
        } else {
            output.add(LangManager.get("xinpga.command.multithread.list.header"));
            for (int i = 0; i < messages.size(); i++) {
                output.add((i + 1) + ". " + messages.get(i));
            }
        }
    }
    
    // 处理 syncmultithread 命令
    private void handleSyncMultiThreadCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa syncmultithread <on|off>"));
            return;
        }
        
        switch (args[1].toLowerCase()) {
            case "on":
                XinPga.INSTANCE.cmdSetSyncMultiThreadMessages(true);
                break;
            case "off":
                XinPga.INSTANCE.cmdSetSyncMultiThreadMessages(false);
                break;
            default:
                output.add(LangManager.get("xinpga.command.mode.invalid", "syncmultithread", "on, off"));
                break;
        }
    }

    // 处理 greeting 命令
    private void handleGreetingCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa greeting <enable|disable|format> [格式]"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "enable" -> {
                XinPga.INSTANCE.cmdSetGreetingEnabled(true);
            }
            case "disable" -> {
                XinPga.INSTANCE.cmdSetGreetingEnabled(false);
            }
            case "format" -> {
                if (args.length < 3) {
                    output.add(LangManager.get("xinpga.command.usage", "/xpa greeting format <格式>"));
                    output.add(LangManager.get("xinpga.command.greeting.format.hint"));
                    return;
                }
                
                String format = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                XinPga.INSTANCE.cmdSetGreetingFormat(format);
            }
            default -> output.add(LangManager.get("xinpga.command.mode.invalid", "greeting", "enable, disable, format"));
        }
    }

    // 处理 string 命令
    private void handleStringCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 3) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa string <编号> <新文本>"));
        } else {
            try {
                int index = Integer.parseInt(args[1]) - 1;
                String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                XinPga.INSTANCE.cmdString(index, text);
                output.add(LangManager.get("xinpga.command.string.updated", args[1], text));
            } catch (NumberFormatException e) {
                output.add(LangManager.get("xinpga.command.mode.invalid", "string", LangManager.get("xinpga.command.usage", "整数")));
            } catch (IndexOutOfBoundsException e) {
                output.add(LangManager.get("xinpga.command.string.index.error", XinPga.INSTANCE.getConfig().getMessages().size()));
            }
        }
    }

    private void handleForceStopCommandWithOutput(String[] args, List<String> output){
        XinPga.INSTANCE.cmdForceStop();
    }

    // 处理 randomSending 命令
    private void handleRandomSendingCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa randomSending <on|off>"));
            return;
        }
        
        switch (args[1].toLowerCase()) {
            case "on":
                XinPga.INSTANCE.cmdSetRandomSending(true);
                break;
            case "off":
                XinPga.INSTANCE.cmdSetRandomSending(false);
                break;
            default:
                output.add(LangManager.get("xinpga.command.mode.invalid", "randomSending", "on, off"));
                break;
        }
    }

    // 处理 addmessage 命令
    private void handleAddMessageCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa addmessage <消息内容>"));
        } else {
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            XinPga.INSTANCE.cmdAddMessage(message);
            output.add(LangManager.get("xinpga.command.message.added", message));
        }
    }

    // 处理 removemessage 命令
    private void handleRemoveMessageCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa removemessage <消息内容>"));
        } else {
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            boolean removed = XinPga.INSTANCE.getConfig().getMessages().remove(message);
            if (removed) {
                try {
                    XinPga.INSTANCE.getConfig().saveConfig();
                    output.add(LangManager.get("xinpga.command.message.removed", message));
                } catch (IOException e) {
                    output.add(LangManager.get("xinpga.config.save.error", e.getMessage()));
                    log.error(LangManager.get("xinpga.config.save.error", e.getMessage()), e);
                }
            } else {
                output.add(LangManager.get("xinpga.command.multithread.message.not.found", message));
            }
        }
    }


    // 处理 listmessages 命令
    private void handleListMessagesCommandWithOutput(List<String> output) {
        List<String> messages = XinPga.INSTANCE.getConfig().getMessages();
        if (messages.isEmpty()) {
            output.add(LangManager.get("xinpga.command.list.empty"));
        } else {
            output.add(LangManager.get("xinpga.command.list.header"));
            for (int i = 0; i < messages.size(); i++) {
                output.add((i + 1) + ". " + messages.get(i));
            }
        }
        
        // 显示多线程消息列表
        List<String> multiThreadMessages = XinPga.INSTANCE.getConfig().getMultiThreadMessages();
        if (multiThreadMessages.isEmpty()) {
            output.add(LangManager.get("xinpga.command.multithread.list.empty"));
        } else {
            output.add(LangManager.get("xinpga.command.multithread.list.header"));
            for (int i = 0; i < multiThreadMessages.size(); i++) {
                output.add((i + 1) + ". " + multiThreadMessages.get(i));
            }
        }
    }

    // 处理 time 命令
    private void handleTimeCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa time <秒>"));
        } else {
            try {
                int sec = Integer.parseInt(args[1]);
                if (sec <= 0) {
                    output.add(LangManager.get("xinpga.command.mode.invalid", "time", LangManager.get("xinpga.command.usage", "大于0的整数")));
                    return;
                }
                XinPga.INSTANCE.cmdTime(sec);
                output.add(LangManager.get("xinpga.command.time.updated", sec));
            } catch (NumberFormatException e) {
                output.add(LangManager.get("xinpga.command.mode.invalid", "time", LangManager.get("xinpga.command.usage", "整数")));
            }
        }
    }

    // 处理 mode 命令
    private void handleModeCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa mode <PUBLIC|PRIVATE>"));
        } else {
            try {
                XinPga.SendMode mode = XinPga.SendMode.valueOf(args[1].toUpperCase());
                XinPga.INSTANCE.cmdSendMode(args[1]);
                output.add(LangManager.get("xinpga.command.mode.updated", args[1]));
            } catch (IllegalArgumentException e) {
                output.add(LangManager.get("xinpga.command.mode.invalid", args[1], "PUBLIC, PRIVATE"));
            }
        }
    }

    // 处理 privateinterval 命令
    private void handlePrivateIntervalCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa privateinterval <秒>"));
        } else {
            try {
                int sec = Integer.parseInt(args[1]);
                if (sec <= 0) {
                    output.add(LangManager.get("xinpga.command.mode.invalid", "privateinterval", LangManager.get("xinpga.command.usage", "大于0的整数")));
                    return;
                }
                XinPga.INSTANCE.cmdPrivateInterval(sec);
                output.add(LangManager.get("xinpga.command.private.interval.updated", sec));
            } catch (NumberFormatException e) {
                output.add(LangManager.get("xinpga.command.mode.invalid", "privateinterval", LangManager.get("xinpga.command.usage", "整数")));
            }
        }
    }

    // 处理 messageinterval 命令
    private void handleMessageIntervalCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa messageinterval <秒>"));
        } else {
            try {
                int sec = Integer.parseInt(args[1]);
                if (sec <= 0) {
                    output.add(LangManager.get("xinpga.command.mode.invalid", "messageinterval", LangManager.get("xinpga.command.usage", "大于0的整数")));
                    return;
                }
                XinPga.INSTANCE.cmdMessageInterval(sec);
                output.add(LangManager.get("xinpga.command.message.interval.updated", sec));
            } catch (NumberFormatException e) {
                output.add(LangManager.get("xinpga.command.mode.invalid", "messageinterval", LangManager.get("xinpga.command.usage", "整数")));
            }
        }
    }
    
    // 处理 multithreadinterval 命令
    private void handleMultiThreadIntervalCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa multithreadinterval <秒>"));
        } else {
            try {
                int sec = Integer.parseInt(args[1]);
                if (sec <= 0) {
                    output.add(LangManager.get("xinpga.command.mode.invalid", "multithreadinterval", LangManager.get("xinpga.command.usage", "大于0的整数")));
                    return;
                }
                XinPga.INSTANCE.cmdMultiThreadInterval(sec);
                output.add(LangManager.get("xinpga.multithread.interval.set.success", sec));
            } catch (NumberFormatException e) {
                output.add(LangManager.get("xinpga.command.mode.invalid", "multithreadinterval", LangManager.get("xinpga.command.usage", "整数")));
            }
        }
    }

    // 处理 debug 命令
    private void handleDebugCommandWithOutput(List<String> output) {
        XinPga.INSTANCE.cmdDebugPlayerList();
        output.add(LangManager.get("xinpga.command.debug.output"));
    }

    // 处理 blacklist 命令
    private void handleBlacklistCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa blacklist add <玩家名> | remove <玩家名> | list"));
        } else {
            switch (args[1].toLowerCase()) {
                case "add" -> {
                    if (args.length < 3) {
                        output.add(LangManager.get("xinpga.command.usage", "/xpa blacklist add <玩家名>"));
                    } else {
                        XinPga.INSTANCE.cmdAddToBlacklist(args[2]);
                        output.add(LangManager.get("xinpga.command.blacklist.added", args[2]));
                    }
                }
                case "remove" -> {
                    if (args.length < 3) {
                        output.add(LangManager.get("xinpga.command.usage", "/xpa blacklist remove <玩家名>"));
                    } else {
                        XinPga.INSTANCE.cmdRemoveFromBlacklist(args[2]);
                        output.add(LangManager.get("xinpga.command.blacklist.removed", args[2]));
                    }
                }
                case "list" -> {
                    List<String> blacklist = XinPga.INSTANCE.getConfig().getPrivateMessageBlacklist();
                    if (blacklist.isEmpty()) {
                        output.add(LangManager.get("xinpga.command.blacklist.empty"));
                    } else {
                        output.add(LangManager.get("xinpga.command.blacklist.header", String.join(", ", blacklist)));
                    }
                }
                default -> output.add(LangManager.get("xinpga.command.unknown", "blacklist"));
            }
        }
    }

    // 处理 admin 命令
    private void handleAdminCommandWithOutput(String[] args, List<String> output) {
        // 检查远程命令的admin功能是否启用
        if (!XinPga.INSTANCE.getConfig().isRemoteCommandAdminEnabled()) {
            output.add(LangManager.get("xinpga.remote.admin.disabled"));
            return;
        }

        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa admin add <玩家名> | remove <玩家名> | list"));
        } else {
            switch (args[1].toLowerCase()) {
                case "add" -> {
                    if (args.length < 3) {
                        output.add(LangManager.get("xinpga.command.usage", "/xpa admin add <玩家名>"));
                    } else {
                        XinPga.INSTANCE.cmdAddAdministrator(args[2]);
                        output.add(LangManager.get("xinpga.command.admin.added", args[2]));
                    }
                }
                case "remove" -> {
                    if (args.length < 3) {
                        output.add(LangManager.get("xinpga.command.usage", "/xpa admin remove <玩家名>"));
                    } else {
                        XinPga.INSTANCE.cmdRemoveAdministrator(args[2]);
                        output.add(LangManager.get("xinpga.command.admin.removed", args[2]));
                    }
                }
                case "list" -> {
                    List<String> admins = XinPga.INSTANCE.getConfig().getAdministrators();
                    if (admins.isEmpty()) {
                        output.add(LangManager.get("xinpga.command.admin.empty"));
                    } else {
                        output.add(LangManager.get("xinpga.command.admin.header", String.join(", ", admins)));
                    }
                }
                default -> output.add(LangManager.get("xinpga.command.unknown", "admin"));
            }
        }
    }

    // 显示帮助信息
    private List<String> showHelpOutput() {
        List<String> output = new ArrayList<>();
        output.add(LangManager.get("xinpga.remote.help.title.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.start.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.stop.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.multistart.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.multistop.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.string.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.addmessage.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.removemessage.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.listmessages.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.time.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.mode.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.privateinterval.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.messageinterval.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.randomsending.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.numberreplacement.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.minconsecutive.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.multistring.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.multiaddmessage.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.multiremovemessage.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.multilistmessages.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.syncmultithread.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.greeting.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.greeting.format.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.updateplayerlist.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.blacklist.add.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.blacklist.remove.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.blacklist.list.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.admin.add.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.admin.remove.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.admin.list.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.reload.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.debug.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.help.with.prefix"));
        output.add(LangManager.get("xinpga.remote.help.separator.with.prefix"));
        return output;
    }
}