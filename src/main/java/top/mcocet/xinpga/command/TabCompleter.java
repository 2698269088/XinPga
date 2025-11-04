package top.mcocet.xinpga.command;

import xin.bbtt.mcbot.command.Command;

import java.util.List;

public class TabCompleter {
    public List<String> getCompletions(Command cmd, String label, String[] args) {
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