package com.hc.starmatchai.common.dto.model;

import com.hc.starmatchai.common.enums.AIModelEnum;
import com.hc.starmatchai.common.enums.LanguageEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
@Schema(description = "星座匹配请求参数")
public class MatchRequest {
    @Schema(description = "第一个人的生日", example = "1990-01-01", required = true)
    private Date person1Birthday;

    @Schema(description = "第一个人的星座")
    private ZodiacSign sign1;

    @Schema(description = "第二个人的生日", example = "1991-02-02", required = true)
    private Date person2Birthday;

    @Schema(description = "第二个人的星座")
    private ZodiacSign sign2;

    @Schema(description = "回答的语言", example = "zh-CN", required = true, allowableValues = { "zh-CN", "en-US" })
    private String language = LanguageEnum.ZH_CN.getCode();

    @Schema(description = "AI模型选择", example = "qwen-turbo", required = true, allowableValues = { "deepseek-chat", "chatglm",
            "qwen-turbo", "baichuan", "wenxin" })
    private String aiModel = AIModelEnum.QWEN.getCode();
}