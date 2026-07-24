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
import java.util.Locale;
import java.util.UUID;

/**
 * Business-logic service for "user" operations.
 * <p>
 * This class implements the core business logic and sits between the controller layer and the repository layer. Validation and access control are enforced here and a proper exception is thrown when a rule is violated.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Checks whether the "valid email" condition holds.
     *
     * @param email the email address
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Checks whether the "valid phone number" condition holds.
     *
     * @param phone the "phone" value of type {@code String}
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null) return false;
        String phoneRegex = "^09[0-9]{9}$";
        return phone.matches(phoneRegex);
    }

    // FIX: تبدیل ارقام فارسی/عربی به انگلیسی (مثلاً شماره تلفن با کیبورد فارسی تایپ‌شده)
    /**
     * Performs the "normalize digits" operation.
     *
     * @param input the "input" value of type {@code String}
     * @return the resulting string
     */
    private String normalizeDigits(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= '۰' && c <= '۹') { // ارقام فارسی
                sb.append((char) (c - '۰' + '0'));
            } else if (c >= '٠' && c <= '٩') { // ارقام عربی
                sb.append((char) (c - '٠' + '0'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // FIX: بررسی فرمت نام کامل (فقط حروف و فاصله، بین ۳ تا ۵۰ کاراکتر)
    /**
     * Checks whether the "valid full name" condition holds.
     *
     * @param fullName the "full name" value of type {@code String}
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    private boolean isValidFullName(String fullName) {
        if (fullName == null) return false;
        return fullName.trim().matches("^[\\p{L} ]{3,50}$");
    }

    // FIX: بررسی فرمت نام کاربری (فقط حروف انگلیسی، عدد و _ ، بین ۳ تا ۲۰ کاراکتر)
    /**
     * Checks whether the "valid username" condition holds.
     *
     * @param username the username
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    private boolean isValidUsername(String username) {
        if (username == null) return false;
        return username.trim().matches("^[A-Za-z0-9_]{3,20}$");
    }

    // FIX: بررسی حداقل/حداکثر طول رمز عبور و عدم وجود فاصله
    /**
     * Validates password.
     *
     * @param password the password
     */
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty())
            throw new BadRequestException("رمز عبور الزامی است!");
        if (password.length() < 6)
            throw new BadRequestException("رمز عبور باید حداقل ۶ کاراکتر باشد!");
        if (password.length() > 100)
            throw new BadRequestException("رمز عبور نباید بیشتر از ۱۰۰ کاراکتر باشد!");
        if (password.contains(" "))
            throw new BadRequestException("رمز عبور نباید شامل فاصله باشد!");
    }

    /**
     * Converts to response.
     *
     * @param user the user object
     * @return the resulting {@code UserResponse} instance
     */
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

    /**
     * Registers user.
     *
     * @param request request body received from the client
     * @return the resulting {@code UserResponse} instance
     */
    public UserResponse registerUser(UserRegisterRequest request) {
        if (request.getFullName() == null || request.getFullName().trim().isEmpty())
            throw new BadRequestException("نام کامل الزامی است!");

        if (!isValidFullName(request.getFullName()))
            throw new BadRequestException("نام کامل باید بین ۳ تا ۵۰ حرف باشد و فقط شامل حروف باشد!");

        if (request.getUsername() == null || request.getUsername().trim().isEmpty())
            throw new BadRequestException("نام کاربری الزامی است!");

        if (!isValidUsername(request.getUsername()))
            throw new BadRequestException("نام کاربری باید بین ۳ تا ۲۰ کاراکتر باشد و فقط شامل حروف انگلیسی، عدد و _ باشد!");

        String normalizedUsername = request.getUsername().trim().toLowerCase();

        // FIX: بررسی تکراری بودن نام کاربری بدون توجه به بزرگی/کوچکی حروف
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername().trim()))
            throw new BadRequestException("نام کاربری تکراری است!");

        // FIX: استفاده از validatePassword به جای بررسی null ساده
        validatePassword(request.getPassword());

        if (request.getConfirmPassword() == null || !request.getPassword().equals(request.getConfirmPassword()))
            throw new BadRequestException("رمز عبور و تکرار آن مطابقت ندارند!");

        if (request.getEmail() == null || request.getEmail().trim().isEmpty())
            throw new BadRequestException("ایمیل الزامی است!");

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (!isValidEmail(normalizedEmail))
            throw new BadRequestException("فرمت ایمیل نامعتبر است!");

        if (userRepository.existsByEmail(normalizedEmail))
            throw new BadRequestException("ایمیل تکراری است!");

        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty())
            throw new BadRequestException("شماره تلفن الزامی است!");

        // FIX: تبدیل ارقام فارسی/عربی احتمالی شماره تلفن به انگلیسی قبل از بررسی فرمت
        String normalizedPhone = normalizeDigits(request.getPhoneNumber().trim());

        if (!isValidPhoneNumber(normalizedPhone))
            throw new BadRequestException("فرمت شماره تلفن نامعتبر است! باید با 09 شروع شود و 11 رقم باشد.");

        if (userRepository.existsByPhoneNumber(normalizedPhone))
            throw new BadRequestException("شماره تلفن تکراری است!");

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setUsername(normalizedUsername); // به‌جای request.getUsername().trim()
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(normalizedPhone);
        user.setEmail(normalizedEmail);
        user.setRole(Role.USER);
        user.setBlocked(false);
        user.setActive(true);

        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    /**
     * Logs in user.
     *
     * @param username the username
     * @param password the password
     * @return the resulting {@code UserResponse} instance
     */
    public UserResponse loginUser(String username, String password) {
        if (username == null || username.trim().isEmpty())
            throw new BadRequestException("نام کاربری الزامی است!");

        if (password == null || password.trim().isEmpty())
            throw new BadRequestException("رمز عبور الزامی است!");

        // FIX: حذف فاصله‌های اضافی احتمالی قبل از جستجو (مثلاً درخواست مستقیم به API بدون فرانت)
        String normalizedUsername = username.trim();

        // FIX: پیدا کردن کاربر بدون توجه به بزرگی/کوچکی حروف نام کاربری
        User user = userRepository.findByUsernameIgnoreCase(normalizedUsername)
                .orElseThrow(() -> new UnauthorizedException("نام کاربری یا رمز عبور اشتباه است"));

        if (!user.isActive())
            throw new ForbiddenException("حساب کاربری شما فعال نیست!");

        if (user.isBlocked())
            throw new ForbiddenException("حساب کاربری شما توسط ادمین مسدود شده است!");

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new UnauthorizedException("نام کاربری یا رمز عبور اشتباه است");

        return convertToResponse(user);
    }

    /**
     * Gets user by id.
     *
     * @param userId id of the user
     * @return the resulting {@code UserResponse} instance
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        return convertToResponse(user);
    }

    /**
     * Gets user by username.
     *
     * @param username the username
     * @return the resulting {@code UserResponse} instance
     */
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        return convertToResponse(user);
    }

    /**
     * Gets user id by username.
     *
     * @param username the username
     * @return the resulting numeric value
     */
    public Long getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        return user.getId();
    }

    /**
     * Gets all users.
     *
     * @param requesterId the "requester id" value of type {@code Long}
     * @return a {@code List<UserResponse>} with the results; empty if nothing matches
     */
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

    /**
     * Toggles user block status.
     *
     * @param adminId the "admin id" value of type {@code Long}
     * @param userId id of the user
     * @param block the "block" value of type {@code boolean}
     * @return the resulting {@code UserResponse} instance
     */
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

    /**
     * Performs the "make admin" operation.
     *
     * @param adminId the "admin id" value of type {@code Long}
     * @param userId id of the user
     * @return the resulting {@code UserResponse} instance
     */
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

    /**
     * Updates user profile.
     *
     * @param userId id of the user
     * @param request request body received from the client
     * @return the resulting {@code UserResponse} instance
     */
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

    /**
     * Changes password.
     *
     * @param userId id of the user
     * @param oldPassword the "old password" value of type {@code String}
     * @param newPassword the "new password" value of type {@code String}
     * @return the resulting {@code UserResponse} instance
     */
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

    /**
     * Updates profile image.
     *
     * @param userId id of the user
     * @param image the image
     * @return the resulting {@code UserResponse} instance
     */
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

        Path oldPath = user.getProfileImagePath() != null && !user.getProfileImagePath().isBlank()
                ? Paths.get(user.getProfileImagePath())
                : null;
        Path newPath = null;

        try {
            Path uploadPath = Paths.get("uploads/");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String originalFileName = image.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains("."))
                extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase(Locale.ROOT);

            if (!List.of(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp").contains(extension))
                throw new BadRequestException("فرمت فایل تصویر مجاز نیست!");

            // UUID avoids overwriting a profile image during rapid successive uploads.
            String fileName = "profile_" + userId + "_" + UUID.randomUUID() + extension;
            newPath = uploadPath.resolve(fileName);
            Files.write(newPath, image.getBytes());

            user.setProfileImagePath(newPath.toString().replace("\\", "/"));
            UserResponse response = convertToResponse(userRepository.save(user));

            // Delete the old file only after the database points to the new one.
            if (oldPath != null) {
                try {
                    Files.deleteIfExists(oldPath);
                } catch (IOException ignored) {
                    // The new profile is already saved; an old-file cleanup failure is non-fatal.
                }
            }
            return response;
        } catch (IOException | RuntimeException e) {
            if (newPath != null) {
                try {
                    Files.deleteIfExists(newPath);
                } catch (IOException cleanupError) {
                    // Keep the original failure; cleanup failure is logged below.
                }
            }
            if (e instanceof BadRequestException badRequest) throw badRequest;
            throw new BadRequestException("خطا در ذخیره تصویر پروفایل: " + e.getMessage());
        }
    }

    /**
     * Checks whether the "admin" condition holds.
     *
     * @param userId id of the user
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean isAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        return user.getRole() == Role.ADMIN;
    }
}