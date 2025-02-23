package com.hc.starmatchai.common.enums;

import lombok.Getter;

@Getter
public enum AIModelEnum {
    DEEPSEEK("deepseek", "DeepSeek Chat"),
    CHATGLM("chatglm", "ChatGLM"),
    QWEN("qwen", "通义千问"),
    BAICHUAN("baichuan", "百川大模型");

    private final String code;
    private final String desc;

    AIModelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AIModelEnum getByCode(String code) {
        for (AIModelEnum model : values()) {
            if (model.getCode().equals(code)) {
                return model;
            }
        }
        return DEEPSEEK; // 默认返回 DEEPSEEK
    }
}