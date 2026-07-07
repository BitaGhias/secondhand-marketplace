package com.secondhand.backend.service;

import com.secondhand.backend.constant.Role;
import com.secondhand.backend.dto.UserRegisterRequest;
import com.secondhand.backend.dto.UserResponse;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserResponse convertToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getRole(),
                user.isBlocked()
        );
    }

    public UserResponse registerUser(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("نام کاربری تکراری است!");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setBlocked(false);

        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    public UserResponse loginUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("نام کاربری یا رمز عبور اشتباه است"));

        if (user.isBlocked()) {
            throw new RuntimeException("حساب کاربری شما توسط ادمین مسدود شده است!");
        }

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("نام کاربری یا رمز عبور اشتباه است");
        }

        return convertToResponse(user);
    }

    public List<UserResponse> getAllUsers(Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("کاربر درخواست‌کننده یافت نشد"));

        if (requester.getRole() != Role.ADMIN) {
            throw new RuntimeException("شما دسترسی ادمین به این عملیات را ندارید!");
        }

        List<User> users = userRepository.findAll();
        List<UserResponse> responses = new ArrayList<>();
        for (User u : users) {
            responses.add(convertToResponse(u));
        }
        return responses;
    }

    public UserResponse toggleUserBlockStatus(Long adminId, Long userId, boolean block) {
        User requester = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("کاربر درخواست‌کننده یافت نشد"));

        if (requester.getRole() != Role.ADMIN) {
            throw new RuntimeException("شما دسترسی ادمین به این عملیات را ندارید!");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("کاربر مورد نظر یافت نشد"));

        if (targetUser.getRole() == Role.ADMIN) {
            throw new RuntimeException("شما نمی‌توانید حساب‌های سطح ادمین را مسدود کنید!");
        }

        targetUser.setBlocked(block);
        User updatedUser = userRepository.save(targetUser);

        return convertToResponse(updatedUser);
    }
}