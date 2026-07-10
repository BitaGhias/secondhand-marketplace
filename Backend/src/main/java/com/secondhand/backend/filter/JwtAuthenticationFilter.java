package com.secondhand.backend.filter;

import com.secondhand.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component // این کلاس رو بساز و توی انبار نگهش دار
public class JwtAuthenticationFilter extends OncePerRequestFilter { // هر درخواست یه بار از این فیلتر عبور میکنه

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService; //برای پیدا کردن کاربر از دیتابیس

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, // هر چیزی که فرانت به سرور میفرسته
            HttpServletResponse response,
            FilterChain filterChain // ایستگاه بعدی
    ) throws ServletException, IOException { //متد اصلی فیلتر

        String authHeader = request.getHeader("Authorization"); // انگار داری مجوز میخونی

        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // برگردوندن توکن بدون Bearer
            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                System.out.println("❌ توکن نامعتبر: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) { //اسم کاربر رو پیدا کردم و کسی هنوز احراز هویت نشده

            UserDetails userDetails = userDetailsService.loadUserByUsername(username); // پیدا کردن کاربر

            if (jwtUtil.validateToken(token, username)) {

                UsernamePasswordAuthenticationToken authToken = // نگه داشتن اطلاعات کاربر
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities() // نقش های کاربر
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("✅ کاربر احراز هویت شد: " + username);
            } else {
                System.out.println("❌ توکن معتبر نیست یا منقضی شده");
            }
        }

        filterChain.doFilter(request, response); // برو فیلتر بعدی
    }
}