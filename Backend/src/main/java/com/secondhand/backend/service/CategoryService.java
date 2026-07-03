package com.secondhand.backend.service;

import com.secondhand.backend.entity.Category;
import com.secondhand.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    public CategoryRepository categoryRepository; // استخدام مأمور دیتابیس دسته‌بندی‌ها

    public Category createCategory(Category category) {
        Category existing = categoryRepository.findByName(category.name);
        if (existing != null) {
            throw new RuntimeException("این دسته‌بندی از قبل وجود دارد");
        }
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
