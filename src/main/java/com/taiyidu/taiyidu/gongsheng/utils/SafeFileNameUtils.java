package com.taiyidu.taiyidu.gongsheng.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SafeFileNameUtils {
    // 2. 抖音专用正则：只匹配 v.douyin.com 抖音短链接（精准）
    private static final Pattern DOUYIN_PATTERN = Pattern.compile("https?://v\\.douyin\\.com/[a-zA-Z0-9_\\-]+");

    /**
     * 只提取抖音v.douyin短链接（推荐你的场景）
     */
    public static String extractDouyinUrl(String text) {
        String url = new String();
        if (text == null || text.isEmpty()) return url;
        Matcher matcher = DOUYIN_PATTERN.matcher(text);
        if(matcher.find()){
            url = matcher.group();
        }
        return url;
    }
}
