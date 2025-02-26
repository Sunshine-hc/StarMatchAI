package com.hc.starmatchai.common.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "星座匹配结果")
public class MatchResult {
    @Schema(description = "第一个人的星座")
    private ZodiacSign person1Sign;

    @Schema(description = "第二个人的星座")
    private ZodiacSign person2Sign;

    @Schema(description = "匹配分数(0-100)", example = "85")
    private Integer matchScore;

    @Schema(description = "整体分析")
    private String analysis;

    @Schema(description = "优势特点")
    private String advantages;

    @Schema(description = "潜在问题")
    private String disadvantages;

    @Schema(description = "相处建议")
    private String suggestions;
}