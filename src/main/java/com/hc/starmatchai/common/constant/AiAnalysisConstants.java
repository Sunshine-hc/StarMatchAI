package com.hc.starmatchai.common.constant;

/**
 * AI分析相关常量
 *
 * @author Claude
 */
public class AiAnalysisConstants {

    /**
     * Redis键前缀
     */
    public static class RedisKey {
        /** AI分析结果基础前缀 */
        public static final String AI_ANALYSIS_PREFIX = "ai:analysis:";

        /** 星座匹配分析前缀 */
        public static final String MATCH_ANALYSIS = AI_ANALYSIS_PREFIX + "match:";

        /** 运势分析前缀 */
        public static final String FORTUNE_ANALYSIS = AI_ANALYSIS_PREFIX + "fortune:";

        // ... 可以添加其他AI分析类型的前缀
    }

    /**
     * AI分析类型
     */
    public static class AnalysisType {
        /** 星座匹配分析 */
        public static final String MATCH = "match";

        /** 运势分析 */
        public static final String FORTUNE = "fortune";

        // ... 可以添加其他AI分析类型
    }

    /**
     * Redis缓存时间（单位：秒）
     */
    public static class ExpireTime {
        /** 分析结果默认过期时间：5分钟 */
        public static final int DEFAULT_EXPIRE = 300;

        /** 短期缓存时间：1分钟 */
        public static final int SHORT_EXPIRE = 60;

        /** 长期缓存时间：30分钟 */
        public static final int LONG_EXPIRE = 1800;
    }

    /**
     * 分析结果字段名
     */
    public static class AnalysisField {
        /** 匹配分数 */
        public static final String MATCH_SCORE = "score";

        /** 分析内容 */
        public static final String ANALYSIS = "analysis";

        /** 优势 */
        public static final String ADVANTAGES = "advantages";

        /** 劣势 */
        public static final String DISADVANTAGES = "disadvantages";

        /** 建议 */
        public static final String SUGGESTIONS = "suggestions";
    }

    /**
     * 错误消息
     */
    public static class ErrorMessage {
        /** 数据同步失败 */
        public static final String SYNC_FAILED = "同步数据失败";

        /** 数据未找到 */
        public static final String DATA_NOT_FOUND = "未找到分析数据";

        /** 未知分析类型 */
        public static final String UNKNOWN_ANALYSIS_TYPE = "未知的分析类型";
    }
}