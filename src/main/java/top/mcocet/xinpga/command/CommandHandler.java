package top.mcocet.xinpga.command;

import xin.bbtt.mcbot.LangManager;
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
            case "multistart" -> XinPga.INSTANCE.cmdStartMultiThread();
            case "multistop" -> XinPga.INSTANCE.cmdStopMultiThread();
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
            case "forcestop" -> forceStop(args);
            case "randomsending" -> handleRandomSendingCommand(args);
            case "greeting" -> handleGreetingCommand(args);
            case "numberreplacement" -> handleNumberReplacementCommand(args);
            case "minconsecutive" -> handleMinConsecutiveCommand(args);
            default -> log.warn(LangManager.get("xinpga.command.unknown", args[0]));
        }
    }

    private void handleNumberReplacementCommand(String[] args) {
        if (args.length < 2) {
            log.info(LangManager.get("xinpga.command.usage", "/xpa numberreplacement <on|off>"));
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
                log.warn(LangManager.get("xinpga.command.mode.invalid", "numberreplacement", "on, off"));
                break;
        }
    }

    private void handleMinConsecutiveCommand(String[] args) {
        if (args.length < 2) {
            log.info(LangManager.get("xinpga.command.usage", "/xpa minconsecutive <数字>"));
            return;
        }
        
        try {
            int minConsecutive = Integer.parseInt(args[1]);
            if (minConsecutive <= 0) {
                log.warn(LangManager.get("xinpga.number.replace.min.error"));
                return;
            }
            XinPga.INSTANCE.cmdSetMinConsecutiveNumbers(minConsecutive);
        } catch (NumberFormatException e) {
            log.warn(LangManager.get("xinpga.command.mode.invalid", "minconsecutive", LangManager.get("xinpga.command.usage", "整数")));
        }
    }

    private void handleGreetingCommand(String[] args) {
        if (args.length < 2) {
            log.info(LangManager.get("xinpga.command.usage", "/xpa greeting <enable|disable|format> [格式]"));
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
                    log.info(LangManager.get("xinpga.command.usage", "/xpa greeting format <格式>"));
                    log.info("提示：格式中可以使用 #name# 来表示玩家名");
                    return;
                }
                
                String format = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                XinPga.INSTANCE.cmdSetGreetingFormat(format);
            }
            default -> log.warn(LangManager.get("xinpga.command.mode.invalid", "greeting", "enable, disable, format"));
        }
    }

    private void forceStop(String[] args){
        XinPga.INSTANCE.cmdForceStop();
    }

    private void handleRandomSendingCommand(String[] args) {
        if (args.length < 2) {
            log.info(LangManager.get("xinpga.command.usage", "/xpa randomSending <on|off>"));
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
                log.warn(LangManager.get("xinpga.command.mode.invalid", "randomSending", "on, off"));
                break;
        }
    }

    private void handleStringCommand(String[] args) {
        if (args.length < 3) {
            log.info(LangManager.get("xinpga.command.usage", "/xpa string <编号> <新文本>"));
            return;
        }
        try {
            int index = Integer.parseInt(args[1]) - 1;
            String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            XinPga.INSTANCE.cmdString(index, text);
        } catch (NumberFormatException e) {
            log.warn(LangManager.get("xinpga.command.mode.invalid", "string", LangManager.get("xinpga.command.usage", "整数")));
        }
    }

    private void handleAddMessageCommand(String[] args) {
        if (args.length < 2) {
            log.info(LangManager.get("xinpga.command.usage", "/xpa addmessage <消息内容>"));
            return;
        }
        XinPga.INSTANCE.cmdAddMessage(String.join(" ", args).substring(args[0].length() + 1));
    }

    private void handleRemoveMessageCommand(String[] args) {
        if (args.length < 2) {
            log.info(LangManager.get("xinpga.command.usage", "/xpa removemessage <消息内容>"));
            return;
        }
        XinPga.INSTANCE.cmdRemoveMessage(String.join(" ", args).substring(args[0].length() + 1));
    }

    private void handleTimeCommand(String[] args) {
        if (args.length < 2) {
            log.info(LangManager.get("xinpga.command.usage", "/xpa time <秒>"));
            return;
        }
        try {
            int sec = Integer.parseInt(args[1]);
            if (sec <= 0) {
                log.warn(LangManager.get("xinpga.command.mode.invalid", "time", LangManager.get("xinpga.command.usage", "大于0的整数")));
                return;
            }
            XinPga.INSTANCE.cmdTime(sec);
        } catch (NumberFormatException e) {
            log.warn(LangManager.get("xinpga.command.mode.invalid", "time", LangManager.get("xinpga.command.usage", "整数")));
        }
    }

    private void handleModeCommand(String[] args) {
        if (args.length < 2) {
            log.info(LangManager.get("xinpga.command.usage", "/xpa mode <PUBLIC|PRIVATE>"));
            return;
        }
        XinPga.INSTANCE.cmdSendMode(args[1]);
    }

    private void handlePrivateIntervalCommand(String[] args) {
        if (args.length < 2) {
            log.info(LangManager.get("xinpga.command.usage", "/xpa privateinterval <秒>"));
            return;
        }
        try {
            int sec = Integer.parseInt(args[1]);
            XinPga.INSTANCE.cmdPrivateInterval(sec);
        } catch (NumberFormatException e) {
            log.warn(LangManager.get("xinpga.command.mode.invalid", "privateinterval", LangManager.get("xinpga.command.usage", "整数")));
        }
    }

    private void handleMessageIntervalCommand(String[] args) {
        if (args.length < 2) {
            log.info(LangManager.get("xinpga.command.usage", "/xpa messageinterval <秒>"));
            return;
        }
        try {
            int sec = Integer.parseInt(args[1]);
            XinPga.INSTANCE.cmdMessageInterval(sec);
        } catch (NumberFormatException e) {
            log.warn(LangManager.get("xinpga.command.mode.invalid", "messageinterval", LangManager.get("xinpga.command.usage", "整数")));
        }
    }

    private void handleAdminCommand(String[] args) {
        // 注意：这个方法只被控制台命令调用，不应该检查remoteCommandAdminEnabled
        // remoteCommandAdminEnabled只应该限制远程命令，不应该限制控制台命令

        if (args.length < 2) {
            log.info(LangManager.get("xinpga.command.usage", "/xpa admin add <玩家名> | remove <玩家名> | list"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "add" -> {
                if (args.length < 3) {
                    log.info(LangManager.get("xinpga.command.usage", "/xpa admin add <玩家名>"));
                    return;
                }
                XinPga.INSTANCE.cmdAddAdministrator(args[2]);
            }
            case "remove" -> {
                if (args.length < 3) {
                    log.info(LangManager.get("xinpga.command.usage", "/xpa admin remove <玩家名>"));
                    return;
                }
                XinPga.INSTANCE.cmdRemoveAdministrator(args[2]);
            }
            case "list" -> XinPga.INSTANCE.cmdListAdministrators();
            default -> log.warn(LangManager.get("xinpga.command.unknown", "admin"));
        }
    }

    private void handleBlacklistCommand(String[] args) {
        if (args.length < 2) {
            log.info(LangManager.get("xinpga.command.usage", "/xpa blacklist add <玩家名> | remove <玩家名> | list"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "add" -> {
                if (args.length < 3) {
                    log.info(LangManager.get("xinpga.command.usage", "/xpa blacklist add <玩家名>"));
                    return;
                }
                XinPga.INSTANCE.cmdAddToBlacklist(args[2]);
            }
            case "remove" -> {
                if (args.length < 3) {
                    log.info(LangManager.get("xinpga.command.usage", "/xpa blacklist remove <玩家名>"));
                    return;
                }
                XinPga.INSTANCE.cmdRemoveFromBlacklist(args[2]);
            }
            case "list" -> XinPga.INSTANCE.cmdListBlacklist();
            default -> log.warn(LangManager.get("xinpga.command.unknown", "blacklist"));
        }
    }

    private void showHelp() {
        log.info(LangManager.get("xinpga.help.title"));
        log.info(LangManager.get("xinpga.help.start"));
        log.info(LangManager.get("xinpga.help.stop"));
        log.info(LangManager.get("xinpga.help.multistart"));
        log.info(LangManager.get("xinpga.help.multistop"));
        log.info(LangManager.get("xinpga.help.string"));
        log.info(LangManager.get("xinpga.help.addmessage"));
        log.info(LangManager.get("xinpga.help.removemessage"));
        log.info(LangManager.get("xinpga.help.listmessages"));
        log.info(LangManager.get("xinpga.help.time"));
        log.info(LangManager.get("xinpga.help.mode"));
        log.info(LangManager.get("xinpga.help.privateinterval"));
        log.info(LangManager.get("xinpga.help.messageinterval"));
        log.info(LangManager.get("xinpga.help.randomsending"));
        log.info(LangManager.get("xinpga.help.numberreplacement"));
        log.info(LangManager.get("xinpga.help.minconsecutive"));
        log.info(LangManager.get("xinpga.help.greeting"));
        log.info(LangManager.get("xinpga.help.greeting.format"));
        log.info(LangManager.get("xinpga.help.updateplayerlist"));
        log.info(LangManager.get("xinpga.help.blacklist.add"));
        log.info(LangManager.get("xinpga.help.blacklist.remove"));
        log.info(LangManager.get("xinpga.help.blacklist.list"));
        log.info(LangManager.get("xinpga.help.admin.add"));
        log.info(LangManager.get("xinpga.help.admin.remove"));
        log.info(LangManager.get("xinpga.help.admin.list"));
        log.info(LangManager.get("xinpga.help.reload"));
        log.info(LangManager.get("xinpga.help.debug"));
        log.info(LangManager.get("xinpga.help.help"));
    }
}