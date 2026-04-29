package top.mcocet.xinpga.command;

import xin.bbtt.mcbot.LangManager;
import xin.bbtt.mcbot.command.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import top.mcocet.xinpga.XinPga;

public class PrivateMessageHandler {
    public List<String> handleCommandWithOutput(Command cmd, String label, String[] args) {
        List<String> output = new ArrayList<>();

        // 暂停私聊宣传任务
        XinPga.INSTANCE.isSuspended = true;
        XinPga.INSTANCE.getScheduler().stop();
        output.add(LangManager.get("xinpga.remote.task.suspended"));

        try {
            switch (args[0].toLowerCase()) {
                case "start" -> {
                    XinPga.INSTANCE.cmdStart();
                    output.add(LangManager.get("xinpga.remote.started"));
                }
                case "stop" -> {
                    XinPga.INSTANCE.cmdStop();
                    output.add(LangManager.get("xinpga.remote.stopped"));
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
                    output.add(LangManager.get("xinpga.remote.reload"));
                }
                case "updateplayerlist" -> {
                    XinPga.INSTANCE.cmdUpdatePlayerList();
                    output.add(LangManager.get("xinpga.remote.playerlist.updated"));
                }
                case "blacklist" -> handleBlacklistCommandWithOutput(args, output);
                case "admin" -> handleAdminCommandWithOutput(args, output);
                case "debug" -> output.add(LangManager.get("xinpga.remote.not.available"));
                case "forcestop" -> {
                    XinPga.INSTANCE.cmdForceStop();
                    output.add(LangManager.get("xinpga.remote.forcestop"));
                }
                case "help" -> output.addAll(showHelpOutput());
                default -> output.add(LangManager.get("xinpga.remote.unknown", args[0]));
            }
        } finally {
            // 恢复私聊宣传任务
            if (XinPga.INSTANCE.getConfig().isEnabled()) {
                XinPga.INSTANCE.getScheduler().start();
                XinPga.INSTANCE.isSuspended = false;
            }
        }

        return output;
    }

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
            }
        }
    }

    private void handleAddMessageCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa addmessage <消息内容>"));
        } else {
            String message = String.join(" ", args).substring(args[0].length() + 1);
            XinPga.INSTANCE.cmdAddMessage(message);
            output.add(LangManager.get("xinpga.command.message.added", message));
        }
    }

    private void handleRemoveMessageCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa removemessage <消息内容>"));
        } else {
            String message = String.join(" ", args).substring(args[0].length() + 1);
            boolean removed = XinPga.INSTANCE.getConfig().getMessages().remove(message);
            if (removed) {
                try {
                    XinPga.INSTANCE.getConfig().saveConfig();
                    output.add(LangManager.get("xinpga.command.message.removed", message));
                } catch (Exception e) {
                    output.add(LangManager.get("xinpga.config.save.error", e.getMessage()));
                }
            } else {
                output.add(LangManager.get("xinpga.command.multithread.message.not.found", message));
            }
        }
    }

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
    }

    private void handleTimeCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa time <秒>"));
        } else {
            try {
                int sec = Integer.parseInt(args[1]);
                XinPga.INSTANCE.cmdTime(sec);
                output.add(LangManager.get("xinpga.command.time.updated", sec));
            } catch (NumberFormatException e) {
                output.add(LangManager.get("xinpga.command.mode.invalid", "time", LangManager.get("xinpga.command.usage", "整数")));
            }
        }
    }

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

    private void handlePrivateIntervalCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa privateinterval <秒>"));
        } else {
            try {
                int sec = Integer.parseInt(args[1]);
                XinPga.INSTANCE.cmdPrivateInterval(sec);
                output.add(LangManager.get("xinpga.command.private.interval.updated", sec));
            } catch (NumberFormatException e) {
                output.add(LangManager.get("xinpga.command.mode.invalid", "privateinterval", LangManager.get("xinpga.command.usage", "整数")));
            }
        }
    }

    private void handleMessageIntervalCommandWithOutput(String[] args, List<String> output) {
        if (args.length < 2) {
            output.add(LangManager.get("xinpga.command.usage", "/xpa messageinterval <秒>"));
        } else {
            try {
                int sec = Integer.parseInt(args[1]);
                XinPga.INSTANCE.cmdMessageInterval(sec);
                output.add(LangManager.get("xinpga.command.message.interval.updated", sec));
            } catch (NumberFormatException e) {
                output.add(LangManager.get("xinpga.command.mode.invalid", "messageinterval", LangManager.get("xinpga.command.usage", "整数")));
            }
        }
    }

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

    private void handleAdminCommandWithOutput(String[] args, List<String> output) {
        // 检查远程命令的admin功能是否启用
        // 这个检查只应该在远程命令中进行，控制台命令不应该受此限制
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

    private List<String> showHelpOutput() {
        List<String> output = new ArrayList<>();
        output.add(LangManager.get("xinpga.remote.help.title"));
        output.add(LangManager.get("xinpga.remote.help.start"));
        output.add(LangManager.get("xinpga.remote.help.stop"));
        output.add(LangManager.get("xinpga.remote.help.string"));
        output.add(LangManager.get("xinpga.remote.help.addmessage"));
        output.add(LangManager.get("xinpga.remote.help.removemessage"));
        output.add(LangManager.get("xinpga.remote.help.listmessages"));
        output.add(LangManager.get("xinpga.remote.help.time"));
        output.add(LangManager.get("xinpga.remote.help.mode"));
        output.add(LangManager.get("xinpga.remote.help.privateinterval"));
        output.add(LangManager.get("xinpga.remote.help.messageinterval"));
        output.add(LangManager.get("xinpga.remote.help.randomsending"));
        output.add(LangManager.get("xinpga.remote.help.greeting"));
        output.add(LangManager.get("xinpga.remote.help.greeting.format"));
        output.add(LangManager.get("xinpga.remote.help.updateplayerlist"));
        output.add(LangManager.get("xinpga.remote.help.blacklist.add"));
        output.add(LangManager.get("xinpga.remote.help.blacklist.remove"));
        output.add(LangManager.get("xinpga.remote.help.blacklist.list"));
        output.add(LangManager.get("xinpga.remote.help.admin.add"));
        output.add(LangManager.get("xinpga.remote.help.admin.remove"));
        output.add(LangManager.get("xinpga.remote.help.admin.list"));
        output.add(LangManager.get("xinpga.remote.help.reload"));
        output.add(LangManager.get("xinpga.remote.help.debug"));
        output.add(LangManager.get("xinpga.remote.help.help"));
        return output;
    }
}