package com.masterannonce.application.service;

import com.masterannonce.application.mapper.AnnonceMapper;
import com.masterannonce.domain.exception.BusinessException;
import com.masterannonce.domain.exception.ResourceNotFoundException;
import com.masterannonce.domain.exception.UnauthorizedActionException;
import com.masterannonce.domain.model.*;
import com.masterannonce.infrastructure.persistence.AnnonceRepository;
import com.masterannonce.infrastructure.persistence.CategoryRepository;
import com.masterannonce.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du service Annonce avec Mockito.
 * Vérifie les règles métier sans base de données.
 */
@ExtendWith(MockitoExtension.class)
class AnnonceServiceTest {

    @Mock
    private AnnonceRepository annonceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AnnonceMapper annonceMapper;

    @InjectMocks
    private AnnonceService annonceService;

    private User testUser;
    private Category testCategory;
    private Annonce testAnnonce;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@test.com", "hashedpassword");
        testUser.setId(1L);

        testCategory = new Category("Immobilier");
        testCategory.setId(1L);

        testAnnonce = new Annonce("Titre test", "Description test", "Paris", "test@test.com");
        testAnnonce.setId(1L);
        testAnnonce.setAuthor(testUser);
        testAnnonce.setCategory(testCategory);
        testAnnonce.setStatus(AnnonceStatus.DRAFT);
        testAnnonce.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    }

    // ===== Création =====

    @Nested
    @DisplayName("createAnnonce")
    class CreateAnnonce {

        @Test
        @DisplayName("Doit créer une annonce en statut DRAFT")
        void shouldCreateAnnonceAsDraft() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(annonceRepository.save(any(Annonce.class))).thenAnswer(inv -> inv.getArgument(0));

            Annonce annonce = new Annonce("Nouveau titre", "Nouvelle desc", "Lyon", "new@test.com");
            Annonce result = annonceService.createAnnonce(annonce, 1L, 1L);

            assertThat(result.getStatus()).isEqualTo(AnnonceStatus.DRAFT);
            assertThat(result.getAuthor()).isEqualTo(testUser);
            assertThat(result.getCategory()).isEqualTo(testCategory);
            verify(annonceRepository).save(annonce);
        }

        @Test
        @DisplayName("Doit rejeter si l'auteur est introuvable")
        void shouldThrowIfAuthorNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            Annonce annonce = new Annonce("Titre", "Desc", "Paris", "t@t.com");

            assertThatThrownBy(() -> annonceService.createAnnonce(annonce, 99L, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
        }

        @Test
        @DisplayName("Doit rejeter si la catégorie est introuvable")
        void shouldThrowIfCategoryNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            Annonce annonce = new Annonce("Titre", "Desc", "Paris", "t@t.com");

            assertThatThrownBy(() -> annonceService.createAnnonce(annonce, 1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
        }
    }

    // ===== Mise à jour =====

    @Nested
    @DisplayName("updateAnnonce")
    class UpdateAnnonce {

        @Test
        @DisplayName("Doit permettre à l'auteur de modifier une annonce DRAFT")
        void shouldAllowAuthorToUpdateDraft() {
            when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAnnonce));
            when(annonceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Annonce updates = new Annonce("Titre mis à jour", "Desc maj", "Lyon", "maj@test.com");
            Annonce result = annonceService.updateAnnonce(1L, updates, null, 1L);

            assertThat(result.getTitle()).isEqualTo("Titre mis à jour");
        }

        @Test
        @DisplayName("Doit rejeter si l'utilisateur n'est pas l'auteur")
        void shouldRejectIfNotAuthor() {
            when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAnnonce));

            Annonce updates = new Annonce("Update", "Desc", "Paris", "t@t.com");

            assertThatThrownBy(() -> annonceService.updateAnnonce(1L, updates, null, 999L))
                .isInstanceOf(UnauthorizedActionException.class);
        }

        @Test
        @DisplayName("Doit rejeter la modification d'une annonce PUBLISHED")
        void shouldRejectUpdateIfPublished() {
            testAnnonce.setStatus(AnnonceStatus.PUBLISHED);
            when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAnnonce));

            Annonce updates = new Annonce("Update", "Desc", "Paris", "t@t.com");

            assertThatThrownBy(() -> annonceService.updateAnnonce(1L, updates, null, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("publiée");
        }
    }

    // ===== Publication =====

    @Nested
    @DisplayName("publishAnnonce")
    class PublishAnnonce {

        @Test
        @DisplayName("Doit publier une annonce DRAFT")
        void shouldPublishDraftAnnonce() {
            when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAnnonce));
            when(annonceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Annonce result = annonceService.publishAnnonce(1L, 1L);
            assertThat(result.getStatus()).isEqualTo(AnnonceStatus.PUBLISHED);
        }

        @Test
        @DisplayName("Doit rejeter la publication d'une annonce déjà publiée")
        void shouldRejectPublishingPublished() {
            testAnnonce.setStatus(AnnonceStatus.PUBLISHED);
            when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAnnonce));

            assertThatThrownBy(() -> annonceService.publishAnnonce(1L, 1L))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    // ===== Archivage =====

    @Nested
    @DisplayName("archiveAnnonce")
    class ArchiveAnnonce {

        @Test
        @DisplayName("Doit archiver une annonce")
        void shouldArchiveAnnonce() {
            when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAnnonce));
            when(annonceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Annonce result = annonceService.archiveAnnonce(1L);
            assertThat(result.getStatus()).isEqualTo(AnnonceStatus.ARCHIVED);
        }
    }

    // ===== Suppression =====

    @Nested
    @DisplayName("deleteAnnonce")
    class DeleteAnnonce {

        @Test
        @DisplayName("Doit supprimer une annonce archivée")
        void shouldDeleteArchivedAnnonce() {
            testAnnonce.setStatus(AnnonceStatus.ARCHIVED);
            when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAnnonce));

            annonceService.deleteAnnonce(1L, 1L);
            verify(annonceRepository).delete(testAnnonce);
        }

        @Test
        @DisplayName("Doit rejeter la suppression d'une annonce non archivée")
        void shouldRejectDeleteIfNotArchived() {
            when(annonceRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAnnonce));

            assertThatThrownBy(() -> annonceService.deleteAnnonce(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("archivée");
        }
    }
}
