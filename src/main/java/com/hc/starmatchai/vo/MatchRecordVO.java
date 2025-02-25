package com.hc.starmatchai.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Schema(description = "匹配记录信息")
public class MatchRecordVO {
    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "第一个人的生日", example = "1990-01-15 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date person1Birthday;

    @Schema(description = "第二个人的生日", example = "1992-07-20 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date person2Birthday;

    @Schema(description = "第一个人的星座", example = "白羊座")
    private String person1Sign;

    @Schema(description = "第二个人的星座", example = "天秤座")
    private String person2Sign;

    @Schema(description = "匹配分数", example = "85")
    private Integer matchScore;

    @Schema(description = "匹配分析")
    private String analysis;

    @Schema(description = "优势")
    private String advantages;

    @Schema(description = "劣势")
    private String disadvantages;

    @Schema(description = "建议")
    private String suggestions;

    @Schema(description = "使用的AI模型", example = "gpt-4")
    private String aiModel;

    @Schema(description = "创建时间", example = "2023-05-20 14:30:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}