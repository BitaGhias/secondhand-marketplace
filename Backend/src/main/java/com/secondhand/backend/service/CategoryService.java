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
import java.util.List;

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
        boolean hasChildren = categoryRepository.findByParentId(category.getId()).size() > 0;

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

    // دریافت همه دسته‌بندی‌ها با تعداد آگهی
    public List<CategoryResponse> getAllCategoriesWithCount() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> responses = new ArrayList<>();

        for (Category category : categories) {
            Long count = categoryRepository.countApprovedItemsByCategoryId(category.getId());
            responses.add(convertToResponseWithCount(category, count));
        }

        return responses;
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> responses = new ArrayList<>();
        for (Category category : categories) {
            responses.add(convertToResponse(category));
        }
        return responses;
    }

    //  دریافت دسته‌بندی‌های پدر
    public List<CategoryResponse> getRootCategories() {
        List<Category> roots = categoryRepository.findByParentIsNull();
        List<CategoryResponse> responses = new ArrayList<>();
        for (Category category : roots) {
            Long count = categoryRepository.countApprovedItemsByCategoryId(category.getId());
            responses.add(convertToResponseWithCount(category, count));
        }
        return responses;
    }

    //  دریافت زیردسته‌های یک دسته‌بندی
    public List<CategoryResponse> getSubcategories(Long parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("دسته‌بندی والد یافت نشد");
        }

        List<Category> subcategories = categoryRepository.findByParentId(parentId);
        List<CategoryResponse> responses = new ArrayList<>();
        for (Category category : subcategories) {
            Long count = categoryRepository.countApprovedItemsByCategoryId(category.getId());
            responses.add(convertToResponseWithCount(category, count));
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
            // جلوگیری از اینکه دسته‌بندی والد خودش باشه
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("یک دسته‌بندی نمی‌تواند والد خودش باشد!");
            }

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("دسته‌بندی والد یافت نشد"));
            category.setParent(parent);
        } else {
            category.setParent(null);  // تبدیل به دسته‌بندی ریشه
        }

        Category updated = categoryRepository.save(category);
        Long count = categoryRepository.countApprovedItemsByCategoryId(updated.getId());
        return convertToResponseWithCount(updated, count);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("دسته‌بندی یافت نشد"));

        // بررسی وجود زیردسته
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

    public List<CategoryResponse> getPopularCategories(int limit) { // تعداد دسته بندی هایی که میخوایم برگردونیم3
        List<Object[]> results = categoryRepository.countItemsByCategory();

        // مرتب‌سازی بر اساس تعداد (نزولی)
        results.sort((a, b) -> Long.compare((Long) b[1], (Long) a[1]));

        List<CategoryResponse> responses = new ArrayList<>();
        int count = 0;
        for (Object[] result : results) {
            if (count >= limit) break;
            Long categoryId = (Long) result[0];
            Long itemCount = (Long) result[1];
            categoryRepository.findById(categoryId).ifPresent(category -> {
                responses.add(convertToResponseWithCount(category, itemCount));
            });
            count++;
        }
        return responses;
    }
}