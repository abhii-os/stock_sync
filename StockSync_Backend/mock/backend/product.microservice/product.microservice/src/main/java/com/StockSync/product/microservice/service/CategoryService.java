package com.StockSync.product.microservice.service;

import com.StockSync.product.microservice.DTO.CategoryDTO;
import com.StockSync.product.microservice.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(Long id);
    Category createCategory(CategoryDTO categoryDTO);
    Category updateCategory(Long id, CategoryDTO categoryDTO);
    void deleteCategory(Long id);
    boolean existsById(Long id);
}
