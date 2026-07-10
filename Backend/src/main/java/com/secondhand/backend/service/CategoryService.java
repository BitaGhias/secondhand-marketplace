package com.secondhand.backend.service;

import com.secondhand.backend.dto.CategoryRequest;
import com.secondhand.backend.dto.CategoryResponse;
import com.secondhand.backend.entity.Category;
import com.secondhand.backend.exception.custom.BadRequestException;
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
        return new CategoryResponse(category.getId(), category.getName());
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        Category existing = categoryRepository.findByName(request.getName());
        if (existing != null) {
            throw new BadRequestException("این دسته‌بندی از قبل وجود دارد");
        }

        Category category = new Category();
        category.setName(request.getName());

        Category saved = categoryRepository.save(category);
        return convertToResponse(saved);
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> responses = new ArrayList<>();
        for (Category cat : categories) {
            responses.add(convertToResponse(cat));
        }
        return responses;
    }
}