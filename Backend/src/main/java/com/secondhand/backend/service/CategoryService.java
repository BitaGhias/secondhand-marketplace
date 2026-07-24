package com.secondhand.backend.service;

import com.secondhand.backend.dto.category.CategoryRequest;
import com.secondhand.backend.dto.category.CategoryResponse;
import com.secondhand.backend.entity.Category;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business-logic service for "category" operations.
 * <p>
 * This class implements the core business logic and sits between the controller layer and the repository layer. Validation and access control are enforced here and a proper exception is thrown when a rule is violated.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Converts to response.
     *
     * @param category the category object
     * @return the resulting {@code CategoryResponse} instance
     */
    private CategoryResponse convertToResponse(Category category) {
        return convertToResponseWithCount(category, 0L);
    }

    /**
     * Converts to response with count.
     *
     * @param category the category object
     * @param itemCount the "item count" value of type {@code Long}
     * @return the resulting {@code CategoryResponse} instance
     */
    private CategoryResponse convertToResponseWithCount(Category category, Long itemCount) {
        Long parentId = category.getParent() != null ? category.getParent().getId() : null;
        String parentName = category.getParent() != null ? category.getParent().getName() : null;
        boolean hasChildren = !categoryRepository.findByParentId(category.getId()).isEmpty();

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                parentId,
                parentName,
                itemCount,
                hasChildren
        );
    }

    /**
     * Creates category.
     *
     * @param request request body received from the client
     * @return the resulting {@code CategoryResponse} instance
     */
    public CategoryResponse createCategory(CategoryRequest request) {
        categoryRepository.findByName(request.getName())
                .ifPresent(c -> {
                    throw new BadRequestException("این دسته‌بندی از قبل وجود دارد");
                });

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("دسته‌بندی والد یافت نشد"));
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setParent(parent);

        Category saved = categoryRepository.save(category);
        return convertToResponse(saved);
    }


    /**
     * Gets all categories with count.
     *
     * @return a {@code List<CategoryResponse>} with the results; empty if nothing matches
     */
    public List<CategoryResponse> getAllCategoriesWithCount() {
        List<Category> categories = categoryRepository.findAll();

        // یک query برای همه count‌ها
        Map<Long, Long> countMap = buildCountMap();
        // یک query برای بررسی children
        Map<Long, Boolean> hasChildrenMap = buildHasChildrenMap(categories);

        List<CategoryResponse> responses = new ArrayList<>();
        for (Category category : categories) {
            Long count = countMap.getOrDefault(category.getId(), 0L);
            boolean hasChildren = hasChildrenMap.getOrDefault(category.getId(), false);

            Long parentId = category.getParent() != null ? category.getParent().getId() : null;
            String parentName = category.getParent() != null ? category.getParent().getName() : null;

            responses.add(new CategoryResponse(
                    category.getId(),
                    category.getName(),
                    parentId,
                    parentName,
                    count,
                    hasChildren
            ));
        }
        return responses;
    }

    //  ساخت map از categoryId -> itemCount با یک query
    /**
     * Builds count map.
     *
     * @return the resulting {@code Map<Long, Long>} instance
     */
    private Map<Long, Long> buildCountMap() {
        List<Object[]> results = categoryRepository.countItemsByCategory();
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : results) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }

    //  ساخت map از parentId -> hasChildren با یک query (تمام categories)
    /**
     * Builds has children map.
     *
     * @param categories the "categories" value of type {@code List<Category>}
     * @return the resulting {@code Map<Long, Boolean>} instance
     */
    private Map<Long, Boolean> buildHasChildrenMap(List<Category> categories) {
        Map<Long, Boolean> map = new HashMap<>();
        for (Category c : categories) {
            if (c.getParent() != null) {
                map.put(c.getParent().getId(), true);
            }
        }
        return map;
    }

    /**
     * Gets all categories.
     *
     * @return a {@code List<CategoryResponse>} with the results; empty if nothing matches
     */
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> responses = new ArrayList<>();
        for (Category category : categories) {
            responses.add(convertToResponse(category));
        }
        return responses;
    }

    /**
     * Gets root categories.
     *
     * @return a {@code List<CategoryResponse>} with the results; empty if nothing matches
     */
    public List<CategoryResponse> getRootCategories() {
        List<Category> roots = categoryRepository.findByParentIsNull();
        Map<Long, Long> countMap = buildCountMap();

        List<CategoryResponse> responses = new ArrayList<>();
        for (Category category : roots) {
            Long count = countMap.getOrDefault(category.getId(), 0L);
            responses.add(new CategoryResponse(
                    category.getId(),
                    category.getName(),
                    null,
                    null,
                    count,
                    !categoryRepository.findByParentId(category.getId()).isEmpty()
            ));
        }
        return responses;
    }

    /**
     * Gets subcategories.
     *
     * @param parentId the "parent id" value of type {@code Long}
     * @return a {@code List<CategoryResponse>} with the results; empty if nothing matches
     */
    public List<CategoryResponse> getSubcategories(Long parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("دسته‌بندی والد یافت نشد");
        }

        List<Category> subcategories = categoryRepository.findByParentId(parentId);
        Map<Long, Long> countMap = buildCountMap();

        List<CategoryResponse> responses = new ArrayList<>();
        for (Category category : subcategories) {
            Long count = countMap.getOrDefault(category.getId(), 0L);
            boolean hasChildren = !categoryRepository.findByParentId(category.getId()).isEmpty();
            Long pId = category.getParent() != null ? category.getParent().getId() : null;
            String pName = category.getParent() != null ? category.getParent().getName() : null;
            responses.add(new CategoryResponse(category.getId(), category.getName(), pId, pName, count, hasChildren));
        }
        return responses;
    }

    /**
     * Gets category by id.
     *
     * @param id unique identifier of the record
     * @return the resulting {@code CategoryResponse} instance
     */
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("دسته‌بندی یافت نشد"));
        Long count = categoryRepository.countApprovedItemsByCategoryId(id);
        return convertToResponseWithCount(category, count);
    }

    /**
     * Updates category.
     *
     * @param id unique identifier of the record
     * @param request request body received from the client
     * @return the resulting {@code CategoryResponse} instance
     */
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("دسته‌بندی یافت نشد"));

        categoryRepository.findByNameAndIdNot(request.getName(), id)
                .ifPresent(c -> {
                    throw new BadRequestException("این نام دسته‌بندی قبلاً استفاده شده است");
                });

        category.setName(request.getName());

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("یک دسته‌بندی نمی‌تواند والد خودش باشد!");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("دسته‌بندی والد یافت نشد"));
            // جلوگیری از حلقه در درخت دسته‌بندی: زنجیره اجداد والد جدید تا ریشه بررسی می‌شود؛
            // اگر خود این دسته در زنجیره باشد، والد انتخابی زیرمجموعه خودش است و حلقه ایجاد می‌شود.
            Category ancestor = parent;
            while (ancestor != null) {
                if (ancestor.getId().equals(id)) {
                    throw new BadRequestException("والد انتخاب‌شده یکی از زیردسته‌های همین دسته‌بندی است و باعث حلقه در درخت دسته‌بندی می‌شود!");
                }
                ancestor = ancestor.getParent();
            }
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updated = categoryRepository.save(category);
        Long count = categoryRepository.countApprovedItemsByCategoryId(updated.getId());
        return convertToResponseWithCount(updated, count);
    }

    /**
     * Deletes category.
     *
     * @param id unique identifier of the record
     */
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("دسته‌بندی یافت نشد"));

        List<Category> subcategories = categoryRepository.findByParentId(id);
        if (!subcategories.isEmpty()) {
            throw new BadRequestException("این دسته‌بندی دارای زیردسته است. ابتدا زیردسته‌ها را حذف کنید!");
        }

        Long itemCount = categoryRepository.countApprovedItemsByCategoryId(id);
        if (itemCount > 0) {
            throw new BadRequestException("این دسته‌بندی دارای " + itemCount + " آگهی فعال است. ابتدا آگهی‌ها را حذف یا تغییر دسته‌بندی دهید!");
        }

        categoryRepository.delete(category);
    }

    /**
     * Gets popular categories.
     *
     * @param limit the "limit" value of type {@code int}
     * @return a {@code List<CategoryResponse>} with the results; empty if nothing matches
     */
    public List<CategoryResponse> getPopularCategories(int limit) {
        List<Object[]> results = categoryRepository.countItemsByCategory();
        results.sort((a, b) -> Long.compare((Long) b[1], (Long) a[1]));

        List<CategoryResponse> responses = new ArrayList<>();
        int count = 0;
        for (Object[] result : results) {
            if (count >= limit) break;
            Long categoryId = (Long) result[0];
            Long itemCount = (Long) result[1];
            categoryRepository.findById(categoryId).ifPresent(category ->
                    responses.add(convertToResponseWithCount(category, itemCount))
            );
            count++;
        }
        return responses;
    }
}