package com.StockSync.product.microservice.mapper;

import com.StockSync.product.microservice.DTO.CategoryDTO;
import com.StockSync.product.microservice.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDTO toDto(Category category) {
        if (category == null) return null;

        return new CategoryDTO(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.isActive()
        );
    }

    public Category toEntity(CategoryDTO dto) {
        if (dto == null) return null;

        Category category = new Category();
        if (dto.getId() != null) {
            category.setId(dto.getId());
        }
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setActive(dto.isActive());
        return category;
    }
}
