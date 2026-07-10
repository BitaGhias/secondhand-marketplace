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
    Optional<User> findByUsername(String username);//برای خطا ندادن برنامه در صورت پیدا نکردن
    Optional<User> findByEmail(String email);
}