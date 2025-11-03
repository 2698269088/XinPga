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
    static List<String> cachedPlayerList = Collections.synchronizedList(new ArrayList<>());
    private static String botName = null;
    private static volatile long lastUpdateTime = 0;
    private static volatile int configVersion = 0; // 配置版本控制

    // 设置Bot名称
    public static void setBotName(String name) {
        botName = name;
    }

    // 强制更新配置版本
    public static void forceUpdate() {
        configVersion++;
        lastUpdateTime = 0; // 强制下次更新
        cachedPlayerList.clear();
        currentPlayerIndex.set(0);
    }

    // 更新在线玩家列表
    public static void updateOnlinePlayerList() {
        // 如果配置版本变化，强制更新
        if (configVersion > 0 && System.currentTimeMillis() - lastUpdateTime < 5000) {
            return;
        }
        lastUpdateTime = System.currentTimeMillis();
        /*
        // 可以考虑添加时间间隔限制，避免过于频繁的更新
        if (System.currentTimeMillis() - lastUpdateTime < 1000) { // 1秒内只更新一次
            return;
        }
        lastUpdateTime = System.currentTimeMillis();
        */
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
        // 检查是否被远程命令暂停
        if (XinPga.INSTANCE.isSuspended) {
            log.info("任务已被远程命令暂停，跳过发送给玩家: {}", playerName);
            return;
        }
        // 再次检查玩家是否在线且不在黑名单中
        if (!isPlayerOnline(playerName) || XinPga.INSTANCE.getConfig().isPlayerBlacklisted(playerName)) {
            log.info("玩家 " + playerName + " 不在线或在黑名单中，跳过发送");
            return;
        }

        new Thread(() -> {
            try {
                XinPga xinPga = XinPga.INSTANCE;
                for (int i = 0; i < messages.size(); i++) {
                    // 检查停止标志
                    if (!xinPga.isRunning) {
                        log.info("检测到停止指令，中断发送给玩家: " + playerName);
                        return;
                    }

                    String message = messages.get(i);
                    try {
                        Bot.Instance.sendCommand("msg " + playerName + " " + message);
                    } catch (Exception e) {
                        log.error("发送私聊消息给玩家 {} 失败: {}", playerName, e.getMessage());
                    }

                    // 如果不是最后一条消息，则等待指定间隔（可中断的等待）
                    if (i < messages.size() - 1) {
                        long waitTime = xinPga.getConfig().getMessageInterval() * 1000L;
                        long startTime = System.currentTimeMillis();
                        // 可中断的等待
                        while (xinPga.isRunning && (System.currentTimeMillis() - startTime) < waitTime) {
                            try {
                                Thread.sleep(100); // 每100ms检查一次
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                        // 再次检查停止标志
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

    // 获取当前玩家列表和索引状态（用于调试）
    public static void printPlayerListStatus() {
        log.info("当前玩家列表: " + cachedPlayerList);
        log.info("当前索引: " + currentPlayerIndex.get());
    }
}
