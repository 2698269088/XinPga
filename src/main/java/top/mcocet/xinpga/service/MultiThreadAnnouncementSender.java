package top.mcocet.xinpga.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.LangManager;
import org.geysermc.mcprotocollib.auth.GameProfile;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import top.mcocet.xinpga.XinPga;
import top.mcocet.xinpga.config.XinPgaConfig;
import top.mcocet.xinpga.util.NumberReplacer;

/**
 * 多线程公告消息发送器
 * 只在主发送模式为私聊发送模式时启用，拥有独立的线程控制和发送信息
 */
public class MultiThreadAnnouncementSender {
    private static final Logger log = LoggerFactory.getLogger("MultiThreadAnnouncementSender");
    private static volatile String botName = null;
    
    // 用于跟踪多线程发送器的活动线程
    private static final Set<Thread> activeSendingThreads = Collections.synchronizedSet(new HashSet<>());
    
    // 控制多线程发送器的状态
    private static volatile boolean isMultiThreadRunning = false;
    private static volatile Thread mainThread = null;

    public static void setBotName(String name) {
        botName = name;
    }

    /**
     * 启动多线程公告发送功能
     * 只有在主发送模式为私聊模式且主发送功能已启动时才允许启动
     */
    public static void startMultiThreadSending() {
        XinPga xinPga = XinPga.INSTANCE;
        
        // 检查前提条件：主发送模式必须是私聊模式且主发送功能已启动
        if (xinPga.getConfig().getSendMode() != XinPga.SendMode.PRIVATE) {
            log.warn(LangManager.get("xinpga.multithread.not.private.log"));
            return;
        }
        
        if (!xinPga.isRunning) {
            log.warn(LangManager.get("xinpga.multithread.not.started.log"));
            return;
        }
        
        if (isMultiThreadRunning) {
            log.warn(LangManager.get("xinpga.multithread.already.running.log"));
            return;
        }

        isMultiThreadRunning = true;
        
        mainThread = new Thread(() -> {
            try {
                log.info(LangManager.get("xinpga.multithread.start.log"));
                
                XinPga xinPgaLocal = XinPga.INSTANCE;
                
                while (isMultiThreadRunning && xinPgaLocal.isRunning) {
                    if (xinPgaLocal.isSuspended) {
                        log.info(LangManager.get("xinpga.multithread.paused.log"));
                        Thread.sleep(1000); // 暂停期间每秒检查一次
                        continue;
                    }
                    
                    // 获取多线程发送的独立配置信息
                    List<String> multiThreadMessages = getMultiThreadMessages();
                    int duration = getMultiThreadDuration();
                    int interval = getMultiThreadInterval();
                    
                    if (multiThreadMessages != null && !multiThreadMessages.isEmpty()) {
                        // 发送多线程消息
                        sendMultiThreadAnnouncements(multiThreadMessages, duration, interval);
                    }
                    
                    // 等待指定的间隔后再发送下一轮
                    Thread.sleep(getMultiThreadCheckInterval() * 1000L);
                }
                
                log.info(LangManager.get("xinpga.multithread.stop.log"));
            } catch (InterruptedException e) {
                log.info(LangManager.get("xinpga.multithread.interrupted.log"));
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(LangManager.get("xinpga.multithread.error.log"), e);
            } finally {
                isMultiThreadRunning = false;
                mainThread = null;
            }
        }, "MultiThreadAnnouncementSender-Main");
        
        mainThread.start();
    }

    /**
     * 停止多线程公告发送功能
     */
    public static void stopMultiThreadSending() {
        if (!isMultiThreadRunning) {
            log.info(LangManager.get("xinpga.multithread.not.running.log"));
            return;
        }
        
        log.info(LangManager.get("xinpga.multithread.stopping.log"));
        isMultiThreadRunning = false;
        
        // 中断主线程
        if (mainThread != null) {
            mainThread.interrupt();
        }
        
        // 中断所有活动的发送线程
        interruptAllSendingThreads();
        
        log.info(LangManager.get("xinpga.multithread.stop.log"));
    }

