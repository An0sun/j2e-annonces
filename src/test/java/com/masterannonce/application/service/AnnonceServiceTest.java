package com.masterannonce.application.service;

import com.masterannonce.application.dto.AnnoncePatchDTO;
import com.masterannonce.domain.exception.BusinessException;
import com.masterannonce.domain.exception.ResourceNotFoundException;
import com.masterannonce.domain.exception.UnauthorizedActionException;
import com.masterannonce.domain.model.*;
import com.masterannonce.application.mapper.AnnonceMapper;
import com.masterannonce.infrastructure.persistence.AnnonceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AnnonceService avec Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AnnonceServiceTest {

    @Mock private AnnonceRepository annonceRepository;
    @Mock private UserService userService;
    @Mock private CategoryService categoryService;
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
    @DisplayName("createAnnonce — définit l'auteur et la catégorie, sauvegarde l'entité")
    void createAnnonce_success() {
        when(userService.getUserById(1L)).thenReturn(author);
        when(categoryService.getCategoryById(10L)).thenReturn(category);
        when(annonceRepository.save(any(Annonce.class))).thenReturn(sampleAnnonce);

        Annonce result = annonceService.createAnnonce(sampleAnnonce, 1L, 10L);

        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getStatus()).isEqualTo(AnnonceStatus.DRAFT);
        verify(annonceRepository).save(sampleAnnonce);
    }

    @Test
    @DisplayName("createAnnonce — categoryId null est autorisé")
    void createAnnonce_noCategoryId() {
        when(userService.getUserById(1L)).thenReturn(author);
        when(annonceRepository.save(any(Annonce.class))).thenReturn(sampleAnnonce);

        Annonce result = annonceService.createAnnonce(sampleAnnonce, 1L, null);

        assertThat(result).isNotNull();
        verify(categoryService, never()).getCategoryById(any());
    }

    @Test
    @DisplayName("getAnnonceById — introuvable lève ResourceNotFoundException")
    void getAnnonceById_notFound() {
        when(annonceRepository.findByIdWithRelations(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> annonceService.getAnnonceById(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("publishAnnonce — transition DRAFT → PUBLISHED")
    void publishAnnonce_success() {
        sampleAnnonce.setStatus(AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));
        when(annonceRepository.save(any(Annonce.class))).thenReturn(sampleAnnonce);

        Annonce result = annonceService.publishAnnonce(100L, 1L);

        assertThat(result.getStatus()).isEqualTo(AnnonceStatus.PUBLISHED);
    }

    @Test
    @DisplayName("publishAnnonce — non-auteur reçoit UnauthorizedActionException")
    void publishAnnonce_nonAuthor() {
        sampleAnnonce.setStatus(AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        assertThatThrownBy(() -> annonceService.publishAnnonce(100L, 999L))
            .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    @DisplayName("archiveAnnonce — transition PUBLISHED → ARCHIVED")
    void archiveAnnonce_success() {
        sampleAnnonce.setStatus(AnnonceStatus.PUBLISHED);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));
        when(annonceRepository.save(any(Annonce.class))).thenReturn(sampleAnnonce);

        Annonce result = annonceService.archiveAnnonce(100L);

        assertThat(result.getStatus()).isEqualTo(AnnonceStatus.ARCHIVED);
    }

    @Test
    @DisplayName("deleteAnnonce — non-auteur reçoit UnauthorizedActionException")
    void deleteAnnonce_nonAuthor() {
        sampleAnnonce.setStatus(AnnonceStatus.ARCHIVED);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        assertThatThrownBy(() -> annonceService.deleteAnnonce(100L, 999L))
            .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    @DisplayName("updateAnnonce — une annonce PUBLISHED ne peut plus être modifiée")
    void updateAnnonce_publishedThrows() {
        sampleAnnonce.setStatus(AnnonceStatus.PUBLISHED);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        Annonce updates = new Annonce();
        updates.setTitle("New Title");

        assertThatThrownBy(() -> annonceService.updateAnnonce(100L, updates, null, 1L))
            .isInstanceOf(BusinessException.class);
    }

    // ===== Nouveaux tests pour augmenter la couverture =====

    @Test
    @DisplayName("updateAnnonce — succès pour une annonce DRAFT avec catégorie")
    void updateAnnonce_success() {
        sampleAnnonce.setStatus(AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        Category newCat = new Category("Véhicules");
        newCat.setId(20L);
        when(categoryService.getCategoryById(20L)).thenReturn(newCat);
        when(annonceRepository.save(any(Annonce.class))).thenReturn(sampleAnnonce);

        Annonce updates = new Annonce();
        updates.setTitle("Updated Title");
        updates.setDescription("Updated Desc");
        updates.setAddress("Paris");
        updates.setMail("new@mail.com");
        updates.setVersion(1L);

        Annonce result = annonceService.updateAnnonce(100L, updates, 20L, 1L);

        assertThat(result).isNotNull();
        verify(categoryService).getCategoryById(20L);
        verify(annonceRepository).save(sampleAnnonce);
    }

    @Test
    @DisplayName("updateAnnonce — succès sans catégorie")
    void updateAnnonce_successNoCategory() {
        sampleAnnonce.setStatus(AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));
        when(annonceRepository.save(any(Annonce.class))).thenReturn(sampleAnnonce);

        Annonce updates = new Annonce();
        updates.setTitle("Title Only");

        Annonce result = annonceService.updateAnnonce(100L, updates, null, 1L);

        assertThat(result).isNotNull();
        verify(categoryService, never()).getCategoryById(any());
    }

    @Test
    @DisplayName("updateAnnonce — non-auteur reçoit UnauthorizedActionException")
    void updateAnnonce_nonAuthor() {
        sampleAnnonce.setStatus(AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        assertThatThrownBy(() -> annonceService.updateAnnonce(100L, new Annonce(), null, 999L))
            .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    @DisplayName("patchAnnonce — succès sans catégorie")
    void patchAnnonce_success() {
        sampleAnnonce.setStatus(AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));
        when(annonceRepository.save(any(Annonce.class))).thenReturn(sampleAnnonce);

        AnnoncePatchDTO patch = new AnnoncePatchDTO();
        patch.setTitle("Patched Title");

        Annonce result = annonceService.patchAnnonce(100L, patch, 1L);

        assertThat(result).isNotNull();
        verify(annonceMapper).patchEntity(patch, sampleAnnonce);
        verify(annonceRepository).save(sampleAnnonce);
    }

    @Test
    @DisplayName("patchAnnonce — avec catégorie")
    void patchAnnonce_withCategory() {
        sampleAnnonce.setStatus(AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        Category newCat = new Category("Services");
        newCat.setId(30L);
        when(categoryService.getCategoryById(30L)).thenReturn(newCat);
        when(annonceRepository.save(any(Annonce.class))).thenReturn(sampleAnnonce);

        AnnoncePatchDTO patch = new AnnoncePatchDTO();
        patch.setCategoryId(30L);

        Annonce result = annonceService.patchAnnonce(100L, patch, 1L);

        assertThat(result).isNotNull();
        verify(categoryService).getCategoryById(30L);
    }

    @Test
    @DisplayName("patchAnnonce — non-auteur reçoit UnauthorizedActionException")
    void patchAnnonce_nonAuthor() {
        sampleAnnonce.setStatus(AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        assertThatThrownBy(() -> annonceService.patchAnnonce(100L, new AnnoncePatchDTO(), 999L))
            .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    @DisplayName("patchAnnonce — annonce PUBLISHED lève BusinessException")
    void patchAnnonce_publishedThrows() {
        sampleAnnonce.setStatus(AnnonceStatus.PUBLISHED);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        assertThatThrownBy(() -> annonceService.patchAnnonce(100L, new AnnoncePatchDTO(), 1L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("deleteAnnonce — succès pour une annonce ARCHIVED")
    void deleteAnnonce_success() {
        sampleAnnonce.setStatus(AnnonceStatus.ARCHIVED);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        annonceService.deleteAnnonce(100L, 1L);

        verify(annonceRepository).delete(sampleAnnonce);
    }

    @Test
    @DisplayName("deleteAnnonce — annonce non-archivée lève BusinessException")
    void deleteAnnonce_notArchivedThrows() {
        sampleAnnonce.setStatus(AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        assertThatThrownBy(() -> annonceService.deleteAnnonce(100L, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("archivée");
    }

    @Test
    @DisplayName("getAnnonceById — succès pour une annonce existante")
    void getAnnonceById_success() {
        when(annonceRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(sampleAnnonce));

        Annonce result = annonceService.getAnnonceById(100L);

        assertThat(result).isEqualTo(sampleAnnonce);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("searchAnnonces — sans filtres utilise la spec de base")
    void searchAnnonces_noFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Annonce> page = new PageImpl<>(List.of(sampleAnnonce));
        when(annonceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Annonce> result = annonceService.searchAnnonces(null, null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("searchAnnonces — avec tous les filtres")
    void searchAnnonces_allFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Annonce> page = new PageImpl<>(List.of());
        when(annonceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Timestamp from = new Timestamp(System.currentTimeMillis() - 86400000);
        Timestamp to = new Timestamp(System.currentTimeMillis());

        Page<Annonce> result = annonceService.searchAnnonces(
            "keyword", AnnonceStatus.PUBLISHED, 10L, 1L, from, to, pageable);

        assertThat(result).isNotNull();
        verify(annonceRepository).findAll(any(Specification.class), eq(pageable));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("searchAnnonces — keyword vide est ignoré")
    void searchAnnonces_emptyKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Annonce> page = new PageImpl<>(List.of(sampleAnnonce));
        when(annonceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Annonce> result = annonceService.searchAnnonces("  ", null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}

