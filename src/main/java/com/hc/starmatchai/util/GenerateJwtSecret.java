package com.hc.starmatchai.util;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Base64;

/**
 * 用于生成安全的JWT密钥的工具类
 */
public class GenerateJwtSecret {
    public static void main(String[] args) {
        // 生成一个安全的HS256密钥
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // 将密钥编码为Base64字符串
        String secretString = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println("=================================================");
        System.out.println("JWT Secret Key: " + secretString);
        System.out.println("=================================================");
        System.out.println("请将此密钥复制到application.properties或application.yml文件中");
        System.out.println("application.properties示例: jwt.secret=" + secretString);
        System.out.println("application.yml示例:");
        System.out.println("jwt:");
        System.out.println("  secret: " + secretString);
        System.out.println("=================================================");
    }
}