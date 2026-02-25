package com.masterannonce.application.mapper;

import com.masterannonce.application.dto.CategoryCreateDTO;
import com.masterannonce.application.dto.CategoryDTO;
import com.masterannonce.domain.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper MapStruct pour Category ↔ DTO.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDTO toDTO(Category entity);

    List<CategoryDTO> toDTOList(List<Category> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "annonces", ignore = true)
    Category toEntity(CategoryCreateDTO dto);
}
