package com.secondhand.backend.service;

import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void registerUser(User user) {
        if(userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("نام کاربری تکراری است");
        }
        // ذخیره مستقیم پسورد بدون هیچ انکودری!
        userRepository.save(user);
    }

    public User loginUser(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new RuntimeException("نام کاربری یا رمز عبور اشتباه است"));

        if (user.isBlocked) {
            throw new RuntimeException("حساب کاربری شما توسط ادمین مسدود شده است!");
        }

        // مقایسه مستقیم و متنی پسوردها
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("نام کاربری یا رمز عبور اشتباه است");
        }
        return user;
    }

    public List<User> getAllUsers(Long adminId) {
        if (!adminId.equals(1L)) {
            throw new RuntimeException("شما دسترسی به این عملیات را ندارید!");
        }
        return userRepository.findAll();
    }

    public User toggleUserBlockStatus(Long adminId, Long userId, boolean block) {
        if (!adminId.equals(1L)) {
            throw new RuntimeException("شما دسترسی به این عملیات را ندارید!");
        }
        if (userId.equals(1L)) {
            throw new RuntimeException("شما نمی‌توانید حساب ادمین اصلی را مسدود کنید!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد"));

        user.isBlocked = block;
        return userRepository.save(user);
    }
}