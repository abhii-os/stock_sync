package com.StockSync.product.microservice.service.impl;

import com.StockSync.product.microservice.DTO.CategoryDTO;
import com.StockSync.product.microservice.Repository.CategoryRepo;
import com.StockSync.product.microservice.exception.CategoryNotFoundException;
import com.StockSync.product.microservice.mapper.CategoryMapper;
import com.StockSync.product.microservice.model.Category;
import com.StockSync.product.microservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepo categoryRepo, CategoryMapper categoryMapper) {
        this.categoryRepo = categoryRepo;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepo.findById(id);
    }

    @Override
    @Transactional
    public Category createCategory(CategoryDTO categoryDTO) {
        if (categoryDTO.getName() == null || categoryDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        try {
            Category category = categoryMapper.toEntity(categoryDTO);
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());
            return categoryRepo.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Category name must be unique: " + categoryDTO.getName());
        }
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepo.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));

        try {
            existingCategory.setName(categoryDTO.getName());
            existingCategory.setDescription(categoryDTO.getDescription());
            existingCategory.setActive(categoryDTO.isActive());
            existingCategory.setUpdatedAt(LocalDateTime.now());
            return categoryRepo.save(existingCategory);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Category name must be unique: " + categoryDTO.getName());
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
        categoryRepo.delete(category);
    }

    @Override
    public boolean existsById(Long id) {
        return categoryRepo.existsById(id);
    }
}
