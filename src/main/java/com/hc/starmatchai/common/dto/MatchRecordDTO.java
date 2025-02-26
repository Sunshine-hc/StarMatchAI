package com.hc.starmatchai.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.util.Date;

@Data
@Schema(description = "匹配记录创建信息")
public class MatchRecordDTO {
    @Schema(description = "第一个人的生日", example = "1990-01-15 00:00:00", required = true)
    @NotNull(message = "第一个人的生日不能为空")
    @Past(message = "生日必须是过去的日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date person1Birthday;

    @Schema(description = "第二个人的生日", example = "1992-07-20 00:00:00", required = true)
    @NotNull(message = "第二个人的生日不能为空")
    @Past(message = "生日必须是过去的日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date person2Birthday;

    @Schema(description = "设备ID（未登录用户使用）", example = "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6")
    private String deviceId;
}