// PrivateMessageSender.java
package top.mcocet.xinpga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.Bot;
import org.geysermc.mcprotocollib.auth.GameProfile;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PrivateMessageSender {
    private static final Logger log = LoggerFactory.getLogger(PrivateMessageSender.class);
    static final AtomicInteger currentPlayerIndex = new AtomicInteger(0);
    static List<String> cachedPlayerList = new ArrayList<>();

    // 添加一个方法来获取下一个要发送的玩家
    // 添加一个方法来获取下一个要发送的玩家
    public static String getNextPlayer() {
        // 获取当前在线玩家列表
        Map<UUID, GameProfile> players = Bot.Instance.players;

        List<String> onlinePlayers = players.values().stream()
                .map(GameProfile::getName)
                .collect(Collectors.toList());

        // 如果没有其他玩家在线，则不发送
        if (onlinePlayers.isEmpty()) {
            return null;
        }

        // 过滤掉黑名单中的玩家
        List<String> nonBlacklistedPlayers = onlinePlayers.stream()
                .filter(player -> !XinPga.INSTANCE.getConfig().isPlayerBlacklisted(player))
                .collect(Collectors.toList());

        // 如果没有非黑名单玩家在线，则不发送
        if (nonBlacklistedPlayers.isEmpty()) {
            return null;
        }

        // 只有当缓存的玩家列表为空时才更新列表（即所有玩家都已接收过消息）
        if (cachedPlayerList.isEmpty()) {
            cachedPlayerList = new ArrayList<>(nonBlacklistedPlayers);
            currentPlayerIndex.set(0);
        }

        // 获取当前玩家
        int index = currentPlayerIndex.getAndIncrement();

        // 如果已经遍历完所有玩家，清空缓存列表，下次会重新获取
        if (index >= cachedPlayerList.size()) {
            cachedPlayerList.clear();
            currentPlayerIndex.set(0);
            return null;
        }

        if (index < cachedPlayerList.size()) {
            return cachedPlayerList.get(index);
        }
        return null;
    }


    // 支持发送多条消息给同一个玩家
    public static void sendPrivateMessagesToPlayer(String playerName, List<String> messages, boolean appendRandom, int randomLength) {
        new Thread(() -> {
            try {
                XinPga xinPga = XinPga.INSTANCE;
                for (int i = 0; i < messages.size(); i++) {
                    String message = messages.get(i);
                    // 私聊模式不添加随机字符串
                    Bot.Instance.sendCommand("msg " + playerName + " " + message);
                    log.info("已发送私聊消息给玩家：" + playerName + " 内容: " + message);

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
        // 获取当前在线玩家列表
        Map<UUID, GameProfile> players = Bot.Instance.players;

        List<String> onlinePlayers = players.values().stream()
                .map(GameProfile::getName)
                .collect(Collectors.toList());

        // 如果没有其他玩家在线，则不发送
        if (onlinePlayers.isEmpty()) {
            return;
        }

        // 过滤掉黑名单中的玩家
        List<String> nonBlacklistedPlayers = onlinePlayers.stream()
                .filter(player -> !XinPga.INSTANCE.getConfig().isPlayerBlacklisted(player))
                .collect(Collectors.toList());

        // 如果没有非黑名单玩家在线，则不发送
        if (nonBlacklistedPlayers.isEmpty()) {
            return;
        }

        // 每次都更新缓存的玩家列表（强制刷新）
        cachedPlayerList = new ArrayList<>(nonBlacklistedPlayers);
        //currentPlayerIndex.set(0); // 重置索引

        // 循环发送给每个非黑名单玩家
        int index = currentPlayerIndex.getAndIncrement();
        if (index >= cachedPlayerList.size()) {
            currentPlayerIndex.set(0);
            index = 0;
        }

        if (index < cachedPlayerList.size()) {
            String playerName = cachedPlayerList.get(index);
            // 发送私聊消息（不添加随机字符串）
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
