package com.secondhand.backend.service;

import com.secondhand.backend.constant.Role;
import com.secondhand.backend.dto.user.UserRegisterRequest;
import com.secondhand.backend.dto.user.UserResponse;
import com.secondhand.backend.dto.user.UserUpdateRequest;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ForbiddenException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.exception.custom.UnauthorizedException;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private boolean isValidPhoneNumber(String phone) {
        if (phone == null) return false;
        String phoneRegex = "^09[0-9]{9}$";
        return phone.matches(phoneRegex);
    }

    // FIX: بررسی حداقل طول رمز عبور
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty())
            throw new BadRequestException("رمز عبور الزامی است!");
        if (password.length() < 6)
            throw new BadRequestException("رمز عبور باید حداقل ۶ کاراکتر باشد!");
    }

    public UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getRole(),
                user.isBlocked(),
                user.getPhoneNumber(),
                user.getEmail()
        );
        response.setProfileImagePath(user.getProfileImagePath());
        return response;
    }

    public UserResponse registerUser(UserRegisterRequest request) {
        if (request.getFullName() == null || request.getFullName().trim().isEmpty())
            throw new BadRequestException("نام کامل الزامی است!");

        if (request.getUsername() == null || request.getUsername().trim().isEmpty())
            throw new BadRequestException("نام کاربری الزامی است!");

        if (userRepository.existsByUsername(request.getUsername()))
            throw new BadRequestException("نام کاربری تکراری است!");

        // FIX: استفاده از validatePassword به جای بررسی null ساده
        validatePassword(request.getPassword());

        if (request.getEmail() == null || request.getEmail().trim().isEmpty())
            throw new BadRequestException("ایمیل الزامی است!");

        if (!isValidEmail(request.getEmail()))
            throw new BadRequestException("فرمت ایمیل نامعتبر است!");

        if (userRepository.existsByEmail(request.getEmail()))
            throw new BadRequestException("ایمیل تکراری است!");

        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty())
            throw new BadRequestException("شماره تلفن الزامی است!");

        if (!isValidPhoneNumber(request.getPhoneNumber()))
            throw new BadRequestException("فرمت شماره تلفن نامعتبر است! باید با 09 شروع شود و 11 رقم باشد.");

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber()))
            throw new BadRequestException("شماره تلفن تکراری است!");

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

    public UserResponse loginUser(String username, String password) {
        if (username == null || username.trim().isEmpty())
            throw new BadRequestException("نام کاربری الزامی است!");

        if (password == null || password.trim().isEmpty())
            throw new BadRequestException("رمز عبور الزامی است!");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("نام کاربری یا رمز عبور اشتباه است"));

        if (!user.isActive())
            throw new ForbiddenException("حساب کاربری شما فعال نیست!");

        if (user.isBlocked())
            throw new ForbiddenException("حساب کاربری شما توسط ادمین مسدود شده است!");

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new UnauthorizedException("نام کاربری یا رمز عبور اشتباه است");

        return convertToResponse(user);
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        return convertToResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        return convertToResponse(user);
    }

    public Long getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        return user.getId();
    }

    public List<UserResponse> getAllUsers(Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر درخواست‌کننده یافت نشد"));

        if (requester.getRole() != Role.ADMIN)
            throw new ForbiddenException("شما دسترسی ادمین به این عملیات را ندارید!");

        List<User> users = userRepository.findAll();
        List<UserResponse> responses = new ArrayList<>();
        for (User u : users) {
            responses.add(convertToResponse(u));
        }
        return responses;
    }

    public UserResponse toggleUserBlockStatus(Long adminId, Long userId, boolean block) {
        User requester = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر درخواست‌کننده یافت نشد"));

        if (requester.getRole() != Role.ADMIN)
            throw new ForbiddenException("شما دسترسی ادمین به این عملیات را ندارید!");

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر مورد نظر یافت نشد"));

        if (targetUser.getRole() == Role.ADMIN)
            throw new ForbiddenException("شما نمی‌توانید حساب‌های سطح ادمین را مسدود کنید!");

        targetUser.setBlocked(block);
        return convertToResponse(userRepository.save(targetUser));
    }

    public UserResponse makeAdmin(Long adminId, Long userId) {
        User requester = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر درخواست‌کننده یافت نشد"));

        if (requester.getRole() != Role.ADMIN)
            throw new ForbiddenException("شما دسترسی ادمین به این عملیات را ندارید!");

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر مورد نظر یافت نشد"));

        targetUser.setRole(Role.ADMIN);
        return convertToResponse(userRepository.save(targetUser));
    }

    public UserResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty())
            user.setFullName(request.getFullName());

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            if (!isValidPhoneNumber(request.getPhoneNumber()))
                throw new BadRequestException("فرمت شماره تلفن نامعتبر است!");
            if (!request.getPhoneNumber().equals(user.getPhoneNumber())) {
                if (userRepository.existsByPhoneNumber(request.getPhoneNumber()))
                    throw new BadRequestException("شماره تلفن تکراری است!");
                user.setPhoneNumber(request.getPhoneNumber());
            }
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!isValidEmail(request.getEmail()))
                throw new BadRequestException("فرمت ایمیل نامعتبر است!");
            if (!request.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail()))
                    throw new BadRequestException("ایمیل تکراری است!");
                user.setEmail(request.getEmail());
            }
        }

        return convertToResponse(userRepository.save(user));
    }

    public UserResponse changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new BadRequestException("رمز عبور فعلی اشتباه است!");

        // FIX: استفاده از validatePassword برای بررسی حداقل طول
        validatePassword(newPassword);

        if (passwordEncoder.matches(newPassword, user.getPassword()))
            throw new BadRequestException("رمز عبور جدید نمی‌تواند با رمز قبلی یکسان باشد!");

        user.setPassword(passwordEncoder.encode(newPassword));
        return convertToResponse(userRepository.save(user));
    }

    public UserResponse updateProfileImage(Long userId, MultipartFile image) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));

        if (image == null || image.isEmpty())
            throw new BadRequestException("فایل تصویر ارسال نشده است!");

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            throw new BadRequestException("فایل ارسال شده تصویر نیست!");

        if (image.getSize() > 5L * 1024 * 1024)
            throw new BadRequestException("حجم تصویر نباید بیشتر از ۵ مگابایت باشد!");

        try {
            Path uploadPath = Paths.get("uploads/");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String originalFileName = image.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains("."))
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));

            String fileName = "profile_" + userId + "_" + System.currentTimeMillis() + extension;
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, image.getBytes());

            if (user.getProfileImagePath() != null && !user.getProfileImagePath().isBlank()) {
                try {
                    Path oldPath = Paths.get(user.getProfileImagePath());
                    if (Files.exists(oldPath)) Files.delete(oldPath);
                } catch (IOException ignored) {}
            }

            user.setProfileImagePath(filePath.toString().replace("\\", "/"));
        } catch (IOException e) {
            throw new BadRequestException("خطا در ذخیره تصویر پروفایل: " + e.getMessage());
        }

        return convertToResponse(userRepository.save(user));
    }

    public boolean isAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        return user.getRole() == Role.ADMIN;
    }
}