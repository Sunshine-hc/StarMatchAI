package com.hc.starmatchai.common.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 语言枚举类
 */
@Getter
public enum LanguageEnum {
    ZH_CN("zh-CN", "中文"),
    EN_US("en-US", "英文");

    private final String code;
    private final String name;

    LanguageEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static LanguageEnum getByCode(String code) {
        return Arrays.stream(LanguageEnum.values())
                .filter(language -> language.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
    
}
