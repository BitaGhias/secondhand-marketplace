package com.secondhand.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // غیرفعال کردن CSRF برای اینکه بتوانیم با سیستم‌های تست و فرانت به API درخواست بزنیم
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // باز کردن مسیرهای ثبت‌نام و ورود برای همه بدون نیاز به احراز هویت
                        .requestMatchers("/api/auth/**").permitAll()
                        // قفل بودن بقیه مسیرهای برنامه
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // ابزار رمزنگاری پسوردها در دیتابیس (اگر هنوز استفاده نکردی بعداً لازم می‌شود)
        return new BCryptPasswordEncoder();
    }
}