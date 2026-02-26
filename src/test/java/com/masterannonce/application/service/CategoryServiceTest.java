package com.masterannonce.application.service;

import com.masterannonce.domain.exception.ResourceNotFoundException;
import com.masterannonce.domain.model.Category;
import com.masterannonce.infrastructure.persistence.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CategoryService avec Mockito.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryService categoryService;

    @Test
    @DisplayName("getAllCategories — retourne toutes les catégories")
    void getAllCategories() {
        List<Category> categories = List.of(new Category("Immobilier"), new Category("Auto"));
        when(categoryRepository.findAll()).thenReturn(categories);

        List<Category> result = categoryService.getAllCategories();

        assertThat(result).hasSize(2);
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("getCategoryById — succès")
    void getCategoryById_success() {
        Category cat = new Category("Emploi");
        cat.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));

        Category result = categoryService.getCategoryById(1L);

        assertThat(result.getLabel()).isEqualTo("Emploi");
    }

    @Test
    @DisplayName("getCategoryById — introuvable lève ResourceNotFoundException")
    void getCategoryById_notFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createCategory — délègue à repository.save()")
    void createCategory() {
        Category cat = new Category("Services");
        when(categoryRepository.save(any(Category.class))).thenReturn(cat);

        Category result = categoryService.createCategory(cat);

        assertThat(result.getLabel()).isEqualTo("Services");
        verify(categoryRepository).save(cat);
    }
}
