package com.secondhand.backend.repository;

import com.secondhand.backend.constant.ItemStatus;
import com.secondhand.backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // ✅ فقط ۱ پارامتر
    List<Item> findByStatus(ItemStatus status);

    List<Item> findByUserId(Long userId);

    // ✅ فقط ۲ پارامتر
    List<Item> findByCategoryIdAndStatus(Long categoryId, ItemStatus status);

    List<Item> findByUserIdAndStatusNot(Long userId, ItemStatus status);

    // ✅ جستجو با ۱ پارامتر + keyword
    @Query("SELECT i FROM Item i WHERE i.status = :status AND (LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Item> findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
            @Param("status") ItemStatus status,
            @Param("keyword") String keyword
    );

    // ✅ فقط ۲ پارامتر - اصلاح شد
    List<Item> findByStatusAndCityId(ItemStatus status, Long cityId);

    // ✅ جستجوی پیشرفته با ۵ پارامتر
    @Query("SELECT i FROM Item i WHERE i.status = com.secondhand.backend.constant.ItemStatus.APPROVED " +
            "AND (:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR i.category.id = :categoryId) " +
            "AND (:cityId IS NULL OR i.city.id = :cityId) " +
            "AND (:minPrice IS NULL OR i.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR i.price <= :maxPrice)")
    List<Item> searchAdvanced(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("cityId") Long cityId,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice
    );

    // ✅ برای خرید
    List<Item> findByBuyerId(Long buyerId);
}