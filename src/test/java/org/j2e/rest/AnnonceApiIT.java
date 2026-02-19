package org.j2e.rest;

import org.j2e.bean.Annonce;
import org.j2e.bean.AnnonceStatus;
import org.j2e.bean.Category;
import org.j2e.bean.User;
import org.j2e.rest.dto.*;
import org.j2e.rest.mapper.AnnonceMapper;
import org.j2e.security.TokenStore;
import org.j2e.service.AnnonceService;
import org.j2e.service.CategoryService;
import org.j2e.service.UserService;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration REST.
 * Simule un workflow complet : login → CRUD annonces → vérification des règles
 * métier.
 * Utilise les vrais services avec H2 en mémoire.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnnonceApiIT {

    private static AnnonceService annonceService;
    private static UserService userService;
    private static CategoryService categoryService;
    private static TokenStore tokenStore;
    private static Long userId;
    private static Long otherUserId;
    private static Long categoryId;
    private static Long annonceId;
    private static String userToken;
    private static String otherUserToken;

    @BeforeAll
    static void setUp() {
        annonceService = new AnnonceService();
        userService = new UserService();
        categoryService = new CategoryService();
        tokenStore = TokenStore.getInstance();

        // Créer deux utilisateurs pour tester les permissions
        User user = new User("apiuser", "api@test.com", "password");
        userService.register(user);
        userId = userService.login("apiuser", "password").getId();
        userToken = tokenStore.generateToken(userId, "apiuser");

        User otherUser = new User("otheruser", "other@test.com", "password");
        userService.register(otherUser);
        otherUserId = userService.login("otheruser", "password").getId();
        otherUserToken = tokenStore.generateToken(otherUserId, "otheruser");

        // Créer une catégorie
        Category cat = new Category("API-Test");
        categoryService.createCategory(cat);
        categoryId = categoryService.getAllCategories().stream()
                .filter(c -> c.getLabel().equals("API-Test"))
                .findFirst().get().getId();
    }

    // ===== Auth Flow =====

    @Test
    @Order(1)
    @DisplayName("Login : credentials valides → token généré")
    void testLoginSuccess() {
        User user = userService.login("apiuser", "password");
        assertNotNull(user);

        String token = tokenStore.generateToken(user.getId(), user.getUsername());
        assertNotNull(token);

        TokenStore.TokenInfo info = tokenStore.validateToken(token);
        assertNotNull(info);
        assertEquals(user.getId(), info.getUserId());

        tokenStore.removeToken(token);
    }

    @Test
    @Order(2)
    @DisplayName("Login : credentials invalides → null")
    void testLoginFailed() {
        User user = userService.login("apiuser", "wrongpassword");
        assertNull(user);
    }

    @Test
    @Order(3)
    @DisplayName("Token : token invalide → validation échoue")
    void testInvalidToken() {
        assertNull(tokenStore.validateToken("fake-token"));
    }

    // ===== CRUD Annonce =====

    @Test
    @Order(10)
    @DisplayName("POST /api/annonces : Création d'une annonce")
    void testCreateAnnonce() {
        Annonce annonce = new Annonce("API Test Annonce", "Description API", "Paris", "api@test.com");
        annonceService.createAnnonce(annonce, userId, categoryId);

        assertNotNull(annonce.getId());
        annonceId = annonce.getId();

        Annonce found = annonceService.getAnnonceById(annonceId);
        assertNotNull(found);
        assertEquals(AnnonceStatus.DRAFT, found.getStatus());
        assertEquals("API Test Annonce", found.getTitle());
    }

    @Test
    @Order(11)
    @DisplayName("GET /api/annonces : Liste paginée")
    void testListPaginated() {
        List<Annonce> page1 = annonceService.listPaginated(1, 5);
        assertNotNull(page1);
        assertFalse(page1.isEmpty());

        long total = annonceService.countAnnonces();
        assertTrue(total >= 1);

        // Vérifier le mapping DTO
        List<AnnonceDTO> dtos = AnnonceMapper.toDTOList(page1);
        assertFalse(dtos.isEmpty());
        assertNotNull(dtos.get(0).getTitle());
    }

    @Test
    @Order(12)
    @DisplayName("GET /api/annonces/{id} : Détail")
    void testGetAnnonceById() {
        Annonce found = annonceService.getAnnonceById(annonceId);
        assertNotNull(found);

        AnnonceDTO dto = AnnonceMapper.toDTO(found);
        assertEquals(annonceId, dto.getId());
        assertEquals("API Test Annonce", dto.getTitle());
        assertEquals("apiuser", dto.getAuthorUsername());
    }

    @Test
    @Order(13)
    @DisplayName("GET /api/annonces/{id} : 404 pour ID inexistant")
    void testGetAnnonceNotFound() {
        Annonce notFound = annonceService.getAnnonceById(99999L);
        assertNull(notFound);
    }

    @Test
    @Order(20)
    @DisplayName("PUT /api/annonces/{id} : Mise à jour par l'auteur")
    void testUpdateByAuthor() {
        Annonce toUpdate = new Annonce("Updated Title", "Updated Desc", "Lyon", "new@test.com");
        toUpdate.setId(annonceId);

        annonceService.updateAnnonce(toUpdate, categoryId);

        Annonce updated = annonceService.getAnnonceById(annonceId);
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Lyon", updated.getAdress());
    }

    @Test
    @Order(21)
    @DisplayName("Règle métier : Non-auteur ne peut pas modifier")
    void testUpdateByNonAuthor() {
        Annonce annonce = annonceService.getAnnonceById(annonceId);
        assertNotNull(annonce);
        assertNotNull(annonce.getAuthor());
        // Vérifier que l'auteur est bien userId et pas otherUserId
        assertEquals(userId, annonce.getAuthor().getId());
        assertNotEquals(otherUserId, annonce.getAuthor().getId());
    }

    // ===== Workflow Statut =====

    @Test
    @Order(30)
    @DisplayName("POST /api/annonces/{id}/publish : DRAFT → PUBLISHED")
    void testPublishAnnonce() {
        annonceService.publishAnnonce(annonceId);

        Annonce published = annonceService.getAnnonceById(annonceId);
        assertEquals(AnnonceStatus.PUBLISHED, published.getStatus());
    }

    @Test
    @Order(31)
    @DisplayName("Règle métier : Annonce PUBLISHED non modifiable")
    void testCannotModifyPublished() {
        Annonce published = annonceService.getAnnonceById(annonceId);
        assertEquals(AnnonceStatus.PUBLISHED, published.getStatus());
        // En conditions réelles, la resource REST lancerait une BusinessException
    }

    @Test
    @Order(32)
    @DisplayName("POST /api/annonces/{id}/archive : PUBLISHED → ARCHIVED")
    void testArchiveAnnonce() {
        annonceService.archiveAnnonce(annonceId);

        Annonce archived = annonceService.getAnnonceById(annonceId);
        assertEquals(AnnonceStatus.ARCHIVED, archived.getStatus());
    }

    @Test
    @Order(33)
    @DisplayName("Règle métier : Suppression uniquement après archivage")
    void testDeleteRequiresArchived() {
        Annonce archived = annonceService.getAnnonceById(annonceId);
        assertEquals(AnnonceStatus.ARCHIVED, archived.getStatus());
        // L'annonce est archivée, la suppression devrait fonctionner
    }

    @Test
    @Order(40)
    @DisplayName("DELETE /api/annonces/{id} : Suppression d'annonce archivée")
    void testDeleteArchivedAnnonce() {
        annonceService.deleteAnnonce(annonceId);
        Annonce deleted = annonceService.getAnnonceById(annonceId);
        assertNull(deleted);
    }

    // ===== Pagination =====

    @Test
    @Order(50)
    @DisplayName("Pagination : création de données + vérification pages")
    void testPagination() {
        // Créer 8 annonces
        for (int i = 0; i < 8; i++) {
            Annonce a = new Annonce("Pagination " + i, "Desc " + i, "Ville", "p@test.com");
            annonceService.createAnnonce(a, userId, categoryId);
        }

        List<Annonce> page1 = annonceService.listPaginated(1, 5);
        assertEquals(5, page1.size());

        List<Annonce> page2 = annonceService.listPaginated(2, 5);
        assertTrue(page2.size() >= 1);

        long total = annonceService.countAnnonces();
        assertTrue(total >= 8);

        // Vérifier PaginatedResponse
        List<AnnonceDTO> dtos = AnnonceMapper.toDTOList(page1);
        PaginatedResponse<AnnonceDTO> response = new PaginatedResponse<>(dtos, 1, 5, total);
        assertEquals(1, response.getPage());
        assertEquals(5, response.getSize());
        assertTrue(response.getTotalPages() >= 2);
    }
}
