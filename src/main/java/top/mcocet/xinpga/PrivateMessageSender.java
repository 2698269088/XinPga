package top.mcocet.xinpga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.Bot;
import org.geysermc.mcprotocollib.auth.GameProfile;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PrivateMessageSender {
    private static final Logger log = LoggerFactory.getLogger(PrivateMessageSender.class);
    static final AtomicInteger currentPlayerIndex = new AtomicInteger(0);
    static List<String> cachedPlayerList = new ArrayList<>();
    private static String botName = null;

    // 设置Bot名称
    public static void setBotName(String name) {
        botName = name;
    }

    // 更新在线玩家列表
    public static void updateOnlinePlayerList() {
        // 获取当前在线玩家列表
        Map<UUID, GameProfile> players = Bot.Instance.players;

        List<String> onlinePlayers = players.values().stream()
                .map(GameProfile::getName)
                .filter(name -> !name.equals(botName)) // 过滤掉自己
                .collect(Collectors.toList());

        // 过滤掉黑名单中的玩家
        List<String> nonBlacklistedPlayers = onlinePlayers.stream()
                .filter(player -> !XinPga.INSTANCE.getConfig().isPlayerBlacklisted(player))
                .collect(Collectors.toList());

        // 更新缓存列表
        cachedPlayerList = new ArrayList<>(nonBlacklistedPlayers);
        currentPlayerIndex.set(0);

        log.info("已更新在线玩家列表: " + cachedPlayerList);
    }


    // 获取下一个要发送的玩家
    public static String getNextPlayer() {
        // 如果缓存列表为空，更新列表
        if (cachedPlayerList.isEmpty()) {
            updateOnlinePlayerList();
        }

        // 如果仍然没有玩家可发送，则返回null
        if (cachedPlayerList.isEmpty()) {
            return null;
        }

        // 获取当前玩家索引
        int index = currentPlayerIndex.get();

        // 如果索引超出范围，重置列表并从头开始
        if (index >= cachedPlayerList.size()) {
            updateOnlinePlayerList();
            index = currentPlayerIndex.get(); // 重新获取索引（应该为0）

            // 如果仍然没有玩家可发送，则返回null
            if (cachedPlayerList.isEmpty()) {
                return null;
            }
        }

        String playerName = cachedPlayerList.get(index);
        currentPlayerIndex.incrementAndGet(); // 移动到下一个玩家

        // 检查玩家是否仍然在线
        if (isPlayerOnline(playerName) && !XinPga.INSTANCE.getConfig().isPlayerBlacklisted(playerName)) {
            return playerName;
        } else {
            // 玩家不在线或在黑名单中，跳过该玩家，递归获取下一个玩家
            return getNextPlayer();
        }
    }

    // 检查玩家是否在线
    private static boolean isPlayerOnline(String playerName) {
        Map<UUID, GameProfile> players = Bot.Instance.players;
        return players.values().stream()
                .map(GameProfile::getName)
                .anyMatch(name -> name.equals(playerName));
    }

    // 支持发送多条消息给同一个玩家
    public static void sendPrivateMessagesToPlayer(String playerName, List<String> messages, boolean appendRandom, int randomLength) {
        // 再次检查玩家是否在线且不在黑名单中
        if (!isPlayerOnline(playerName) || XinPga.INSTANCE.getConfig().isPlayerBlacklisted(playerName)) {
            log.info("玩家 " + playerName + " 不在线或在黑名单中，跳过发送");
            return;
        }

        new Thread(() -> {
            try {
                XinPga xinPga = XinPga.INSTANCE;
                for (int i = 0; i < messages.size(); i++) {
                    String message = messages.get(i);
                    // 再次检查玩家是否仍然在线
                    if (!isPlayerOnline(playerName)) {
                        log.info("玩家 " + playerName + " 已离线，停止发送消息");
                        return;
                    }

                    // 私聊模式不添加随机字符串
                    Bot.Instance.sendCommand("msg " + playerName + " " + message);
                    //log.info("已发送私聊消息给玩家：" + playerName + " 内容: " + message);

                    // 如果不是最后一条消息，则等待指定间隔
                    if (i < messages.size() - 1) {
                        Thread.sleep(xinPga.getConfig().getMessageInterval() * 1000L);
                    }
                }
                log.info("已发送所有私聊消息给玩家：" + playerName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // 保持原有的方法以保证兼容性
    public static void sendPrivateMessages(String message) {
        String playerName = getNextPlayer();
        if (playerName != null) {
            // 发送私聊消息
            Bot.Instance.sendCommand("msg " + playerName + " " + message);
            log.info("已发送私聊消息给玩家：" + playerName);
        }
    }

    // 新增方法：获取当前玩家列表和索引状态（用于调试）
    public static void printPlayerListStatus() {
        log.info("当前玩家列表: " + cachedPlayerList);
        log.info("当前索引: " + currentPlayerIndex.get());
    }
}
