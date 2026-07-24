package com.secondhand.backend.repository;

import com.secondhand.backend.constant.ItemStatus;
import com.secondhand.backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data JPA repository for {@code Item} entities.
 * <p>
 * This interface performs read and write operations on the database via Spring Data JPA; method implementations are generated at runtime from the method name or the {@code @Query} annotation.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // ✅ فقط ۱ پارامتر
    /**
     * Finds by status.
     *
     * @param status the status value
     * @return a {@code List<Item>} with the results; empty if nothing matches
     */
    List<Item> findByStatus(ItemStatus status);

    /**
     * Finds by user id.
     *
     * @param userId id of the user
     * @return a {@code List<Item>} with the results; empty if nothing matches
     */
    List<Item> findByUserId(Long userId);

    // ✅ فقط ۲ پارامتر
    /**
     * Finds by category id and status.
     *
     * @param categoryId id of the category
     * @param status the status value
     * @return a {@code List<Item>} with the results; empty if nothing matches
     */
    List<Item> findByCategoryIdAndStatus(Long categoryId, ItemStatus status);

    /**
     * Finds by user id and status not.
     *
     * @param userId id of the user
     * @param status the status value
     * @return a {@code List<Item>} with the results; empty if nothing matches
     */
    List<Item> findByUserIdAndStatusNot(Long userId, ItemStatus status);

    // ✅ جستجو با ۱ پارامتر + keyword
    /**
     * Finds by status and title containing ignore case or status and description containing ignore case.
     *
     * @param status the status value
     * @param keyword the search keyword
     * @return a {@code List<Item>} with the results; empty if nothing matches
     */
    @Query("SELECT i FROM Item i WHERE i.status = :status AND (LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Item> findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
            @Param("status") ItemStatus status,
            @Param("keyword") String keyword
    );

    // ✅ فقط ۲ پارامتر - اصلاح شد
    /**
     * Finds by status and city id.
     *
     * @param status the status value
     * @param cityId id of the city
     * @return a {@code List<Item>} with the results; empty if nothing matches
     */
    List<Item> findByStatusAndCityId(ItemStatus status, Long cityId);

    // ✅ جستجوی پیشرفته با ۵ پارامتر
    @Query("SELECT i FROM Item i WHERE i.status = com.secondhand.backend.constant.ItemStatus.APPROVED " +
            "AND (:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR i.category.id = :categoryId) " +
            "AND (:cityId IS NULL OR i.city.id = :cityId) " +
            "AND (:minPrice IS NULL OR i.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR i.price <= :maxPrice)")
    /**
     * Searches advanced.
     *
     * @param keyword the search keyword
     * @param categoryId id of the category
     * @param cityId id of the city
     * @param minPrice minimum price bound
     * @param maxPrice maximum price bound
     * @return a {@code List<Item>} with the results; empty if nothing matches
     */
    List<Item> searchAdvanced(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("cityId") Long cityId,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice
    );

    // ✅ برای خرید
    /**
     * Finds by buyer id.
     *
     * @param buyerId id of the buyer
     * @return a {@code List<Item>} with the results; empty if nothing matches
     */
    List<Item> findByBuyerId(Long buyerId);
}