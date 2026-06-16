package top.mcocet.xinpga.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.LangManager;

import java.util.List;
import java.util.concurrent.*;

import top.mcocet.xinpga.XinPga;
import top.mcocet.xinpga.config.XinPgaConfig;
import top.mcocet.xinpga.util.NumberReplacer;

public class MessageScheduler {
    private static final Logger log = LoggerFactory.getLogger("MessageScheduler");

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> task;
    private final XinPga xinPga;
    private final XinPgaConfig config;

    public MessageScheduler(XinPga xinPga, XinPgaConfig config) {
        this.xinPga = xinPga;
        this.config = config;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        stop();
        // 等待确保任务完全停止后再启动新任务
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        ensureSchedulerAvailable();

        xinPga.isRunning = true;

        // 只有在私聊模式下才需要更新玩家列表
        if (config.getSendMode() == XinPga.SendMode.PRIVATE) {
            PrivateMessageSender.updateOnlinePlayerList();
        }

        try {
            if (config.getSendMode() == XinPga.SendMode.PRIVATE) {
                long initialDelay = getRandomizedMainInterval(config.getPrivateMessageInterval());
                task = scheduler.scheduleWithFixedDelay(this::sendPrivateMessages, initialDelay, getRandomizedMainInterval(config.getPrivateMessageInterval()), TimeUnit.SECONDS);
            } else {
                long initialDelay = getRandomizedMainInterval(config.getIntervalSeconds());
                task = scheduler.scheduleWithFixedDelay(this::sendPublicMessages, initialDelay, getRandomizedMainInterval(config.getIntervalSeconds()), TimeUnit.SECONDS);
            }
            log.info(LangManager.get("xinpga.scheduler.started", config.getSendMode()));
        } catch (RejectedExecutionException e) {
            log.error(LangManager.get("xinpga.scheduler.start.failed", e.getMessage()));
            ensureSchedulerAvailable();
        }
    }

    /**
     * 获取带随机偏差的主线程发送间隔
     */
    private long getRandomizedMainInterval(int baseInterval) {
        if (!config.isRandomIntervalEnabled()) {
            return baseInterval;
        }
        int deviation = config.getMainIntervalDeviation();
        int min = Math.max(1, baseInterval - deviation);
        int max = baseInterval + deviation;
        return min + (int) (Math.random() * (max - min + 1));
    }

    public void forceStop() {
        // 立即强制停止所有任务
        xinPga.isRunning = false;
        
        // 取消定时任务
        if (task != null) {
            task.cancel(true);
            task = null;
        }
        
        // 强制中断所有正在进行的发送线程
        PrivateMessageSender.interruptAllSendingThreads();
        
        // 强制关闭线程池
        if (scheduler != null) {
            try {
                scheduler.shutdownNow();
            } catch (Exception e) {
                // 忽略异常
            }
            scheduler = null;
        }
    }

    public void stop() {
        xinPga.isRunning = false;
        if (task != null) {
            task.cancel(true);
            task = null;
        }
        
        // 强制中断所有正在进行的发送线程
        PrivateMessageSender.interruptAllSendingThreads();
        
        // 增加等待时间并使用中断机制确保线程及时停止
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void ensureSchedulerAvailable() {
        if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }
    }

    private void sendPublicMessages() {
        List<String> messages = config.getMessages();
        if (messages.isEmpty()) return;

        new Thread(() -> {
            try {
                // 如果启用了随机发送，则只发送一条随机消息
                if (config.isRandomSendingEnabled()) {
                    if (!xinPga.isRunning) return;
                    
                    // 随机选择一条消息
                    String message = messages.get((int) (Math.random() * messages.size()));
                    
                    // 如果启用了主发送模式数字替换功能，则对消息进行数字替换
                    if (config.isMainNumberReplacementEnabled()) {
                        message = NumberReplacer.replaceNumbersWithMathFont(message, config.getMinConsecutiveNumbers());
                    }
                    
                    if (config.isAppendRandom()) {
                        message += " " + xinPga.randomString(config.getRandomLength());
                    }
                    Bot.INSTANCE.sendChatMessage(message);
                    
                    // 添加控制台提示
                    if (xinPga.isRunning) {
                        log.info(LangManager.get("xinpga.scheduler.random.sent"));
                    }
                } else {
                    // 原有的顺序发送所有消息逻辑
                    for (int i = 0; i < messages.size(); i++) {
                        if (!xinPga.isRunning) return;

                        String message = messages.get(i);
                        
                        // 如果启用了主发送模式数字替换功能，则对消息进行数字替换
                        if (config.isMainNumberReplacementEnabled()) {
                            message = NumberReplacer.replaceNumbersWithMathFont(message, config.getMinConsecutiveNumbers());
                        }
                        
                        if (config.isAppendRandom()) {
                            message += " " + xinPga.randomString(config.getRandomLength());
                        }
                        Bot.INSTANCE.sendChatMessage(message);

                        if (i < messages.size() - 1) {
                            long waitTime = getRandomizedMainMessageInterval(config.getMessageInterval()) * 1000L;
                            long startTime = System.currentTimeMillis();
                            while (xinPga.isRunning && (System.currentTimeMillis() - startTime) < waitTime) {
                                Thread.sleep(100);
                            }
                        }
                    }

                    // 在所有消息发送完毕后添加控制台提示
                    if (xinPga.isRunning) {
                        log.info(LangManager.get("xinpga.scheduler.round.completed"));
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * 获取带随机偏差的主线程消息间发送间隔
     */
    private int getRandomizedMainMessageInterval(int baseInterval) {
        if (!config.isRandomIntervalEnabled()) {
            return baseInterval;
        }
        int deviation = config.getMainMessageIntervalDeviation();
        int min = Math.max(1, baseInterval - deviation);
        int max = baseInterval + deviation;
        return min + (int) (Math.random() * (max - min + 1));
    }

    private void sendPrivateMessages() {
        if (xinPga.isSuspended) return;

        List<String> messages = config.getMessages();
        if (messages.isEmpty()) return;

        String currentPlayer = PrivateMessageSender.getNextPlayer();
        if (currentPlayer != null) {
            // 如果启用了随机发送，则只发送一条随机消息
            if (config.isRandomSendingEnabled()) {
                String message = messages.get((int) (Math.random() * messages.size()));
                PrivateMessageSender.sendPrivateMessagesToPlayer(currentPlayer, List.of(message), config.isAppendRandom(), config.getRandomLength());
            } else {
                PrivateMessageSender.sendPrivateMessagesToPlayer(currentPlayer, messages, config.isAppendRandom(), config.getRandomLength());
            }
        }
    }

    public void shutdown() {
        stop();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}