package com.secondhand.backend.config;

import com.secondhand.backend.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired; // برای تزریق وابستگی ها
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { // برگردوندن زنجیره فیلتر های امنیتی
        http
                //  غیرفعال کردن CSRF
                .csrf(AbstractHttpConfigurer::disable)

                //  بدون حالت (Stateless) - هیچ جلسه‌ای ذخیره نمیشه
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                //  تنظیم دسترسی‌ها
                .authorizeHttpRequests(auth -> auth
                        //  مسیرهایی که نیاز به توکن ندارن
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/items/approved",
                                "/api/items/{id}",
                                "/api/categories/all",
                                "/api/cities",
                                "/api/items/search",
                                "/api/items/category/**",
                                "/api/items/city/**"
                        ).permitAll()

                        //  مسیرهای محافظت‌شده نیاز به توکن دارن
                        .requestMatchers(
                                "/api/items/create",
                                "/api/items/user",
                                "/api/items/{id}",
                                "/api/items/*/sold",
                                "/api/chat/**",
                                "/api/favorites/**",
                                "/api/ratings/**",
                                "/api/comments/**"
                        ).authenticated()


                        //  مسیرهای ادمین
                        .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/items/pending").hasRole("ADMIN")
                        .requestMatchers("/api/items/*/status").hasRole("ADMIN")
                        .requestMatchers("/api/categories/create").hasRole("ADMIN")
                        .requestMatchers("/api/categories/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/cities/add").hasRole("ADMIN")
                        .requestMatchers("/api/auth/admin/make-admin").hasRole("ADMIN")

                        .anyRequest().authenticated()  // بقیه مسیرها نیاز به توکن دارن
                )

                //  اضافه کردن فیلتر JWT قبل از فیلتر پیش‌فرض Spring Security
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}