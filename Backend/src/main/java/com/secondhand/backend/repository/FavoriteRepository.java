package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // پیدا کردن تمام علاقه‌مندی‌های یک کاربر خاص
    List<Favorite> findByUserId(Long userId);

    //  پیدا کردن یک مورد خاص بر اساس آیدی کاربر و آیدی آگهی (برای حذف یا چک کردن تکراری نبودن)
    Optional<Favorite> findByUserIdAndItemId(Long userId, Long itemId);
}
