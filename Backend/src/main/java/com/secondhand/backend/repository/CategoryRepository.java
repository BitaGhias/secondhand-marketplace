package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    // پیدا کردن دسته‌بندی‌های ریشه (parent = null)
    List<Category> findByParentIsNull();

    // پیدا کردن زیردسته‌های یک دسته‌بندی
    List<Category> findByParentId(Long parentId);

    // پیدا کردن دسته‌بندی با نام (به جز خودش) برای بررسی تکراری نبودن در ویرایش
    Optional<Category> findByNameAndIdNot(String name, Long id);

    // پیدا کردن تعداد آگهی‌های فعال هر دسته‌بندی
    @Query("SELECT c.id, COUNT(i) FROM Category c LEFT JOIN Item i ON i.category = c AND i.status = com.secondhand.backend.constant.ItemStatus.APPROVED GROUP BY c.id")
    List<Object[]> countItemsByCategory();

    // پیدا کردن تعداد آگهی‌های یک دسته‌بندی خاص
    @Query("SELECT COUNT(i) FROM Item i WHERE i.category.id = :categoryId AND i.status = com.secondhand.backend.constant.ItemStatus.APPROVED")
    Long countApprovedItemsByCategoryId(@Param("categoryId") Long categoryId);
}