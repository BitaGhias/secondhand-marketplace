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

    Optional<Rating> findByRaterIdAndItemId(Long raterId, Long itemId);

    // FIX: COUNT در سطح دیتابیس به جای بارگذاری همه رکوردها
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.seller.id = :sellerId")
    long countBySellerId(@Param("sellerId") Long sellerId);

    // FIX: AVG در سطح دیتابیس به جای محاسبه در جاوا
    @Query("SELECT COALESCE(AVG(r.score), 0.0) FROM Rating r WHERE r.seller.id = :sellerId")
    double averageScoreBySellerId(@Param("sellerId") Long sellerId);
}