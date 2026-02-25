package com.masterannonce.adapter.rest;

import com.masterannonce.application.dto.CategoryCreateDTO;
import com.masterannonce.application.dto.CategoryDTO;
import com.masterannonce.application.mapper.CategoryMapper;
import com.masterannonce.application.service.CategoryService;
import com.masterannonce.domain.model.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controller REST pour les Catégories.
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Catégories", description = "Gestion des catégories d'annonces")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    @GetMapping
    @Operation(summary = "Lister les catégories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categoryMapper.toDTOList(categories));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une catégorie")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(categoryMapper.toDTO(category));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une catégorie", description = "Réservé aux administrateurs")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryCreateDTO dto) {
        Category category = categoryMapper.toEntity(dto);
        Category saved = categoryService.createCategory(category);
        return ResponseEntity.created(URI.create("/api/categories/" + saved.getId()))
            .body(categoryMapper.toDTO(saved));
    }
}
