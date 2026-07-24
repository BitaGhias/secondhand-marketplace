package com.secondhand.backend.util;//ابزار
//قلب سیستم احراز هویت
import io.jsonwebtoken.Claims;//برای همه اطلاعاتی که توی توکن ذخیره شده
import io.jsonwebtoken.Jwts;//کارخونه ی ساخت توکن
import io.jsonwebtoken.security.Keys;//تبدیل درست کلید مخفی به فرمت درست
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;//کلیدمخفی برای امضای توکن
import java.util.Date;

/**
 * Utility class providing "jwt util" helpers.
 * <p>
 * This class is a helper utility whose methods are used across different parts of the application.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Component
public class JwtUtil {

    //  کلید مخفی برای امضای توکن
    //برای جلوگیری از جعل توکن
    @Value("${jwt.secret}")
    private String secretKey;

    //  مدت اعتبار توکن (۲۴ ساعت)
    //یعنی بعد 24 ساعت دوباره باید لاگین کنه
    @Value("${jwt.expiration}")
    private Long expirationTime;

    //  گرفتن کلید مخفی
    /**
     * Gets signing key.
     *
     * @return the resulting {@code SecretKey} instance
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    //  ساختن توکن جدید
    /**
     * Performs the "generate token" operation.
     *
     * @param username the username
     * @param userId id of the user
     * @param role the "role" value of type {@code String}
     * @return the resulting string
     */
    public String generateToken(String username, Long userId, String role) {
        return Jwts.builder()
                .subject(username)                    // username و موضوع توکن
                .claim("userId", userId)              // userId
                .claim("role", role)                  // role
                .issuedAt(new Date())                 // زمان ساخت
                .expiration(new Date(System.currentTimeMillis() + expirationTime)) // زمان انقضا
                .signWith(getSigningKey())            // امضا با کلید مخفی
                .compact();                           // تبدیل به رشته
    }

    //  گرفتن username از توکن
    /**
     * Extracts username.
     *
     * @param token JWT authentication token
     * @return the resulting string
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    //  گرفتن userId از توکن
    /**
     * Extracts user id.
     *
     * @param token JWT authentication token
     * @return the resulting numeric value
     */
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class); //به صورت لانگ برگردون

    }

    //  گرفتن role از توکن
    /**
     * Extracts role.
     *
     * @param token JWT authentication token
     * @return the resulting string
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    //  چک کردن انقضای توکن
    /**
     * Checks whether the "token expired" condition holds.
     *
     * @param token JWT authentication token
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // اعتبارسنجی توکن
    /**
     * Validates token.
     *
     * @param token JWT authentication token
     * @param username the username
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // استخراج همه اطلاعات از توکن
    /**
     * Extracts all claims.
     *
     * @param token JWT authentication token
     * @return the resulting {@code Claims} instance
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) //چک کردن امضای توکن
                .build() //اماده کردن رمزگشا
                .parseSignedClaims(token)
                .getPayload(); //برگردوندن بخش دوم توکن
    }
}