package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@code Rating} entities.
 * <p>
 * This interface performs read and write operations on the database via Spring Data JPA; method implementations are generated at runtime from the method name or the {@code @Query} annotation.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Finds by seller id.
     *
     * @param sellerId id of the seller
     * @return a {@code List<Rating>} with the results; empty if nothing matches
     */
    List<Rating> findBySellerId(Long sellerId);

    //بررسی امتیاز تکراری
    /**
     * Finds by rater id and item id.
     *
     * @param raterId the "rater id" value of type {@code Long}
     * @param itemId id of the ad (item)
     * @return an {@code Optional<Rating>} that may be empty
     */
    Optional<Rating> findByRaterIdAndItemId(Long raterId, Long itemId);

    //  COUNT در سطح دیتابیس به جای بارگذاری همه رکوردها
    /**
     * Counts by seller id.
     *
     * @param sellerId id of the seller
     * @return the resulting numeric value
     */
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.seller.id = :sellerId")
    long countBySellerId(@Param("sellerId") Long sellerId);

    //  AVG در سطح دیتابیس به جای محاسبه در جاوا
    /**
     * Performs the "average score by seller id" operation.
     *
     * @param sellerId id of the seller
     * @return the resulting numeric value
     */
    @Query("SELECT COALESCE(AVG(r.score), 0.0) FROM Rating r WHERE r.seller.id = :sellerId")
    double averageScoreBySellerId(@Param("sellerId") Long sellerId);

    // میانگین امتیاز همه فروشنده‌ها در یک کوئری (برای مرتب‌سازی لیست آگهی‌ها)
    /**
     * Grouped query returning the average rating per seller; each result row contains the seller id and the average score.
     *
     * @return a {@code List<Object[]>} with the results; empty if nothing matches
     */
    @Query("SELECT r.seller.id, AVG(r.score) FROM Rating r GROUP BY r.seller.id")
    List<Object[]> averageScoreGroupedBySeller();
}