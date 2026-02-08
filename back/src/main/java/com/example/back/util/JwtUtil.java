package com.example.back.util;

import com.example.back.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具类
 */
@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getKey().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // 密钥过短时，使用 SHA-256 拉伸到 256bit
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                keyBytes = digest.digest(keyBytes);
            } catch (Exception ignored) {
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Token
     */
    public String generateToken(String email, List<String> roles) {
        long expireMs = jwtProperties.getExpire() * 60L * 60L * 1000L;
        return generateToken(email, roles, expireMs);
    }

    /**
     * 生成 Token（自定义有效期）
     */
    public String generateToken(String email, List<String> roles, long expireMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireMs);
        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
