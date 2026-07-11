package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByStatus(String status);
    List<Item> findByUserId(Long userId);
    List<Item> findByCategoryIdAndStatus(Long categoryId, String status);
    List<Item> findByUserIdAndStatusNot(Long userId, String status);

    List<Item> findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
            String status1, String titleKeyword, String status2, String descKeyword
    );

    List<Item> findByStatusAndCityId(String status, Long cityId);

    //جستجوی پیشرفته
    @Query("SELECT i FROM Item i WHERE i.status = 'APPROVED' " +
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
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );
}