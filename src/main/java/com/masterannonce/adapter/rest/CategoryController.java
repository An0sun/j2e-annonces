package com.masterannonce.adapter.rest;

import com.masterannonce.application.dto.CategoryCreateDTO;
import com.masterannonce.application.dto.CategoryDTO;
import com.masterannonce.application.mapper.CategoryMapper;
import com.masterannonce.application.service.CategoryService;
import com.masterannonce.domain.model.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/api/v1/categories")
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
    @ApiResponse(responseCode = "200", description = "Liste des catégories retournée")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categoryMapper.toDTOList(categories));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une catégorie")
    @ApiResponse(responseCode = "200", description = "Catégorie trouvée")
    @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(categoryMapper.toDTO(category));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une catégorie", description = "Réservé aux administrateurs")
    @ApiResponse(responseCode = "201", description = "Catégorie créée")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "403", description = "Rôle ADMIN requis")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryCreateDTO dto) {
        Category category = categoryMapper.toEntity(dto);
        Category saved = categoryService.createCategory(category);
        return ResponseEntity.created(URI.create("/api/v1/categories/" + saved.getId()))
            .body(categoryMapper.toDTO(saved));
    }
}
