package com.secondhand.backend.repository;
//کارمند دیتابیس و پل ارتباطی ذیتابیس و بک اند
//واسطه ای هست که باعث میشه کد های SQL رو ننویسی
import com.secondhand.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;//ابزار اصلی DB
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>
{
    //نوع Entity که باهاش کار میکنه : User و نوع Id : Long
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    // FIX: بررسی تکراری بودن نام کاربری بدون توجه به بزرگی/کوچکی حروف (Ali == ali)
    boolean existsByUsernameIgnoreCase(String username);

    Optional<User> findByUsername(String username);//برای خطا ندادن برنامه در صورت پیدا نکردن
    // FIX: پیدا کردن کاربر بدون توجه به بزرگی/کوچکی حروف برای لاگین یکسان با هر حالتی از حروف
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
}