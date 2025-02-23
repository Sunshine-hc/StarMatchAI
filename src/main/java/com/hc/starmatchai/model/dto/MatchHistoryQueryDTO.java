package com.hc.starmatchai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "匹配历史查询参数")
public class MatchHistoryQueryDTO extends PageQueryDTO {

    @Schema(description = "开始时间")
    private Date startTime;

    @Schema(description = "结束时间")
    private Date endTime;

    @Schema(description = "第一个人的星座")
    private String person1Sign;

    @Schema(description = "第二个人的星座")
    private String person2Sign;

    @Schema(description = "最小匹配分数", example = "60")
    private Integer minScore;

    @Schema(description = "最大匹配分数", example = "100")
    private Integer maxScore;

    @Schema(description = "使用的AI模型", example = "deepseek")
    private String aiModel;
}