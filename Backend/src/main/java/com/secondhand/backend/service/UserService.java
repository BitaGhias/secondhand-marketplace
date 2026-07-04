package com.secondhand.backend.service;

import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        // مقایسه مستقیم و متنی پسوردها
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("نام کاربری یا رمز عبور اشتباه است");
        }
        return user;
    }
}