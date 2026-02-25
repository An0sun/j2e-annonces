package com.masterannonce.application.service;

import com.masterannonce.domain.exception.BusinessException;
import com.masterannonce.domain.exception.ResourceNotFoundException;
import com.masterannonce.domain.exception.UnauthorizedActionException;
import com.masterannonce.domain.model.*;
import com.masterannonce.application.mapper.AnnonceMapper;
import com.masterannonce.infrastructure.persistence.AnnonceRepository;
import com.masterannonce.infrastructure.persistence.CategoryRepository;
import com.masterannonce.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AnnonceService with Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AnnonceServiceTest {

    @Mock private AnnonceRepository annonceRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private AnnonceMapper annonceMapper;
    @InjectMocks private AnnonceService annonceService;

    private User author;
    private Category category;
    private Annonce sampleAnnonce;

    @BeforeEach
    void setUp() {
        author = new User("user1", "user1@test.com", "hashed");
        author.setId(1L);
        author.setRole(Role.ROLE_USER);

        category = new Category("Immobilier");
        category.setId(10L);

        sampleAnnonce = new Annonce();
        sampleAnnonce.setId(100L);
        sampleAnnonce.setTitle("Appartement");
        sampleAnnonce.setDescription("Bel appart");
        sampleAnnonce.setStatus(AnnonceStatus.DRAFT);
        sampleAnnonce.setAuthor(author);
        sampleAnnonce.setCategory(category);
    }

    @Test
    @DisplayName("createAnnonce — sets author and category, saves entity")
    void createAnnonce_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(annonceRepository.save(any(Annonce.class))).thenReturn(sampleAnnonce);

        Annonce result = annonceService.createAnnonce(sampleAnnonce, 1L, 10L);

        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getStatus()).isEqualTo(AnnonceStatus.DRAFT);
        verify(annonceRepository).save(sampleAnnonce);
    }

    @Test
    @DisplayName("createAnnonce — null categoryId is allowed")
    void createAnnonce_noCategoryId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(annonceRepository.save(any(Annonce.class))).thenReturn(sampleAnnonce);

        Annonce result = annonceService.createAnnonce(sampleAnnonce, 1L, null);

        assertThat(result).isNotNull();
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getAnnonceById — not found throws ResourceNotFoundException")
    void getAnnonceById_notFound() {
        when(annonceRepository.findByIdWithRelations(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> annonceService.getAnnonceById(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("publishAnnonce — transitions DRAFT → PUBLISHED")
    void publishAnnonce_success() {
        sampleAnnonce.setStatus(AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));
        when(annonceRepository.save(any(Annonce.class))).thenReturn(sampleAnnonce);

        Annonce result = annonceService.publishAnnonce(100L, 1L);

        assertThat(result.getStatus()).isEqualTo(AnnonceStatus.PUBLISHED);
    }

    @Test
    @DisplayName("publishAnnonce — non-author gets UnauthorizedActionException")
    void publishAnnonce_nonAuthor() {
        sampleAnnonce.setStatus(AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        assertThatThrownBy(() -> annonceService.publishAnnonce(100L, 999L))
            .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    @DisplayName("archiveAnnonce — transitions PUBLISHED → ARCHIVED")
    void archiveAnnonce_success() {
        sampleAnnonce.setStatus(AnnonceStatus.PUBLISHED);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));
        when(annonceRepository.save(any(Annonce.class))).thenReturn(sampleAnnonce);

        Annonce result = annonceService.archiveAnnonce(100L);

        assertThat(result.getStatus()).isEqualTo(AnnonceStatus.ARCHIVED);
    }

    @Test
    @DisplayName("deleteAnnonce — non-author gets UnauthorizedActionException")
    void deleteAnnonce_nonAuthor() {
        sampleAnnonce.setStatus(AnnonceStatus.ARCHIVED);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        assertThatThrownBy(() -> annonceService.deleteAnnonce(100L, 999L))
            .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    @DisplayName("updateAnnonce — PUBLISHED annonce cannot be modified")
    void updateAnnonce_publishedThrows() {
        sampleAnnonce.setStatus(AnnonceStatus.PUBLISHED);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        Annonce updates = new Annonce();
        updates.setTitle("New Title");

        assertThatThrownBy(() -> annonceService.updateAnnonce(100L, updates, null, 1L))
            .isInstanceOf(BusinessException.class);
    }
}
