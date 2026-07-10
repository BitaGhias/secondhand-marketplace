package com.secondhand.backend.config;

import com.secondhand.backend.constant.Role;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner { //با اولین ران، یه ادمین و کابر تست میسازه

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override // اجرا پس از بالا اومدن کامل برنامه
    public void run(String... args) throws Exception {
        // بررسی وجود ادمین
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setFullName("مدیر سیستم");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setBlocked(false);
            admin.setPhoneNumber("09123456789");
            admin.setEmail("admin@example.com");

            userRepository.save(admin);
            System.out.println("✅ ادمین پیش‌فرض ایجاد شد: username=admin, password=admin123, phone=09123456789");
        }

        // ایجاد کاربر تست
        if (!userRepository.existsByUsername("testuser")) {
            User testUser = new User();
            testUser.setFullName("کاربر تست");
            testUser.setUsername("testuser");
            testUser.setPassword(passwordEncoder.encode("123456"));
            testUser.setRole(Role.USER);
            testUser.setActive(true);
            testUser.setBlocked(false);
            testUser.setPhoneNumber("09123456788");
            testUser.setEmail("test@example.com");

            userRepository.save(testUser);
            System.out.println("✅ کاربر تست ایجاد شد: username=testuser, password=123456, phone=09123456788");
        }
    }
}