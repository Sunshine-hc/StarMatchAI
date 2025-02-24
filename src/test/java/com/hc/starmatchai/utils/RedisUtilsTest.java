package com.hc.starmatchai.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedisUtilsTest {

    @Autowired
    private RedisUtils redisUtils;

    @Test
    public void testRedis() {
        // 测试数据
        String key = "test:key";
        String value = "Hello Redis!";
        long ttl = 60;

        // 保存数据
        boolean saveResult = redisUtils.set(key, value, ttl);
        System.out.println("保存结果：" + saveResult);

        // 获取数据
        Object getValue = redisUtils.get(key);
        System.out.println("获取的值：" + getValue);

        // 删除数据（测试完成后清理）
//        redisUtils.delete(key);
    }
}