package org.j2e.service;

import org.j2e.bean.Annonce;
import org.j2e.bean.AnnonceStatus;
import org.j2e.bean.Category;
import org.j2e.bean.User;
import org.j2e.util.JPAUtil;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Niveau 2 – Tests Service (unitaires).
 * Teste la logique métier du AnnonceService et du UserService.
 * Utilise H2 en mémoire pour valider les règles métier.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnnonceServiceTest {

    private static AnnonceService annonceService;
    private static UserService userService;
    private static CategoryService categoryService;
    private static Long userId;
    private static Long categoryId;
    private static Long annonceId;

    @BeforeAll
    static void setUp() {
        annonceService = new AnnonceService();
        userService = new UserService();
        categoryService = new CategoryService();

        // Préparer les données
        User user = new User("serviceuser", "service@test.com", "pass123");
        userService.register(user);
        userId = userService.login("serviceuser", "pass123").getId();

        Category cat = new Category("Emploi");
        categoryService.createCategory(cat);
        categoryId = categoryService.getAllCategories().stream()
                .filter(c -> c.getLabel().equals("Emploi"))
                .findFirst().get().getId();
    }

    // ===== UserService Tests =====

    @Test
    @Order(1)
    @DisplayName("Login réussi")
    void testLoginSuccess() {
        User user = userService.login("serviceuser", "pass123");
        assertNotNull(user);
        assertEquals("serviceuser", user.getUsername());
    }

    @Test
    @Order(2)
    @DisplayName("Login échoué - mauvais mot de passe")
    void testLoginWrongPassword() {
        User user = userService.login("serviceuser", "wrongpass");
        assertNull(user);
    }

    @Test
    @Order(3)
    @DisplayName("Login échoué - utilisateur inexistant")
    void testLoginUnknownUser() {
        User user = userService.login("unknown", "pass123");
        assertNull(user);
    }

    @Test
    @Order(4)
    @DisplayName("Inscription échouée - username dupliqué")
    void testRegisterDuplicate() {
        User duplicate = new User("serviceuser", "other@test.com", "pass");
        assertThrows(IllegalArgumentException.class, () -> userService.register(duplicate));
    }

    // ===== AnnonceService Tests =====

    @Test
    @Order(5)
    @DisplayName("Créer une annonce via le service")
    void testCreateAnnonce() {
        Annonce annonce = new Annonce("Dev Java Senior", "CDI Paris", "Paris 8e", "rh@company.com");
        annonceService.createAnnonce(annonce, userId, categoryId);

        assertNotNull(annonce.getId());
        annonceId = annonce.getId();

        // Vérifier que le statut est DRAFT
        Annonce found = annonceService.getAnnonceById(annonceId);
        assertNotNull(found);
        assertEquals(AnnonceStatus.DRAFT, found.getStatus());
    }

    @Test
    @Order(6)
    @DisplayName("Créer avec un utilisateur inexistant → exception")
    void testCreateAnnonceInvalidUser() {
        Annonce annonce = new Annonce("Test", "Test", "Test", "test@test.com");
        assertThrows(IllegalArgumentException.class,
                () -> annonceService.createAnnonce(annonce, 99999L, null));
    }

    @Test
    @Order(7)
    @DisplayName("Publier une annonce (DRAFT → PUBLISHED)")
    void testPublishAnnonce() {
        annonceService.publishAnnonce(annonceId);

        Annonce published = annonceService.getAnnonceById(annonceId);
        assertEquals(AnnonceStatus.PUBLISHED, published.getStatus());
    }

    @Test
    @Order(8)
    @DisplayName("Archiver une annonce (PUBLISHED → ARCHIVED)")
    void testArchiveAnnonce() {
        annonceService.archiveAnnonce(annonceId);

        Annonce archived = annonceService.getAnnonceById(annonceId);
        assertEquals(AnnonceStatus.ARCHIVED, archived.getStatus());
    }

    @Test
    @Order(9)
    @DisplayName("Modifier une annonce")
    void testUpdateAnnonce() {
        Annonce toUpdate = new Annonce("Dev Java Confirmé", "CDI Lyon", "Lyon 3e", "rh@company.com");
        toUpdate.setId(annonceId);

        annonceService.updateAnnonce(toUpdate, categoryId);

        Annonce updated = annonceService.getAnnonceById(annonceId);
        assertEquals("Dev Java Confirmé", updated.getTitle());
        assertEquals("Lyon 3e", updated.getAdress());
    }

    @Test
    @Order(10)
    @DisplayName("Modifier une annonce inexistante → exception")
    void testUpdateAnnonceNotFound() {
        Annonce ghost = new Annonce("Ghost", "Ghost", "Ghost", "ghost@test.com");
        ghost.setId(99999L);
        assertThrows(IllegalArgumentException.class,
                () -> annonceService.updateAnnonce(ghost, null));
    }

    @Test
    @Order(11)
    @DisplayName("Recherche par mot-clé")
    void testSearch() {
        var results = annonceService.search("Java");
        assertFalse(results.isEmpty());
    }

    @Test
    @Order(12)
    @DisplayName("Supprimer une annonce")
    void testDeleteAnnonce() {
        annonceService.deleteAnnonce(annonceId);
        Annonce deleted = annonceService.getAnnonceById(annonceId);
        assertNull(deleted);
    }
}
