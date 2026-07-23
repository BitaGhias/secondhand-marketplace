package com.secondhand.backend.controller;

import com.secondhand.backend.dto.category.CategoryRequest;
import com.secondhand.backend.dto.category.CategoryResponse;
import com.secondhand.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller exposing the "category" API endpoints.
 * <p>
 * This class is the entry point for HTTP requests; it delegates the work to the service layer and returns the result as JSON with a proper status code. Errors are handled centrally by {@code GlobalExceptionHandler}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Creates category.
     *
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PostMapping("/create")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        CategoryResponse created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Gets all categories.
     *
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/all")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategoriesWithCount());
    }

    /**
     * Gets root categories.
     *
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/roots")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    /**
     * Gets subcategories.
     *
     * @param id unique identifier of the record
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getSubcategories(id));
    }

    /**
     * Gets category by id.
     *
     * @param id unique identifier of the record
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * Updates category.
     *
     * @param id unique identifier of the record
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request
    ) {
        CategoryResponse updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updated);
    }

    // FIX: 200 OK با body -> 204 No Content
    /**
     * Deletes category.
     *
     * @param id unique identifier of the record
     * @return an HTTP response containing the operation result and a proper status code
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets popular categories.
     *
     * @param limit the "limit" value of type {@code int}
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/popular")
    public ResponseEntity<List<CategoryResponse>> getPopularCategories(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(categoryService.getPopularCategories(limit));
    }
}