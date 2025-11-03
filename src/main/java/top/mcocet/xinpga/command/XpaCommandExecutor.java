package top.mcocet.xinpga.command;

import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.TabExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}