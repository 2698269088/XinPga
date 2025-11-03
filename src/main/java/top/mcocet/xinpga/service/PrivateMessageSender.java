// PrivateMessageSender.java
package top.mcocet.xinpga.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.Bot;
import org.geysermc.mcprotocollib.auth.GameProfile;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import top.mcocet.xinpga.XinPga;

public class PrivateMessageSender {
    private static final Logger log = LoggerFactory.getLogger(PrivateMessageSender.class);
    static final AtomicInteger currentPlayerIndex = new AtomicInteger(0);
    static List<String> cachedPlayerList = Collections.synchronizedList(new ArrayList<>());
    private static String botName = null;
    private static volatile long lastUpdateTime = 0;
    private static volatile int configVersion = 0;

    public static void setBotName(String name) {
        botName = name;
    }

    public static void forceUpdate() {
        configVersion++;
        lastUpdateTime = 0;
        cachedPlayerList.clear();
        currentPlayerIndex.set(0);
    }

    public static void updateOnlinePlayerList() {
        if (configVersion > 0 && System.currentTimeMillis() - lastUpdateTime < 5000) {
            return;
        }
        lastUpdateTime = System.currentTimeMillis();

        Map<UUID, GameProfile> players = Bot.Instance.players;

        List<String> onlinePlayers = players.values().stream()
                .map(GameProfile::getName)
                .filter(name -> !name.equals(botName))
                .collect(Collectors.toList());

        List<String> nonBlacklistedPlayers = onlinePlayers.stream()
                .filter(player -> !XinPga.INSTANCE.getConfig().isPlayerBlacklisted(player))
                .collect(Collectors.toList());

        cachedPlayerList = new ArrayList<>(nonBlacklistedPlayers);
        currentPlayerIndex.set(0);

        log.info("已更新在线玩家列表: " + cachedPlayerList);
    }

    public static String getNextPlayer() {
        if (cachedPlayerList.isEmpty()) {
            updateOnlinePlayerList();
        }

        if (cachedPlayerList.isEmpty()) {
            return null;
        }

        int index = currentPlayerIndex.get();

        if (index >= cachedPlayerList.size()) {
            updateOnlinePlayerList();
            index = currentPlayerIndex.get();

            if (cachedPlayerList.isEmpty()) {
                return null;
            }
        }

        String playerName = cachedPlayerList.get(index);
        currentPlayerIndex.incrementAndGet();

        if (isPlayerOnline(playerName) && !XinPga.INSTANCE.getConfig().isPlayerBlacklisted(playerName)) {
            return playerName;
        } else {
            return getNextPlayer();
        }
    }

    private static boolean isPlayerOnline(String playerName) {
        Map<UUID, GameProfile> players = Bot.Instance.players;
        return players.values().stream()
                .map(GameProfile::getName)
                .anyMatch(name -> name.equals(playerName));
    }

    public static void sendPrivateMessagesToPlayer(String playerName, List<String> messages, boolean appendRandom, int randomLength) {
        if (XinPga.INSTANCE.isSuspended) {
            log.info("任务已被远程命令暂停，跳过发送给玩家: {}", playerName);
            return;
        }

        if (!isPlayerOnline(playerName) || XinPga.INSTANCE.getConfig().isPlayerBlacklisted(playerName)) {
            log.info("玩家 " + playerName + " 不在线或在黑名单中，跳过发送");
            return;
        }

        new Thread(() -> {
            try {
                XinPga xinPga = XinPga.INSTANCE;
                for (int i = 0; i < messages.size(); i++) {
                    if (!xinPga.isRunning) {
                        log.info("检测到停止指令，中断发送给玩家: " + playerName);
                        return;
                    }

                    String message = messages.get(i);
                    if (appendRandom) {
                        message += " " + xinPga.randomString(randomLength);
                    }
                    try {
                        Bot.Instance.sendCommand("msg " + playerName + " " + message);
                    } catch (Exception e) {
                        log.error("发送私聊消息给玩家 {} 失败: {}", playerName, e.getMessage());
                    }

                    if (i < messages.size() - 1) {
                        long waitTime = xinPga.getConfig().getMessageInterval() * 1000L;
                        long startTime = System.currentTimeMillis();
                        while (xinPga.isRunning && (System.currentTimeMillis() - startTime) < waitTime) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }

                        if (!xinPga.isRunning) {
                            log.info("检测到停止指令，中断发送给玩家: " + playerName);
                            return;
                        }
                    }
                }
                log.info("已发送所有私聊消息给玩家：" + playerName);
            } catch (Exception e) {
                log.error("发送私聊消息时发生错误: ", e);
            }
        }).start();
    }

    public static void printPlayerListStatus() {
        log.info("当前玩家列表: " + cachedPlayerList);
        log.info("当前索引: " + currentPlayerIndex.get());
    }
}
