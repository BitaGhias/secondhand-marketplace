package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@code Favorite} entities.
 * <p>
 * This interface performs read and write operations on the database via Spring Data JPA; method implementations are generated at runtime from the method name or the {@code @Query} annotation.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // پیدا کردن تمام علاقه‌مندی‌های یک کاربر خاص
    /**
     * Finds by user id.
     *
     * @param userId id of the user
     * @return a {@code List<Favorite>} with the results; empty if nothing matches
     */
    List<Favorite> findByUserId(Long userId);

    //  پیدا کردن یک مورد خاص بر اساس آیدی کاربر و آیدی آگهی (برای حذف یا چک کردن تکراری نبودن)
    /**
     * Finds by user id and item id.
     *
     * @param userId id of the user
     * @param itemId id of the ad (item)
     * @return an {@code Optional<Favorite>} that may be empty
     */
    Optional<Favorite> findByUserIdAndItemId(Long userId, Long itemId);
}
