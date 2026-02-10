package org.j2e.dao;

import org.j2e.bean.Annonce;
import org.j2e.bean.AnnonceStatus;
import org.j2e.bean.Category;
import org.j2e.bean.User;
import org.j2e.util.JPAUtil;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Niveau 1 – Tests Repository (intégration avec H2).
 * Teste le CRUD et les requêtes JPQL avec une vraie base de données.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnnonceRepositoryTest {

    private static AnnonceRepository annonceRepo;
    private static UserRepository userRepo;
    private static CategoryRepository categoryRepo;
    private static Long userId;
    private static Long categoryId;
    private static Long annonceId;

    @BeforeAll
    static void setUp() {
        annonceRepo = new AnnonceRepository();
        userRepo = new UserRepository();
        categoryRepo = new CategoryRepository();

        // Créer un utilisateur et une catégorie de test
        User user = new User("testuser", "test@test.com", "pass123");
        userRepo.save(user);

        Category category = new Category("Immobilier");
        categoryRepo.save(category);

        // Récupérer les IDs générés
        userId = userRepo.findByUsername("testuser").getId();
        categoryId = categoryRepo.findAll().get(0).getId();
    }

    @Test
    @Order(1)
    @DisplayName("Créer une annonce (persist)")
    void testCreate() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, userId);
            Category category = em.find(Category.class, categoryId);

            Annonce annonce = new Annonce("Appartement T3", "Bel appart centre-ville", "Paris", "test@mail.com");
            annonce.setAuthor(user);
            annonce.setCategory(category);
            annonce.setStatus(AnnonceStatus.DRAFT);

            em.persist(annonce);
            em.getTransaction().commit();

            assertNotNull(annonce.getId());
            annonceId = annonce.getId();
        } finally {
            em.close();
        }
    }

    @Test
    @Order(2)
    @DisplayName("Trouver par ID")
    void testFindById() {
        Annonce found = annonceRepo.findById(annonceId);
        assertNotNull(found);
        assertEquals("Appartement T3", found.getTitle());
    }

    @Test
    @Order(3)
    @DisplayName("Trouver par ID avec relations (JOIN FETCH)")
    void testFindByIdWithRelations() {
        Annonce found = annonceRepo.findByIdWithRelations(annonceId);
        assertNotNull(found);
        assertNotNull(found.getAuthor());
        assertEquals("testuser", found.getAuthor().getUsername());
        assertNotNull(found.getCategory());
        assertEquals("Immobilier", found.getCategory().getLabel());
    }

    @Test
    @Order(4)
    @DisplayName("Lister toutes les annonces")
    void testFindAll() {
        List<Annonce> all = annonceRepo.findAll();
        assertFalse(all.isEmpty());
        assertTrue(all.size() >= 1);
    }

    @Test
    @Order(5)
    @DisplayName("Recherche par mot-clé")
    void testSearchByKeyword() {
        List<Annonce> results = annonceRepo.searchByKeyword("Appartement");
        assertFalse(results.isEmpty());
        assertEquals("Appartement T3", results.get(0).getTitle());

        // Recherche insensible à la casse
        List<Annonce> resultsLower = annonceRepo.searchByKeyword("appart");
        assertFalse(resultsLower.isEmpty());

        // Recherche sans résultat
        List<Annonce> empty = annonceRepo.searchByKeyword("ZZZZZ");
        assertTrue(empty.isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("Filtrage par catégorie et statut")
    void testFindByCategoryAndStatus() {
        // Par catégorie
        List<Annonce> byCat = annonceRepo.findByCategoryAndStatus(categoryId, null);
        assertFalse(byCat.isEmpty());

        // Par statut
        List<Annonce> byStatus = annonceRepo.findByCategoryAndStatus(null, AnnonceStatus.DRAFT);
        assertFalse(byStatus.isEmpty());

        // Combiné
        List<Annonce> combined = annonceRepo.findByCategoryAndStatus(categoryId, AnnonceStatus.DRAFT);
        assertFalse(combined.isEmpty());

        // Statut inexistant pour cette annonce
        List<Annonce> noMatch = annonceRepo.findByCategoryAndStatus(categoryId, AnnonceStatus.ARCHIVED);
        assertTrue(noMatch.isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("Pagination")
    void testPagination() {
        // Ajouter des annonces pour tester la pagination
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, userId);
            for (int i = 0; i < 7; i++) {
                Annonce a = new Annonce("Annonce " + i, "Desc " + i, "Addr", "mail@test.com");
                a.setAuthor(user);
                a.setStatus(AnnonceStatus.DRAFT);
                em.persist(a);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        // Page 1 (taille 5)
        List<Annonce> page1 = annonceRepo.findPaginated(1, 5);
        assertEquals(5, page1.size());

        // Page 2
        List<Annonce> page2 = annonceRepo.findPaginated(2, 5);
        assertTrue(page2.size() >= 1);

        // Count
        long total = annonceRepo.count();
        assertTrue(total >= 8); // 1 initiale + 7 ajoutées
    }

    @Test
    @Order(8)
    @DisplayName("Supprimer une annonce")
    void testDelete() {
        long countBefore = annonceRepo.count();
        annonceRepo.delete(annonceId);
        long countAfter = annonceRepo.count();
        assertEquals(countBefore - 1, countAfter);

        Annonce deleted = annonceRepo.findById(annonceId);
        assertNull(deleted);
    }
}
