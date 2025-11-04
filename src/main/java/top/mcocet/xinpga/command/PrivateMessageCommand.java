package top.mcocet.xinpga.command;

import xin.bbtt.mcbot.command.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import top.mcocet.xinpga.XinPga;

public class PrivateMessageCommand {
    public List<String> pmcWithOutput(Command cmd, String label, String[] args) {
        List<String> output = new ArrayList<>();

        if (args.length == 0) {
            output.add("用法: " + cmd.getUsage());
            return output;
        }

        PrivateMessageHandler handler = new PrivateMessageHandler();
        return handler.handleCommandWithOutput(cmd, label, args);
    }
}