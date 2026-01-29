package top.mcocet.xinpga.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.Bot;
import org.geysermc.mcprotocollib.auth.GameProfile;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import top.mcocet.xinpga.XinPga;
import top.mcocet.xinpga.config.XinPgaConfig;

/**
 * 多线程公告消息发送器
 * 只在主发送模式为公告发送模式时启用，拥有独立的线程控制和发送信息
 */
public class MultiThreadAnnouncementSender {
    private static final Logger log = LoggerFactory.getLogger(MultiThreadAnnouncementSender.class);
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
            log.warn("[多线程发送] 主发送模式不是私聊模式，无法启动多线程发送功能");
            return;
        }
        
        if (!xinPga.isRunning) {
            log.warn("[多线程发送] 主发送功能未启动，无法启动多线程发送功能");
            return;
        }
        
        if (isMultiThreadRunning) {
            log.warn("[多线程发送] 多线程发送功能已经在运行中");
            return;
        }

        isMultiThreadRunning = true;
        
        mainThread = new Thread(() -> {
            try {
                log.info("[多线程发送] 多线程公告发送功能已启动");
                
                while (isMultiThreadRunning && xinPga.isRunning) {
                    if (xinPga.isSuspended) {
                        log.info("[多线程发送] 任务被暂停，等待恢复...");
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
                
                log.info("[多线程发送] 多线程公告发送功能已停止");
            } catch (InterruptedException e) {
                log.info("[多线程发送] 多线程发送功能被中断");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("[多线程发送] 多线程发送过程中发生错误: ", e);
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
            log.info("[多线程发送] 多线程发送功能未运行");
            return;
        }
        
        log.info("[多线程发送] 正在停止多线程公告发送功能...");
        isMultiThreadRunning = false;
        
        // 中断主线程
        if (mainThread != null) {
            mainThread.interrupt();
        }
        
        // 中断所有活动的发送线程
        interruptAllSendingThreads();
        
        log.info("[多线程发送] 多线程公告发送功能已停止");
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
        if (XinPga.INSTANCE.isSuspended) {
            log.info("[多线程发送] 任务被远程命令暂停，跳过发送公告");
            return;
        }

        Thread sendingThread = new Thread(() -> {
            try {
                // 将当前线程添加到活动线程集合中
                activeSendingThreads.add(Thread.currentThread());
                
                XinPga xinPga = XinPga.INSTANCE;
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
                        message = replaceNumbersWithMathFont(message, xinPga.getConfig().getMinConsecutiveNumbers());
                    }
                    
                    // 根据原始配置决定是否添加随机字符串
                    if (xinPga.getConfig().isAppendRandom()) {
                        message += " " + xinPga.randomString(xinPga.getConfig().getRandomLength());
                    }
                    
                    try {
                        Bot.Instance.sendChatMessage(message);
                        log.debug("[多线程发送] 已发送公告: {}", message);
                    } catch (Exception e) {
                        log.error("[多线程发送] 发送公告失败: {}", e.getMessage());
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
                           System.currentTimeMillis() < endTime) {
                        try {
                            Thread.sleep(Math.min(50, endTime - System.currentTimeMillis()));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.info("[多线程发送] 发送公告被中断");
                            return;
                        }
                    }
                }
                
                log.info("[多线程发送] 已完成发送多线程公告");
            } catch (Exception e) {
                log.error("[多线程发送] 发送多线程公告时发生错误: ", e);
            } finally {
                // 无论正常结束还是异常退出，都要从活动线程集合中移除
                activeSendingThreads.remove(Thread.currentThread());
            }
        }, "MultiThreadAnnouncementSender-Announcement");
        
        sendingThread.start();
    }
    
    /**
     * 替换消息中的数字为数学字体，只有当连续数字的数量达到或超过指定阈值时才替换
     * @param message 原始消息
     * @param minConsecutiveNumbers 最少连续数字数量
     * @return 替换后的消息
     */
    private static String replaceNumbersWithMathFont(String message, int minConsecutiveNumbers) {
        if (message == null || minConsecutiveNumbers <= 0) {
            return message;
        }
        
        // 使用正则表达式查找连续的数字
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(message);
        
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String matchedNumbers = matcher.group();
            
            // 检查匹配的数字串长度是否达到最小连续数字数量
            if (matchedNumbers.length() >= minConsecutiveNumbers) {
                // 将整个数字串中的每个数字替换为数学字体
                StringBuilder replacement = new StringBuilder();
                for (char digit : matchedNumbers.toCharArray()) {
                    replacement.append(convertDigitToMathFont(digit));
                }
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement.toString()));
            } else {
                // 没有达到最小连续数字数量，保持原样
                matcher.appendReplacement(result, Matcher.quoteReplacement(matchedNumbers));
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 将单个数字字符转换为对应的数学粗体数字
     * @param digit 数字字符 (0-9)
     * @return 数学粗体数字字符串
     */
    private static String convertDigitToMathFont(char digit) {
        switch (digit) {
            case '0': return "\uD835\uDFCE"; // 𝟎 (U+1D7CE)
            case '1': return "\uD835\uDFCF"; // 𝟏 (U+1D7CF)
            case '2': return "\uD835\uDFD0"; // 𝟐 (U+1D7D0)
            case '3': return "\uD835\uDFD1"; // 𝟑 (U+1D7D1)
            case '4': return "\uD835\uDFD2"; // 𝟒 (U+1D7D2)
            case '5': return "\uD835\uDFD3"; // 𝟓 (U+1D7D3)
            case '6': return "\uD835\uDFD4"; // 𝟔 (U+1D7D4)
            case '7': return "\uD835\uDFD5"; // 𝟕 (U+1D7D5)
            case '8': return "\uD835\uDFD6"; // 𝟖 (U+1D7D6)
            case '9': return "\uD835\uDFD7"; // 𝟗 (U+1D7D7)
            default: return String.valueOf(digit); // 如果不是数字，保持原样
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
                    log.info("[多线程发送] 已中断发送线程: " + thread.getName());
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