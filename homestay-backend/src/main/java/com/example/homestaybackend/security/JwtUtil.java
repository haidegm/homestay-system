package com.example.homestaybackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // 【关键改动】使用固定的字符串生成 Key，不要让它动态生成
    // 注意：这个字符串长度至少要 32 位，否则会报错
    private final String SECRET_STRING = "homestay_backend_secret_key_fixed_12345678";
    private final Key key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    private final long expiration = 1000 * 60 * 60 * 24;

    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // 这里的转换要稳健，先把 Object 转成 Number 再转 Long
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Key getKey() {
        return key;
    }
}