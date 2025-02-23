package com.hc.starmatchai.model;

import lombok.Getter;

/**
 * 星座
 */
@Getter
public enum ZodiacSign {
    ARIES("白羊座", "3.21-4.19"),
    TAURUS("金牛座", "4.20-5.20"),
    GEMINI("双子座", "5.21-6.21"),
    CANCER("巨蟹座", "6.22-7.22"),
    LEO("狮子座", "7.23-8.22"),
    VIRGO("处女座", "8.23-9.22"),
    LIBRA("天秤座", "9.23-10.23"),
    SCORPIO("天蝎座", "10.24-11.22"),
    SAGITTARIUS("射手座", "11.23-12.21"),
    CAPRICORN("摩羯座", "12.22-1.19"),
    AQUARIUS("水瓶座", "1.20-2.18"),
    PISCES("双鱼座", "2.19-3.20");

    private final String chineseName;
    private final String dateRange;

    ZodiacSign(String chineseName, String dateRange) {
        this.chineseName = chineseName;
        this.dateRange = dateRange;
    }
}