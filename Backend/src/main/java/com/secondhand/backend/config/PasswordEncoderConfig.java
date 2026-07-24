package com.secondhand.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder; //برای رعایت قوانین هش پسورد

//برای هش کردن پسورد و تبدیل ان به متنی غیر قابل فهم
/**
 * Configuration class: "password encoder config".
 * <p>
 * This class is part of the application configuration and is loaded by Spring at startup.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Configuration //کلاس تنظیمات
public class PasswordEncoderConfig {

    /**
     * Performs the "password encoder" operation.
     *
     * @return the resulting {@code PasswordEncoder} instance
     */
    @Bean //این شیء رو تو بساز و هر وقت کسی بهش نیاز داشت، بهش بده
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}