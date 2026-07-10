package com.secondhand.backend.service;

import com.secondhand.backend.constant.Role;
import com.secondhand.backend.dto.UserRegisterRequest;
import com.secondhand.backend.dto.UserResponse;
import com.secondhand.backend.dto.UserUpdateRequest;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ForbiddenException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired // یه ابزار ازش برام بساز
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== تبدیل ====================
    public UserResponse convertToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getRole(),
                user.isBlocked(),
                user.getPhoneNumber(),
                user.getEmail()
        );
    }

    // ==================== ثبت‌نام ====================
    public UserResponse registerUser(UserRegisterRequest request) {

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new BadRequestException("نام کامل الزامی است!");
        }

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BadRequestException("نام کاربری الزامی است!");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("نام کاربری تکراری است!");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BadRequestException("رمز عبور الزامی است!");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("ایمیل الزامی است!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("ایمیل تکراری است!");
        }

        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new BadRequestException("شماره تلفن الزامی است!");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("شماره تلفن تکراری است!");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setRole(Role.USER);
        user.setBlocked(false);
        user.setActive(true);

        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    // ==================== ورود ====================
    public UserResponse loginUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("نام کاربری یا رمز عبور اشتباه است"));

        if (user.isBlocked()) {
            throw new ForbiddenException("حساب کاربری شما توسط ادمین مسدود شده است!");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("نام کاربری یا رمز عبور اشتباه است");
        }

        return convertToResponse(user);
    }

    // ==================== گرفتن کاربر با آی‌دی ====================
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        return convertToResponse(user);
    }

    // ==================== گرفتن کاربر با نام کاربری ====================
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        return convertToResponse(user);
    }

    // ==================== گرفتن userId با نام کاربری ====================
    public Long getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        return user.getId();
    }

    // ==================== گرفتن لیست همه کاربران (فقط ادمین) ====================
    public List<UserResponse> getAllUsers(Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر درخواست‌کننده یافت نشد"));

        if (requester.getRole() != Role.ADMIN) {
            throw new ForbiddenException("شما دسترسی ادمین به این عملیات را ندارید!");
        }

        List<User> users = userRepository.findAll();
        List<UserResponse> responses = new ArrayList<>();
        for (User u : users) {
            responses.add(convertToResponse(u));
        }
        return responses;
    }

    // ==================== مسدود/فعال‌سازی کاربر (فقط ادمین) ====================
    public UserResponse toggleUserBlockStatus(Long adminId, Long userId, boolean block) {
        User requester = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر درخواست‌کننده یافت نشد"));

        if (requester.getRole() != Role.ADMIN) {
            throw new ForbiddenException("شما دسترسی ادمین به این عملیات را ندارید!");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر مورد نظر یافت نشد"));

        if (targetUser.getRole() == Role.ADMIN) {
            throw new ForbiddenException("شما نمی‌توانید حساب‌های سطح ادمین را مسدود کنید!");
        }

        targetUser.setBlocked(block);
        User updatedUser = userRepository.save(targetUser);

        return convertToResponse(updatedUser);
    }

    // ==================== تبدیل به ادمین (فقط ادمین) ====================
    public UserResponse makeAdmin(Long adminId, Long userId) {
        User requester = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر درخواست‌کننده یافت نشد"));

        if (requester.getRole() != Role.ADMIN) {
            throw new ForbiddenException("شما دسترسی ادمین به این عملیات را ندارید!");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر مورد نظر یافت نشد"));

        targetUser.setRole(Role.ADMIN);
        User updatedUser = userRepository.save(targetUser);

        return convertToResponse(updatedUser);
    }

    // ==================== ویرایش پروفایل کاربر (خود کاربر) ====================
    public UserResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));

        // به‌روزرسانی نام کامل
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }

        // بررسی شماره تلفن تکراری (اگه تغییر کرده باشه)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            if (!request.getPhoneNumber().equals(user.getPhoneNumber())) {
                if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                    throw new BadRequestException("شماره تلفن تکراری است!");
                }
                user.setPhoneNumber(request.getPhoneNumber());
            }
        }

        // بررسی ایمیل تکراری (اگه تغییر کرده باشه)
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new BadRequestException("ایمیل تکراری است!");
                }
                user.setEmail(request.getEmail());
            }
        }

        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }

    // ==================== تغییر رمز عبور (خود کاربر) ====================
    public UserResponse changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));

        // بررسی درستی رمز قدیم
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("رمز عبور فعلی اشتباه است!");
        }

        // بررسی رمز جدید (نمی‌تونه خالی باشه)
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BadRequestException("رمز عبور جدید نمی‌تواند خالی باشد!");
        }

        // بررسی اینکه رمز جدید با رمز قدیم یکی نباشه
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestException("رمز عبور جدید نمی‌تواند با رمز قبلی یکسان باشد!");
        }

        // هش کردن رمز جدید
        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);

        return convertToResponse(updatedUser);
    }
}