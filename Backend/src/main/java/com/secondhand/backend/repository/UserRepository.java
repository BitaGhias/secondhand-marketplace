package com.secondhand.backend.repository;

import com.secondhand.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>
{
    // برای ثبت‌نام (چک کردن تکراری نبودن یوزر)
    boolean existsByUsername(String username);

    // برای لاگین (بیرون کشیدن پرونده کاربر)
    Optional<User> findByUsername(String username);
}