    /**
     * 获取多线程发送的独立消息列表
     * 从配置中获取或使用默认值
     */
    private static List<String> getMultiThreadMessages() {
        XinPga xinPga = XinPga.INSTANCE;
        XinPgaConfig config = xinPga.getConfig();
        List<String> messages = config.getMultiThreadMessages();
        
        if (messages == null || messages.isEmpty()) {
            // 如果没有配置多线程消息，则使用普通消息作为默认值
            return xinPga.getConfig().getMessages();
        }
        
        return messages;
    }

    /**
     * 获取多线程发送的持续时间（秒）
     */
    private static int getMultiThreadDuration() {
        XinPga xinPga = XinPga.INSTANCE;
        XinPgaConfig config = xinPga.getConfig();
        return config.getMultiThreadDuration();
    }

    /**
     * 获取多线程发送的消息间隔（秒）
     */
    private static int getMultiThreadInterval() {
        XinPga xinPga = XinPga.INSTANCE;
        XinPgaConfig config = xinPga.getConfig();
        return config.getMultiThreadInterval();
    }

    /**
     * 获取多线程发送检查的间隔（秒）
     */
    private static int getMultiThreadCheckInterval() {
        XinPga xinPga = XinPga.INSTANCE;
        XinPgaConfig config = xinPga.getConfig();
        return config.getMultiThreadCheckInterval();
    }

    /**
     * 发送多线程公告消息
     */
    private static void sendMultiThreadAnnouncements(List<String> messages, int duration, int interval) {
        XinPga xinPga = XinPga.INSTANCE;
        
        if (xinPga.isSuspended) {
            log.info(LangManager.get("xinpga.multithread.skipped.log"));
            return;
        }

        try {
            long startTime = System.currentTimeMillis();
            long durationMillis = duration * 1000L;
            
            int messageIndex = 0;
            while (isMultiThreadRunning && 
                   xinPga.isRunning && 
                   !xinPga.isSuspended && 
                   (System.currentTimeMillis() - startTime) < durationMillis) {
                
                String message = messages.get(messageIndex % messages.size()); // 循环使用消息
                
                // 如果启用了数字替换功能，则对消息进行数字替换
                if (xinPga.getConfig().isNumberReplacementEnabled()) {
                    message = NumberReplacer.replaceNumbersWithMathFont(message, xinPga.getConfig().getMinConsecutiveNumbers());
                }
                
                // 根据原始配置决定是否添加随机字符串
                if (xinPga.getConfig().isAppendRandom()) {
                    message += " " + xinPga.randomString(xinPga.getConfig().getRandomLength());
                }
                
                try {
                    Bot.INSTANCE.sendChatMessage(message);
                    log.debug(LangManager.get("xinpga.multithread.send.success", message));
                } catch (Exception e) {
                    log.error(LangManager.get("xinpga.multithread.send.error.log", e.getMessage()));
                }

                messageIndex++;
                
                // 等待指定的时间间隔
                long sleepTime = interval * 1000L;
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= durationMillis) {
                    break; // 超出持续时间，退出循环
                }
                
                long remainingSleep = Math.min(sleepTime, durationMillis - elapsed);
                long endTime = System.currentTimeMillis() + remainingSleep;
                
                while (isMultiThreadRunning && 
                       xinPga.isRunning && 
                       System.currentTimeMillis() < endTime && 
                       !xinPga.isSuspended) {
                    try {
                        Thread.sleep(Math.min(50, endTime - System.currentTimeMillis()));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.info(LangManager.get("xinpga.multithread.interrupted.sending.log"));
                        return;
                    }
                }
            }
            
            log.info(LangManager.get("xinpga.multithread.completed.log"));
        } catch (Exception e) {
            log.error(LangManager.get("xinpga.multithread.error.log"), e);
        }
    }
    

    
    /**
     * 强制中断所有多线程发送相关的线程
     */
    public static void interruptAllSendingThreads() {
        synchronized (activeSendingThreads) {
            for (Thread thread : activeSendingThreads) {
                if (thread != null && thread.isAlive()) {
                    thread.interrupt();
                    log.info(LangManager.get("xinpga.multithread.thread.interrupted.log", thread.getName()));
                }
            }
            activeSendingThreads.clear();
        }
    }

    /**
     * 获取多线程发送功能是否正在运行
     */
    public static boolean isMultiThreadRunning() {
        return isMultiThreadRunning;
    }
}