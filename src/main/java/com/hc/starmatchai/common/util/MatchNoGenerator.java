package com.hc.starmatchai.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MatchNoGenerator {
    // 随机数生成器
    private static Random random = new Random();

    // 时间格式化器
    private static SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /**
     * 生成匹配编号（M + 时间戳后6位 + 4位随机数）
     *
     * @return 匹配编号
     */
    public static String generateMatchNo() {
            // 1. 获取当前时间戳（精确到毫秒）
            String timestamp = timestampFormat.format(new Date());
            // 2. 截取时间戳后6位
            String timestampSuffix = timestamp.substring(timestamp.length() - 6);
            // 3. 生成4位随机数
            String randomSuffix = String.format("%04d", random.nextInt(10000));
            // 4. 组合成匹配编号
        return "M" + timestampSuffix + randomSuffix;
    }

}
