package com.secondhand.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // فعال کردن امنیت وب
public class SecurityConfig { // بررسی درخواست ها قبل از رسیدن به کنترلر

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // غیرفعال کردن CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // بدون حالت (Stateless) - هیچ جلسه‌ای ذخیره نمیشه
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // تنظیم دسترسی‌ها (ترتیب قوانین مهم است: از خاص به عام)
                .authorizeHttpRequests(auth -> auth
                        // --- ثبت‌نام و ورود (عمومی) ---
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()

                        // --- مسیرهای ادمین (قبل از الگوهای عمومی تعریف می‌شوند) ---
                        .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/items/pending").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/items/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/items/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categories/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/cities/add").hasRole("ADMIN")

                        // --- مسیرهای احرازشده که با الگوی {id} تداخل دارند ---
                        .requestMatchers(HttpMethod.GET, "/api/items/user", "/api/items/purchased").authenticated()

                        // --- مسیرهای عمومی (بدون نیاز به توکن) ---
                        .requestMatchers(HttpMethod.GET,
                                "/api/items/approved",
                                "/api/items/search",
                                "/api/items/category/**",
                                "/api/items/city/**",
                                "/api/items/{id}",
                                "/api/items/{id}/images",
                                "/api/categories/all",
                                "/api/categories/roots",
                                "/api/categories/popular",
                                "/api/categories/{id}/subcategories",
                                "/api/cities",
                                "/uploads/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/items/search/advanced").permitAll()

                        // --- بقیه مسیرها نیاز به توکن دارن ---
                        .anyRequest().authenticated()
                )

                // اضافه کردن فیلتر JWT قبل از فیلتر پیش‌فرض Spring Security
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
