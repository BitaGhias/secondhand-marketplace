package com.secondhand.backend.security;

import com.secondhand.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                logger.warn("توکن JWT نامعتبر است: {}", e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // FIX: اگر کاربر مسدود/حذف شده باشد، loadUserByUsername خطا پرتاب می‌کند؛
            // قبلاً این خطا کنترل نمی‌شد و باعث خطای خام (500) در تمام درخواست‌های بعدی کاربر مسدودشده می‌شد.
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token, username)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    logger.debug("کاربر با موفقیت احراز هویت شد: {}", username);
                } else {
                    logger.warn("توکن JWT نامعتبر است یا منقضی شده است.");
                }
            } catch (UsernameNotFoundException e) {
                logger.warn("دسترسی رد شد برای '{}': {}", username, e.getMessage());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"message\":\"" + e.getMessage().replace("\"", "'") + "\",\"statusCode\":403,\"status\":\"FORBIDDEN\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}