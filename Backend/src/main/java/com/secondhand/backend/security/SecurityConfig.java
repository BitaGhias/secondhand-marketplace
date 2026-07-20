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
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // --- ثبت‌نام و ورود (عمومی) ---
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()

                        // --- مسیرهای ادمین ---
                        .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/items/pending").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/items/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/items/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categories/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/cities/add").hasRole("ADMIN")

                        // --- مسیرهای احراز‌شده ---
                        .requestMatchers(HttpMethod.GET, "/api/items/user", "/api/items/purchased").authenticated()

                        // --- مسیرهای عمومی (بدون توکن) ---
                        .requestMatchers(HttpMethod.GET,
                                "/api/health",
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
                                // FIX: این‌ها فراموش شده بودند
                                "/api/categories/{id}",
                                "/api/ratings/seller/**",
                                "/api/comments/item/**",
                                "/uploads/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/items/search/advanced").permitAll()
                        // FIX: logout عمومی است
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()

                        // --- بقیه نیاز به توکن دارند ---
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}