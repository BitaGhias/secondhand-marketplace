package com.secondhand.backend.repository;
//کارمند دیتابیس و پل ارتباطی ذیتابیس و بک اند
//واسطه ای هست که باعث میشه کد های SQL رو ننویسی
import com.secondhand.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;//ابزار اصلی DB
import java.util.Optional;

/**
 * Spring Data JPA repository for {@code User} entities.
 * <p>
 * This interface performs read and write operations on the database via Spring Data JPA; method implementations are generated at runtime from the method name or the {@code @Query} annotation.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public interface UserRepository extends JpaRepository<User,Long>
{
    //نوع Entity که باهاش کار میکنه : User و نوع Id : Long
    /**
     * Performs the "exists by username" operation.
     *
     * @param username the username
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    boolean existsByUsername(String username);
    /**
     * Performs the "exists by email" operation.
     *
     * @param email the email address
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    boolean existsByEmail(String email);
    /**
     * Performs the "exists by phone number" operation.
     *
     * @param phoneNumber the phone number
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    boolean existsByPhoneNumber(String phoneNumber);

    // FIX: بررسی تکراری بودن نام کاربری بدون توجه به بزرگی/کوچکی حروف (Ali == ali)
    /**
     * Performs the "exists by username ignore case" operation.
     *
     * @param username the username
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Finds by username.
     *
     * @param username the username
     * @return an {@code Optional<User>} that may be empty
     */
    Optional<User> findByUsername(String username);//برای خطا ندادن برنامه در صورت پیدا نکردن
    // FIX: پیدا کردن کاربر بدون توجه به بزرگی/کوچکی حروف برای لاگین یکسان با هر حالتی از حروف
    Optional<User> findByUsernameIgnoreCase(String username);
    /**
     * Finds by email.
     *
     * @param email the email address
     * @return an {@code Optional<User>} that may be empty
     */
    Optional<User> findByEmail(String email);
    /**
     * Finds by phone number.
     *
     * @param phoneNumber the phone number
     * @return an {@code Optional<User>} that may be empty
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
}