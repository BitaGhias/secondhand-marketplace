package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@code Category} entities.
 * <p>
 * This interface performs read and write operations on the database via Spring Data JPA; method implementations are generated at runtime from the method name or the {@code @Query} annotation.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds by name.
     *
     * @param name the name
     * @return an {@code Optional<Category>} that may be empty
     */
    Optional<Category> findByName(String name);

    // پیدا کردن دسته‌بندی‌های ریشه (parent = null)
    /**
     * Finds by parent is null.
     *
     * @return a {@code List<Category>} with the results; empty if nothing matches
     */
    List<Category> findByParentIsNull();

    // پیدا کردن زیردسته‌های یک دسته‌بندی
    /**
     * Finds by parent id.
     *
     * @param parentId the "parent id" value of type {@code Long}
     * @return a {@code List<Category>} with the results; empty if nothing matches
     */
    List<Category> findByParentId(Long parentId);

    // پیدا کردن دسته‌بندی با نام (به جز خودش) برای بررسی تکراری نبودن در ویرایش
    /**
     * Finds by name and id not.
     *
     * @param name the name
     * @param id unique identifier of the record
     * @return an {@code Optional<Category>} that may be empty
     */
    Optional<Category> findByNameAndIdNot(String name, Long id);

    // پیدا کردن تعداد آگهی‌های فعال هر دسته‌بندی
    /**
     * Counts items by category.
     *
     * @return a {@code List<Object[]>} with the results; empty if nothing matches
     */
    @Query("SELECT c.id, COUNT(i) FROM Category c LEFT JOIN Item i ON i.category = c AND i.status = com.secondhand.backend.constant.ItemStatus.APPROVED GROUP BY c.id")
    List<Object[]> countItemsByCategory();

    // پیدا کردن تعداد آگهی‌های یک دسته‌بندی خاص
    /**
     * Counts approved items by category id.
     *
     * @param categoryId id of the category
     * @return the resulting numeric value
     */
    @Query("SELECT COUNT(i) FROM Item i WHERE i.category.id = :categoryId AND i.status = com.secondhand.backend.constant.ItemStatus.APPROVED")
    Long countApprovedItemsByCategoryId(@Param("categoryId") Long categoryId);
}