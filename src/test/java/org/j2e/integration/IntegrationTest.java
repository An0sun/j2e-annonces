package org.j2e.integration;

import org.j2e.bean.Annonce;
import org.j2e.bean.AnnonceStatus;
import org.j2e.bean.Category;
import org.j2e.bean.User;
import org.j2e.service.AnnonceService;
import org.j2e.service.CategoryService;
import org.j2e.service.UserService;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Niveau 3 – Tests d'intégration métier.
 * Enchaînement complet : création → publication → recherche.
 * Test reproduisant un problème Lazy / N+1.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegrationTest {

    private static AnnonceService annonceService;
    private static UserService userService;
    private static CategoryService categoryService;
    private static Long userId;
    private static Long categoryId;

    @BeforeAll
    static void setUp() {
        annonceService = new AnnonceService();
        userService = new UserService();
        categoryService = new CategoryService();

        User user = new User("integuser", "integ@test.com", "password");
        userService.register(user);
        userId = userService.login("integuser", "password").getId();

        Category cat = new Category("Véhicules");
        categoryService.createCategory(cat);
        categoryId = categoryService.getAllCategories().stream()
                .filter(c -> c.getLabel().equals("Véhicules"))
                .findFirst().get().getId();
    }

    @Test
    @Order(1)
    @DisplayName("Workflow complet : création → publication → recherche")
    void testFullWorkflow() {
        // 1. Création
        Annonce annonce = new Annonce("Peugeot 308", "Diesel, 80k km", "Marseille", "auto@vente.com");
        annonceService.createAnnonce(annonce, userId, categoryId);
        Long id = annonce.getId();
        assertNotNull(id);

        // 2. Vérifier que le statut est DRAFT
        Annonce draft = annonceService.getAnnonceById(id);
        assertEquals(AnnonceStatus.DRAFT, draft.getStatus());

        // 3. Publication
        annonceService.publishAnnonce(id);
        Annonce published = annonceService.getAnnonceById(id);
        assertEquals(AnnonceStatus.PUBLISHED, published.getStatus());

        // 4. Recherche : retrouver l'annonce publiée
        List<Annonce> results = annonceService.search("Peugeot");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(a -> a.getTitle().equals("Peugeot 308")));

        // 5. Filtrage par catégorie et statut
        List<Annonce> filtered = annonceService.filter(categoryId, AnnonceStatus.PUBLISHED);
        assertFalse(filtered.isEmpty());

        // 6. Archivage
        annonceService.archiveAnnonce(id);
        Annonce archived = annonceService.getAnnonceById(id);
        assertEquals(AnnonceStatus.ARCHIVED, archived.getStatus());

        // 7. Suppression
        annonceService.deleteAnnonce(id);
        assertNull(annonceService.getAnnonceById(id));
    }

    @Test
    @Order(2)
    @DisplayName("Test N+1 : accès aux relations sans LazyInitializationException")
    void testNoLazyInitializationException() {
        // Créer plusieurs annonces avec auteur et catégorie
        for (int i = 0; i < 5; i++) {
            Annonce a = new Annonce("Voiture " + i, "Desc " + i, "Ville", "mail@test.com");
            annonceService.createAnnonce(a, userId, categoryId);
        }

        // Récupérer toutes les annonces (avec JOIN FETCH)
        // Si N+1 n'est pas géré, on aurait 1 + N requêtes.
        // Avec JOIN FETCH, tout est chargé en 1 requête.
        List<Annonce> all = annonceService.getAllAnnonces();
        assertFalse(all.isEmpty());

        // Accéder aux relations APRÈS la fermeture de l'EntityManager
        // Ceci lèverait une LazyInitializationException si JOIN FETCH n'était pas utilisé
        assertDoesNotThrow(() -> {
            for (Annonce a : all) {
                if (a.getAuthor() != null) {
                    String username = a.getAuthor().getUsername();
                    assertNotNull(username);
                }
                if (a.getCategory() != null) {
                    String label = a.getCategory().getLabel();
                    assertNotNull(label);
                }
            }
        });
    }

    @Test
    @Order(3)
    @DisplayName("Pagination fonctionne avec les relations chargées")
    void testPaginationWithRelations() {
        List<Annonce> page1 = annonceService.listPaginated(1, 3);
        assertNotNull(page1);
        assertTrue(page1.size() <= 3);

        // Vérifier qu'on peut accéder aux relations
        for (Annonce a : page1) {
            assertDoesNotThrow(() -> {
                if (a.getAuthor() != null) a.getAuthor().getUsername();
                if (a.getCategory() != null) a.getCategory().getLabel();
            });
        }

        long total = annonceService.countAnnonces();
        assertTrue(total >= 5);
    }
}
