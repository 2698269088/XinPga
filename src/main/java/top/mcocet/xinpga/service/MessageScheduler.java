package top.mcocet.xinpga.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.Bot;

import java.util.List;
import java.util.concurrent.*;

import top.mcocet.xinpga.XinPga;
import top.mcocet.xinpga.config.XinPgaConfig;

public class MessageScheduler {
    private static final Logger log = LoggerFactory.getLogger(MessageScheduler.class);

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
        ensureSchedulerAvailable();

        xinPga.isRunning = true;

        // 只有在私聊模式下才需要更新玩家列表
        if (config.getSendMode() == XinPga.SendMode.PRIVATE) {
            PrivateMessageSender.updateOnlinePlayerList();
        }

        try {
            if (config.getSendMode() == XinPga.SendMode.PRIVATE) {
                task = scheduler.scheduleWithFixedDelay(this::sendPrivateMessages, 0, config.getPrivateMessageInterval(), TimeUnit.SECONDS);
            } else {
                task = scheduler.scheduleAtFixedRate(this::sendPublicMessages, 0, config.getIntervalSeconds(), TimeUnit.SECONDS);
            }
            log.info("调度器已启动，模式: " + config.getSendMode());
        } catch (RejectedExecutionException e) {
            log.error("启动调度器失败，线程池不可用: " + e.getMessage());
            ensureSchedulerAvailable();
        }
    }

    public void stop() {
        xinPga.isRunning = false;
        if (task != null) {
            task.cancel(true);
            task = null;
        }
        try {
            Thread.sleep(100);
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
                for (int i = 0; i < messages.size(); i++) {
                    if (!xinPga.isRunning) return;

                    String message = messages.get(i);
                    if (config.isAppendRandom()) {
                        message += " " + xinPga.randomString(config.getRandomLength());
                    }
                    Bot.Instance.sendChatMessage(message);

                    if (i < messages.size() - 1) {
                        long waitTime = config.getMessageInterval() * 1000L;
                        long startTime = System.currentTimeMillis();
                        while (xinPga.isRunning && (System.currentTimeMillis() - startTime) < waitTime) {
                            Thread.sleep(100);
                        }
                    }
                }

                // 在所有消息发送完毕后添加控制台提示
                if (xinPga.isRunning) {
                    log.info("本次公告已发送完毕，准备开始发送新一轮信息");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void sendPrivateMessages() {
        if (xinPga.isSuspended) return;

        List<String> messages = config.getMessages();
        if (messages.isEmpty()) return;

        String currentPlayer = PrivateMessageSender.getNextPlayer();
        if (currentPlayer != null) {
            PrivateMessageSender.sendPrivateMessagesToPlayer(currentPlayer, messages, config.isAppendRandom(), config.getRandomLength());
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