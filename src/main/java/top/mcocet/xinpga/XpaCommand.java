package top.mcocet.xinpga;

import xin.bbtt.mcbot.command.Command;

public class XpaCommand extends Command {
    @Override
    public String getName() {
        return "xpa";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"xpa", "xinpga"};
    }

    @Override
    public String getDescription() {
        return "XinPga 定时宣传";
    }

    @Override
    public String getUsage() {
        return "/xpa start|stop|string <文本>|addmessage <消息>|removemessage <消息>|listmessages|time <秒>|mode <PUBLIC|PRIVATE>|privateinterval <秒>|messageinterval <秒>|reload|blacklist add <玩家名>|blacklist remove <玩家名>|blacklist list|updateplayerlist";
    }
}
