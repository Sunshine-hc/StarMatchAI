package com.hc.starmatchai.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
@Schema(description = "匹配记录")
@TableName("match_record")
public class MatchRecord {
    @Schema(description = "记录ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "第一个人的生日")
    private Date person1Birthday;

    @Schema(description = "第二个人的生日")
    private Date person2Birthday;

    @Schema(description = "第一个人的星座")
    private String person1Sign;

    @Schema(description = "第二个人的星座")
    private String person2Sign;

    @Schema(description = "匹配分数")
    private Integer matchScore;

    @Schema(description = "整体分析")
    private String analysis;

    @Schema(description = "优势特点")
    private String advantages;

    @Schema(description = "潜在问题")
    private String disadvantages;

    @Schema(description = "相处建议")
    private String suggestions;

    @Schema(description = "使用的AI模型")
    private String aiModel;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;

    @Schema(description = "是否删除")
    @TableLogic
    private Integer deleted;
}