package com.hc.starmatchai.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final RedisUtil redisUtil;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    // Redis中存储token的key前缀
    private static final String TOKEN_KEY_PREFIX = "user:token:";

    /**
     * 从Base64编码的字符串获取签名密钥
     * 使用HMAC-SHA256算法，密钥长度至少为256位
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成token并存入Redis
     */
    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        // 生成唯一的jti (JWT ID)
        String jti = UUID.randomUUID().toString();
        claims.put("jti", jti);

        // 创建JWT
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        // 将token存入Redis，使用userId和jti作为key的一部分
        String redisKey = TOKEN_KEY_PREFIX + userId + ":" + jti;
        redisUtil.set(redisKey, token, jwtExpiration, TimeUnit.MILLISECONDS);

        return token;
    }

    /**
     * 从token中提取用户ID
     */
    public Long extractUserId(String token) {
        try {
            return extractClaim(token, claims -> claims.get("userId", Long.class));
        } catch (Exception e) {
            log.error("Failed to extract userId from token", e);
            return null;
        }
    }

    /**
     * 从token中提取JWT ID
     */
    public String extractJti(String token) {
        return extractClaim(token, claims -> claims.get("jti").toString());
    }

    /**
     * 从token中提取指定的claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析token获取所有claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查token是否过期
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 从token中提取过期时间
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 验证token是否有效
     * 1. JWT格式和签名是否正确
     * 2. 是否过期
     * 3. 是否存在于Redis中
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }

    /**
     * 使token失效（登出时调用）
     */
    public void invalidateToken(String token) {
        try {
            Long userId = extractUserId(token);
            String jti = extractJti(token);

            String redisKey = TOKEN_KEY_PREFIX + userId + ":" + jti;
            redisUtil.delete(redisKey);
        } catch (Exception e) {
            // 忽略异常，可能是token已经无效
        }
    }

    /**
     * 使指定用户的所有token失效（修改密码、强制下线等场景）
     */
    public void invalidateAllUserTokens(Long userId) {
        // 使用Redis的模式匹配功能，删除该用户的所有token
        String pattern = TOKEN_KEY_PREFIX + userId + ":*";
        redisUtil.deleteByPattern(pattern);
    }

    /**
     * 刷新token（延长有效期）
     */
    public String refreshToken(String token) {
        try {
            Long userId = extractUserId(token);
            String jti = extractJti(token);

            // 先使旧token失效
            String redisKey = TOKEN_KEY_PREFIX + userId + ":" + jti;
            redisUtil.delete(redisKey);

            // 生成新token
            return generateToken(userId);
        } catch (Exception e) {
            throw new RuntimeException("刷新token失败", e);
        }
    }
}