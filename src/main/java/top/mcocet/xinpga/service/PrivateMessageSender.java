package top.mcocet.xinpga.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.LangManager;
import org.geysermc.mcprotocollib.auth.GameProfile;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import top.mcocet.xinpga.XinPga;
import top.mcocet.xinpga.config.XinPgaConfig;
import top.mcocet.xinpga.util.NumberReplacer;

public class PrivateMessageSender {
    private static final Logger log = LoggerFactory.getLogger("PrivateMessageSender");
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

        Map<UUID, GameProfile> players = Bot.INSTANCE.players;

        List<String> onlinePlayers = players.values().stream()
                .map(GameProfile::getName)
                .filter(name -> !name.equals(botName))
                .collect(Collectors.toList());

        List<String> nonBlacklistedPlayers = onlinePlayers.stream()
                .filter(player -> !XinPga.INSTANCE.getConfig().isPlayerBlacklisted(player))
                .collect(Collectors.toList());

        cachedPlayerList = new ArrayList<>(nonBlacklistedPlayers);
        currentPlayerIndex.set(0);

        log.info(LangManager.get("xinpga.playerlist.updated.log", cachedPlayerList));
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
        Map<UUID, GameProfile> players = Bot.INSTANCE.players;
        return players.values().stream()
                .map(GameProfile::getName)
                .anyMatch(name -> name.equals(playerName));
    }

    public static void sendPrivateMessagesToPlayer(String playerName, List<String> messages, boolean appendRandom, int randomLength) {
        if (XinPga.INSTANCE.isSuspended) {
            log.info(LangManager.get("xinpga.private.skipped.log", playerName));
            return;
        }

        if (!isPlayerOnline(playerName) || XinPga.INSTANCE.getConfig().isPlayerBlacklisted(playerName)) {
            log.info(LangManager.get("xinpga.private.player.offline.log", playerName));
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
                        log.info(LangManager.get("xinpga.private.stop.detected.log", playerName));
                        return;
                    }

                    String message = messages.get(i);
                    
                    // 如果启用了主发送模式数字替换功能，则对消息进行数字替换
                    if (xinPga.getConfig().isMainNumberReplacementEnabled()) {
                        message = NumberReplacer.replaceNumbersWithMathFont(message, xinPga.getConfig().getMinConsecutiveNumbers());
                    }
                    
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
                        Bot.INSTANCE.sendCommand("msg " + playerName + " " + message);
                    } catch (Exception e) {
                        log.error(LangManager.get("xinpga.private.send.error.log", playerName, e.getMessage()));
                    }

                    // 如果不是最后一条消息，则等待
                    if (i < messages.size() - 1) {
                        long waitTime = getRandomizedMessageInterval(xinPga.getConfig()) * 1000L;
                        long startTime = System.currentTimeMillis();
                        
                        // 使用更短的睡眠间隔以提高响应性
                        while (xinPga.isRunning && (System.currentTimeMillis() - startTime) < waitTime) {
                            try {
                                Thread.sleep(Math.min(50, waitTime - (System.currentTimeMillis() - startTime)));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                log.info(LangManager.get("xinpga.private.interrupted.log", playerName));
                                return;
                            }
                        }

                        // 再次检查运行状态
                        if (!xinPga.isRunning) {
                            log.info(LangManager.get("xinpga.private.stop.detected.log", playerName));
                            return;
                        }
                    }
                }
                log.info(LangManager.get("xinpga.private.completed.log", playerName));
            } catch (Exception e) {
                log.error(LangManager.get("xinpga.private.send.error.full.log"), e);
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
                    log.info(LangManager.get("xinpga.private.thread.interrupted.log", thread.getName()));
                }
            }
            activeSendingThreads.clear();
        }
    }

    public static void printPlayerListStatus() {
        log.info(LangManager.get("xinpga.playerlist.status.current_list", cachedPlayerList));
        log.info(LangManager.get("xinpga.playerlist.status.current_index", currentPlayerIndex.get()));
    }

    /**
     * 获取带随机偏差的消息间发送间隔
     */
    private static int getRandomizedMessageInterval(XinPgaConfig config) {
        if (!config.isRandomIntervalEnabled()) {
            return config.getMessageInterval();
        }
        int baseInterval = config.getMessageInterval();
        int deviation = config.getMainMessageIntervalDeviation();
        int min = Math.max(1, baseInterval - deviation);
        int max = baseInterval + deviation;
        return min + (int) (Math.random() * (max - min + 1));
    }
}