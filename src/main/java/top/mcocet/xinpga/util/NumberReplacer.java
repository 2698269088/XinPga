package top.mcocet.xinpga.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数字替换工具类
 * 用于将阿拉伯数字替换为数学字体
 */
public class NumberReplacer {
    
    /**
     * 替换消息中的数字为数学字体，只有当连续数字的数量达到或超过指定阈值时才替换
     * @param message 原始消息
     * @param minConsecutiveNumbers 最少连续数字数量
     * @return 替换后的消息
     */
    public static String replaceNumbersWithMathFont(String message, int minConsecutiveNumbers) {
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
}