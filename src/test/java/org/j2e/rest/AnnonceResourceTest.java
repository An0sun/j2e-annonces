package org.j2e.rest;

import org.j2e.bean.Annonce;
import org.j2e.bean.AnnonceStatus;
import org.j2e.rest.dto.*;
import org.j2e.rest.exception.BusinessException;
import org.j2e.rest.exception.ForbiddenException;
import org.j2e.rest.exception.NotFoundException;
import org.j2e.rest.mapper.AnnonceMapper;
import org.j2e.rest.resource.AnnonceResource;
import org.j2e.bean.User;
import org.j2e.bean.Category;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de la Resource REST Annonce.
 * Teste le mapping DTO ↔ Entity et la logique de validation.
 */
class AnnonceResourceTest {

    // ===== Tests du Mapper =====

    @Test
    @DisplayName("Mapper : Entity → DTO avec Builder")
    void testEntityToDTO() {
        User author = new User("john", "john@test.com", "pass");
        author.setId(1L);

        Category category = new Category("Immobilier");
        category.setId(2L);

        Annonce annonce = new Annonce("Appartement T3", "Bel appart", "Paris", "contact@test.com");
        annonce.setId(10L);
        annonce.setStatus(AnnonceStatus.DRAFT);
        annonce.setDate(new Timestamp(System.currentTimeMillis()));
        annonce.setAuthor(author);
        annonce.setCategory(category);

        AnnonceDTO dto = AnnonceMapper.toDTO(annonce);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Appartement T3", dto.getTitle());
        assertEquals("Bel appart", dto.getDescription());
        assertEquals("Paris", dto.getAdress());
        assertEquals("contact@test.com", dto.getMail());
        assertEquals("DRAFT", dto.getStatus());
        assertEquals("john", dto.getAuthorUsername());
        assertEquals(1L, dto.getAuthorId());
        assertEquals("Immobilier", dto.getCategoryLabel());
        assertEquals(2L, dto.getCategoryId());
    }

    @Test
    @DisplayName("Mapper : Entity null → null")
    void testEntityNullToDTO() {
        assertNull(AnnonceMapper.toDTO(null));
    }

    @Test
    @DisplayName("Mapper : Entity sans relations → DTO avec champs null")
    void testEntityWithoutRelationsToDTO() {
        Annonce annonce = new Annonce("Test", "Desc", "Addr", "mail@test.com");
        annonce.setId(1L);
        annonce.setStatus(AnnonceStatus.DRAFT);

        AnnonceDTO dto = AnnonceMapper.toDTO(annonce);

        assertNotNull(dto);
        assertEquals("Test", dto.getTitle());
        assertNull(dto.getAuthorUsername());
        assertNull(dto.getCategoryLabel());
    }

    // ===== Tests des DTOs =====

    @Test
    @DisplayName("AnnonceDTO Builder")
    void testAnnonceDTOBuilder() {
        AnnonceDTO dto = AnnonceDTO.builder()
                .id(1L)
                .title("Test")
                .description("Description")
                .status("DRAFT")
                .authorUsername("john")
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Test", dto.getTitle());
        assertEquals("DRAFT", dto.getStatus());
        assertEquals("john", dto.getAuthorUsername());
    }

    @Test
    @DisplayName("PaginatedResponse calcul totalPages")
    void testPaginatedResponse() {
        PaginatedResponse<String> response = new PaginatedResponse<>(
                java.util.List.of("a", "b", "c"), 1, 10, 25);

        assertEquals(1, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(25, response.getTotalElements());
        assertEquals(3, response.getTotalPages()); // ceil(25/10) = 3
    }

    @Test
    @DisplayName("ApiErrorResponse format correct")
    void testApiErrorResponse() {
        ApiErrorResponse error = new ApiErrorResponse(404, "Not Found", "Annonce introuvable");

        assertEquals(404, error.getStatus());
        assertEquals("Not Found", error.getError());
        assertEquals("Annonce introuvable", error.getMessage());
        assertNotNull(error.getTimestamp());
        assertTrue(error.getDetails().isEmpty());
    }

    @Test
    @DisplayName("ApiErrorResponse avec détails")
    void testApiErrorResponseWithDetails() {
        ApiErrorResponse error = new ApiErrorResponse(
                400, "Bad Request", "Validation failed",
                java.util.List.of("Le titre est obligatoire", "L'email est invalide"));

        assertEquals(400, error.getStatus());
        assertEquals(2, error.getDetails().size());
    }

    // ===== Tests des Exceptions =====

    @Test
    @DisplayName("NotFoundException avec entité et id")
    void testNotFoundException() {
        NotFoundException ex = new NotFoundException("Annonce", 42L);
        assertEquals("Annonce introuvable (id=42)", ex.getMessage());
    }

    @Test
    @DisplayName("BusinessException message")
    void testBusinessException() {
        BusinessException ex = new BusinessException("Annonce publiée non modifiable");
        assertEquals("Annonce publiée non modifiable", ex.getMessage());
    }

    @Test
    @DisplayName("ForbiddenException message")
    void testForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Seul l'auteur peut modifier");
        assertEquals("Seul l'auteur peut modifier", ex.getMessage());
    }

    // ===== Tests du TokenStore =====

    @Test
    @DisplayName("TokenStore : génération et validation de token")
    void testTokenStoreGenerateAndValidate() {
        var store = org.j2e.security.TokenStore.getInstance();
        String token = store.generateToken(1L, "testuser");

        assertNotNull(token);
        var info = store.validateToken(token);
        assertNotNull(info);
        assertEquals(1L, info.getUserId());
        assertEquals("testuser", info.getUsername());

        // Cleanup
        store.removeToken(token);
    }

    @Test
    @DisplayName("TokenStore : token invalide retourne null")
    void testTokenStoreInvalidToken() {
        var store = org.j2e.security.TokenStore.getInstance();
        assertNull(store.validateToken("invalid-token-xyz"));
        assertNull(store.validateToken(null));
    }

    @Test
    @DisplayName("TokenStore : suppression de token")
    void testTokenStoreRemoveToken() {
        var store = org.j2e.security.TokenStore.getInstance();
        String token = store.generateToken(2L, "user2");

        assertNotNull(store.validateToken(token));
        store.removeToken(token);
        assertNull(store.validateToken(token));
    }
}
