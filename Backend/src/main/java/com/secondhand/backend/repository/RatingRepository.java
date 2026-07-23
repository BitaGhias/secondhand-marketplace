package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findBySellerId(Long sellerId);

    //بررسی امتیاز تکراری
    Optional<Rating> findByRaterIdAndItemId(Long raterId, Long itemId);

    //  COUNT در سطح دیتابیس به جای بارگذاری همه رکوردها
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.seller.id = :sellerId")
    long countBySellerId(@Param("sellerId") Long sellerId);

    //  AVG در سطح دیتابیس به جای محاسبه در جاوا
    @Query("SELECT COALESCE(AVG(r.score), 0.0) FROM Rating r WHERE r.seller.id = :sellerId")
    double averageScoreBySellerId(@Param("sellerId") Long sellerId);

    // میانگین امتیاز همه فروشنده‌ها در یک کوئری (برای مرتب‌سازی لیست آگهی‌ها)
    @Query("SELECT r.seller.id, AVG(r.score) FROM Rating r GROUP BY r.seller.id")
    List<Object[]> averageScoreGroupedBySeller();
}