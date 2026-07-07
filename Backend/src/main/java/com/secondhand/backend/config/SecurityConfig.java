package com.secondhand.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; //غیرفعال کردن تنظیمات پیش فرض
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // غیرفعال کردن CSRF (قفل اضافی)
                .authorizeHttpRequests(auth -> auth //کی به چی دسترسی داره
                        .anyRequest().permitAll()       // همه درخواست‌ها مجاز
                );
        return http.build();
    }
}