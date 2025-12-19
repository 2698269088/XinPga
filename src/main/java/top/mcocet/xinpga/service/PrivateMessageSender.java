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
    
    // 用于跟踪所有正在发送消息的线程
    private static final Set<Thread> activeSendingThreads = Collections.synchronizedSet(new HashSet<>());

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
        // 减少缓存时间限制，使其适应更短的消息间隔设置
        if (configVersion > 0 && System.currentTimeMillis() - lastUpdateTime < 500) {
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

        Thread sendingThread = new Thread(() -> {
            try {
                // 将当前线程添加到活动线程集合中
                activeSendingThreads.add(Thread.currentThread());
                
                XinPga xinPga = XinPga.INSTANCE;
                
                // 处理问候语
                String greetingMessage = null;
                if (xinPga.getConfig().isGreetingEnabled()) {
                    String format = xinPga.getConfig().getGreetingFormat();
                    if (format.contains("#name#")) {
                        greetingMessage = format.replace("#name#", playerName);
                    } else {
                        greetingMessage = format;
                    }
                }
                
                for (int i = 0; i < messages.size(); i++) {
                    // 更频繁地检查运行状态
                    if (!xinPga.isRunning) {
                        log.info("检测到停止指令，中断发送给玩家: " + playerName);
                        return;
                    }

                    String message = messages.get(i);
                    
                    // 如果启用了问候语，则在每条消息前添加问候语
                    if (greetingMessage != null) {
                        message = greetingMessage + message;
                    }
                    
                    // 本来私聊模式是不会添加随机字符串的
                    // 但是不知道为什么，重构后的代码莫名其妙的加上字符串了
                    // 我懒得找原因了，直接在这里添加一个判断，去除随机字符串
                    if (appendRandom && xinPga.getConfig().getSendMode() == XinPga.SendMode.PUBLIC) {
                        message += " " + xinPga.randomString(randomLength);
                    }
                    try {
                        Bot.Instance.sendCommand("msg " + playerName + " " + message);
                    } catch (Exception e) {
                        log.error("发送私聊消息给玩家 {} 失败: {}", playerName, e.getMessage());
                    }

                    // 如果不是最后一条消息，则等待
                    if (i < messages.size() - 1) {
                        long waitTime = xinPga.getConfig().getMessageInterval() * 1000L;
                        long startTime = System.currentTimeMillis();
                        
                        // 使用更短的睡眠间隔以提高响应性
                        while (xinPga.isRunning && (System.currentTimeMillis() - startTime) < waitTime) {
                            try {
                                Thread.sleep(Math.min(50, waitTime - (System.currentTimeMillis() - startTime)));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                log.info("发送给玩家 " + playerName + " 的消息被中断");
                                return;
                            }
                        }

                        // 再次检查运行状态
                        if (!xinPga.isRunning) {
                            log.info("检测到停止指令，中断发送给玩家: " + playerName);
                            return;
                        }
                    }
                }
                log.info("已发送所有私聊消息给玩家：" + playerName);
            } catch (Exception e) {
                log.error("发送私聊消息时发生错误: ", e);
            } finally {
                // 无论正常结束还是异常退出，都要从活动线程集合中移除
                activeSendingThreads.remove(Thread.currentThread());
            }
        });
        
        sendingThread.start();
    }
    
    /**
     * 强制中断所有正在进行的消息发送线程
     */
    public static void interruptAllSendingThreads() {
        synchronized (activeSendingThreads) {
            for (Thread thread : activeSendingThreads) {
                if (thread != null && thread.isAlive()) {
                    thread.interrupt();
                    log.info("已中断发送线程: " + thread.getName());
                }
            }
            activeSendingThreads.clear();
        }
    }

    public static void printPlayerListStatus() {
        log.info("当前玩家列表: " + cachedPlayerList);
        log.info("当前索引: " + currentPlayerIndex.get());
    }
}