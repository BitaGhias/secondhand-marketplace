package com.secondhand.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    //  کلید مخفی برای امضای توکن
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    //  مدت اعتبار توکن (۲۴ ساعت)
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 ساعت

    //  گرفتن کلید مخفی
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    //  ساختن توکن جدید
    public String generateToken(String username, Long userId, String role) {
        return Jwts.builder()
                .subject(username)                    // username
                .claim("userId", userId)              // userId
                .claim("role", role)                  // role
                .issuedAt(new Date())                 // زمان ساخت
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // زمان انقضا
                .signWith(getSigningKey())            // امضا با کلید مخفی
                .compact();                           // تبدیل به رشته
    }

    //  گرفتن username از توکن
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    //  گرفتن userId از توکن
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    //  گرفتن role از توکن
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    //  چک کردن انقضای توکن
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // اعتبارسنجی توکن
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // استخراج همه اطلاعات از توکن
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}