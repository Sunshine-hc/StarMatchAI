package com.hc.starmatchai.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.Set;

@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 设置带过期时间的缓存
     *
     * @param key      键
     * @param value    值
     * @param time     时间
     * @param timeUnit 时间单位
     */
    public void set(String key, Object value, long time, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, time, timeUnit);
    }

    /**
     * 设置缓存
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取缓存
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     *
     * @param key 键
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     *
     * @param key      键
     * @param time     时间
     * @param timeUnit 时间单位
     * @return true成功 false失败
     */
    public Boolean expire(String key, long time, TimeUnit timeUnit) {
        return redisTemplate.expire(key, time, timeUnit);
    }

    /**
     * 删除匹配pattern的所有key
     *
     * @param pattern 模式字符串
     */
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}