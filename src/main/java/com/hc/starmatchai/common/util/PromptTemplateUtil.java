package com.hc.starmatchai.common.util;

import com.hc.starmatchai.common.dto.model.ZodiacSign;
import com.hc.starmatchai.common.enums.LanguageEnum;
import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import cn.hutool.core.date.DateUtil;

/**
 * AI模型提示词模板工具类
 * 负责管理不同语言的提示词模板和段落标记
 */
@Component
public class PromptTemplateUtil {

    /**
     * 获取匹配分析提示词
     * 
     * @param sign1           第一个人的星座
     * @param person1Birthday 第一个人的生日
     * @param sign2           第二个人的星座
     * @param person2Birthday 第二个人的生日
     * @param language        语言代码
     * @return 生成的提示词
     */
    public String getMatchAnalysisPrompt(ZodiacSign sign1, Date person1Birthday, ZodiacSign sign2, Date person2Birthday,
            String language) {
        String person1BirthdayStr = DateUtil.format(person1Birthday, "yyyy-MM-dd HH:mm");
        String person2BirthdayStr = DateUtil.format(person2Birthday, "yyyy-MM-dd HH:mm");

        // 获取对应语言的模板，如果没有则使用中文模板
        PromptTemplate template = getPromptTemplates().getOrDefault(language,
                getPromptTemplates().get(LanguageEnum.ZH_CN.getCode()));
        return template.generate(sign1, person1BirthdayStr, sign2, person2BirthdayStr);
    }

    /**
     * 获取不同语言的段落标记
     * 
     * @return 语言到段落标记的映射
     */
    public Map<String, SectionMarkers> getSectionMarkersMap() {
        Map<String, SectionMarkers> markersMap = new HashMap<>();

        // 中文标记
        markersMap.put(LanguageEnum.ZH_CN.getCode(), new SectionMarkers(
                "匹配得分：",
                "整体分析：",
                "优势特点：",
                "潜在问题：",
                "相处建议："));

        // 英文标记
        markersMap.put(LanguageEnum.EN_US.getCode(), new SectionMarkers(
                "Compatibility Score:",
                "Overall Analysis:",
                "Strengths:",
                "Potential Issues:",
                "Advice:"));

        return markersMap;
    }

    /**
     * 获取提示词模板映射
     * 
     * @return 语言到提示词模板的映射
     */
    private Map<String, PromptTemplate> getPromptTemplates() {
        Map<String, PromptTemplate> promptTemplates = new HashMap<>();

        // 中文模板
        promptTemplates.put(LanguageEnum.ZH_CN.getCode(), (s1, p1Date, s2, p2Date) -> new StringBuilder()
                .append("作为一位专业的占星师，请分析")
                .append(p1Date)
                .append("出生的人")
                .append(s1.getChineseName())
                .append("和")
                .append(p2Date)
                .append("出生的人")
                .append(s2.getChineseName())
                .append("的匹配关系。请严格按照以下格式输出：\n\n")
                .append("匹配得分：[请综合考虑双方星座属性和出生时间细节，在0-100分范围内给出精确匹配评分。即使是相同星座组合，也应基于出生时间差异给出不同分数。特别注意：避免使用73、75等重复性评分，每次分析必须给出新的随机分数。无论何种组合，请确保分数多样化且有意义。仅输出数字]\n\n")
                .append("整体分析：[从星座特质的角度分析两个星座的整体匹配程度]\n\n")
                .append("优势特点：[分析两个星座在感情中的互补优势]\n\n")
                .append("潜在问题：[指出可能存在的性格冲突或沟通障碍]\n\n")
                .append("相处建议：[给出具体的相处建议和改善方法]\n\n")
                .append("请确保严格按照以上格式回复，每个部分都要有实质性的内容。")
                .toString());

        // 英文模板
        promptTemplates.put(LanguageEnum.EN_US.getCode(), (s1, p1Date, s2, p2Date) -> new StringBuilder()
                .append("As a professional astrologer, please analyze the compatibility between a ")
                .append(s1.name()) // 使用枚举类型的name()方法获取枚举名称
                .append(" born on ")
                .append(p1Date)
                .append(" and a ")
                .append(s2.name()) // 使用枚举类型的name()方法获取枚举名称
                .append(" born on ")
                .append(p2Date)
                .append(". Please provide your analysis in the following format:\n\n")
                .append("Compatibility Score: [Consider both zodiac properties and detailed birth time information to provide an exact compatibility score between 0-100. Even for identical zodiac combinations, assign different scores based on birth time variations. Important: Avoid repetitive scores like 73 or 75. Each analysis must produce a new randomized score. For any combination, ensure scores are diverse and meaningful. Number only]\n\n")
                .append("Overall Analysis: [Analyze the overall compatibility between these two zodiac signs]\n\n")
                .append("Strengths: [Analyze the complementary strengths these signs bring to a relationship]\n\n")
                .append("Potential Issues: [Point out potential personality conflicts or communication challenges]\n\n")
                .append("Advice: [Provide specific advice for improving their relationship]\n\n")
                .append("Please ensure you strictly follow this format with substantive content in each section.")
                .toString());

        return promptTemplates;
    }

    /**
     * 提示词模板函数式接口
     */
    @FunctionalInterface
    private interface PromptTemplate {
        String generate(ZodiacSign sign1, String person1BirthdayStr, ZodiacSign sign2, String person2BirthdayStr);
    }

    /**
     * 不同语言的段落标记类
     */
    @Data
    public static class SectionMarkers {
        private final String scoreMarker;
        private final String analysisMarker;
        private final String advantagesMarker;
        private final String disadvantagesMarker;
        private final String suggestionsMarker;

        public SectionMarkers(String scoreMarker, String analysisMarker, String advantagesMarker,
                String disadvantagesMarker, String suggestionsMarker) {
            this.scoreMarker = scoreMarker;
            this.analysisMarker = analysisMarker;
            this.advantagesMarker = advantagesMarker;
            this.disadvantagesMarker = disadvantagesMarker;
            this.suggestionsMarker = suggestionsMarker;
        }

    }
}