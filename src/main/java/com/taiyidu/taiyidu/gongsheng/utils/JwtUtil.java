package com.taiyidu.taiyidu.gongsheng.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    /**
     * 生成JWT令牌（带参数版本，配合JwtProperties使用）
     * @param secretKey 密钥
     * @param ttl       过期时间（毫秒）
     * @param claims    存放的数据
     */
    public static String generateJwt(String secretKey, Long ttl, Map<String, Object> claims) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + ttl))
                .signWith(key)
                .compact();
    }

    /**
     * 解析JWT令牌
     * @param secretKey 密钥（与生成时一致）
     * @param jwt       JWT令牌
     */
    public static Claims parseJWT(String secretKey, String jwt) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getBody();
    }
}
