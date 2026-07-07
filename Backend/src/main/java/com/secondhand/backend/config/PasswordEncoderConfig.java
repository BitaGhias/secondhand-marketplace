package com.secondhand.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder; //برای رعایت قوانین هش پسورد

//برای هش کردن پسورد و تبدیل ان به متنی غیر قابل فهم
@Configuration //کلاس تنظیمات
public class PasswordEncoderConfig {

    @Bean //ساخت ابزاری برای استفاده از بقیه
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}