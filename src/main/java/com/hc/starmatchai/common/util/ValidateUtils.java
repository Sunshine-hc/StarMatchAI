package com.hc.starmatchai.common.util;

import com.hc.starmatchai.common.exception.BusinessException;
import com.hc.starmatchai.common.enums.AIModelEnum;
import com.hc.starmatchai.common.dto.model.MatchRequest;
import org.apache.commons.lang3.StringUtils;
import java.util.Date;

public class ValidateUtils {

    public static void validateMatchRequest(MatchRequest request) {
        Date person1Birthday = request.getPerson1Birthday();
        Date person2Birthday = request.getPerson2Birthday();
        String aiModel = request.getAiModel();

        Date now = new Date();
        if (person1Birthday == null || person2Birthday == null) {
            throw new BusinessException(400, "生日不能为空");
        }
        if (person1Birthday.after(now) || person2Birthday.after(now)) {
            throw new BusinessException(400, "生日不能晚于当前时间");
        }
        if (StringUtils.isBlank(aiModel)) {
            throw new BusinessException(400, "AI模型不能为空");
        }
        try {
            AIModelEnum.getByCode(aiModel);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "不支持的AI模型类型");
        }
    }
}