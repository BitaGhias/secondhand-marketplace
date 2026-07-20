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

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    private CategoryResponse convertToResponse(Category category) {
        return convertToResponseWithCount(category, 0L);
    }

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
    private Map<Long, Long> buildCountMap() {
        List<Object[]> results = categoryRepository.countItemsByCategory();
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : results) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }

    //  ساخت map از parentId -> hasChildren با یک query (تمام categories)
    private Map<Long, Boolean> buildHasChildrenMap(List<Category> categories) {
        Map<Long, Boolean> map = new HashMap<>();
        for (Category c : categories) {
            if (c.getParent() != null) {
                map.put(c.getParent().getId(), true);
            }
        }
        return map;
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> responses = new ArrayList<>();
        for (Category category : categories) {
            responses.add(convertToResponse(category));
        }
        return responses;
    }

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

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("دسته‌بندی یافت نشد"));
        Long count = categoryRepository.countApprovedItemsByCategoryId(id);
        return convertToResponseWithCount(category, count);
    }

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
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updated = categoryRepository.save(category);
        Long count = categoryRepository.countApprovedItemsByCategoryId(updated.getId());
        return convertToResponseWithCount(updated, count);
    }

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