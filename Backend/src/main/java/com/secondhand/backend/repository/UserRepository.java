package com.secondhand.backend.repository;
//کارمند دیتابیس
import com.secondhand.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;//ابزار اصلی DB
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>
{
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username); //برای خطا ندادن برنامه در صورت پیدا نکردن
}
