package com.hc.starmatchai.common.enums;

import lombok.Getter;

@Getter
public enum AIModelEnum {
    DEEPSEEK("deepseek-chat", "DeepSeek Chat"),
    CHATGLM("chatglm", "ChatGLM"),
    QWEN("qwen-turbo", "通义千问-Turbo"),
    BAICHUAN("baichuan", "百川大模型"),
    WENXIN("wenxin", "文心一言")
    ;

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