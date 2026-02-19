package org.j2e.service;

import org.j2e.bean.Annonce;
import org.j2e.bean.AnnonceStatus;
import org.j2e.bean.Category;
import org.j2e.bean.User;
import org.j2e.dao.AnnonceRepository;
import org.j2e.dao.CategoryRepository;
import org.j2e.dao.UserRepository; // Assuming a UserRepository exists for validation in Service
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Niveau 4 (Bonus) – Tests Unitaires avec Mockito.
 * Teste la logique métier de AnnonceService en isolant les dépendances (DAO).
 */
@ExtendWith(MockitoExtension.class)
class AnnonceServiceMockitoTest {

    @Mock
    private AnnonceRepository annonceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private AnnonceService annonceService;

    @Test
    @DisplayName("Créer une annonce appelle le repository")
    void testCreateAnnonce() {
        // GIVEN
        Annonce annonce = new Annonce("Dev Java", "Desc", "Paris", "test@test.com");
        Long authorId = 1L;
        Long categoryId = 2L;

        User author = new User();
        author.setId(authorId);
        author.setUsername("testuser");

        Category category = new Category();
        category.setId(categoryId);
        category.setLabel("IT");

        // On configure le mock pour retourner les objets quand on les cherche par ID
        when(userRepository.findById(authorId)).thenReturn(author);
        when(categoryRepository.findById(categoryId)).thenReturn(category);
        
        // WHEN
        annonceService.createAnnonce(annonce, authorId, categoryId);

        // THEN
        // Vérifier que le service a bien appelé les repositories pour trouver author/category
        verify(userRepository).findById(authorId);
        verify(categoryRepository).findById(categoryId);
        
        // Vérifier que l'annonce a bien été sauvegardée avec les bonnes propriétés
        verify(annonceRepository).save(argThat(a -> 
            a.getTitle().equals("Dev Java") &&
            a.getAuthor().getId().equals(authorId) &&
            a.getCategory().getId().equals(categoryId) &&
            a.getStatus() == AnnonceStatus.DRAFT &&
            a.getDate() != null
        ));
    }

    @Test
    @DisplayName("Publier une annonce (DRAFT → PUBLISHED)")
    void testPublishAnnonce() {
        // GIVEN
        Long annonceId = 100L;
        Annonce annonce = new Annonce();
        annonce.setId(annonceId);
        annonce.setStatus(AnnonceStatus.DRAFT);

        when(annonceRepository.findById(annonceId)).thenReturn(annonce);

        // WHEN
        annonceService.publishAnnonce(annonceId);

        // THEN
        assertEquals(AnnonceStatus.PUBLISHED, annonce.getStatus());
        verify(annonceRepository).save(annonce);
    }
}
