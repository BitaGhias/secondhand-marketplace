package com.secondhand.backend.util;//ابزار
//قلب سیستم احراز هویت
import io.jsonwebtoken.Claims;//برای همه اطلاعاتی که توی توکن ذخیره شده
import io.jsonwebtoken.Jwts;//کارخونه ی ساخت توکن
import io.jsonwebtoken.security.Keys;//تبدیل درست کلید مخفی به فرمت درست
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;//کلیدمخفی برای امضای توکن
import java.util.Date;

@Component
public class JwtUtil {

    //  کلید مخفی برای امضای توکن
    //برای جلوگیری از جعل توکن
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    //  مدت اعتبار توکن (۲۴ ساعت)
    //یعنی بعد 24 ساعت دوباره باید لاگین کنه
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    //  گرفتن کلید مخفی
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    //  ساختن توکن جدید
    public String generateToken(String username, Long userId, String role) {
        return Jwts.builder()
                .subject(username)                    // username و موضوع توکن
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
        return extractAllClaims(token).get("userId", Long.class); //به صورت لانگ برگردون

} //به صورت لانگ برگردون

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
                .verifyWith(getSigningKey()) //چک کردن امضای توکن
                .build() //اماده کردن رمزگشا
                .parseSignedClaims(token)
                .getPayload(); //برگردوندن بخش دوم توکن
    }
}