package org.j2e.rest;

import org.j2e.bean.Annonce;
import org.j2e.bean.User;
import org.j2e.security.TokenStore;
import org.j2e.service.AnnonceService;
import org.j2e.service.UserService;
import org.j2e.util.JPAUtil;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de charge simples (Exercice 10.3).
 *
 * Vérifie que les services et repositories supportent des accès concurrents
 * sans erreur ni corruption de données.
 */
class LoadTestIT {

    private static final int NUM_THREADS = 20;
    private static final int REQUESTS_PER_THREAD = 10;

    private UserService userService;
    private AnnonceService annonceService;
    private Long userId;
    private String token;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        annonceService = new AnnonceService();

        // Créer un utilisateur de test
        User user = new User("loadtest_user", "load@test.com", "password");
        userService.register(user);
        userId = user.getId();

        // Générer un token
        token = TokenStore.getInstance().generateToken(userId, "loadtest_user");
    }

    @AfterEach
    void tearDown() {
        // Nettoyage : supprimer les annonces et l'utilisateur
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Annonce a WHERE a.author.id = :uid")
                    .setParameter("uid", userId)
                    .executeUpdate();
            em.createQuery("DELETE FROM User u WHERE u.id = :uid")
                    .setParameter("uid", userId)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        } finally {
            em.close();
        }
        TokenStore.getInstance().removeToken(token);
    }

    @Test
    @DisplayName("GET concurrents - Lecture paginée sous charge")
    void concurrentReads() throws Exception {
        // D'abord, créer quelques annonces
        for (int i = 0; i < 5; i++) {
            Annonce a = new Annonce("Load Test " + i, "Description " + i, "Adresse", "test@test.com");
            annonceService.createAnnonce(a, userId, null);
        }

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        // Lancer N requêtes concurrentes de lecture
        for (int i = 0; i < NUM_THREADS * REQUESTS_PER_THREAD; i++) {
            futures.add(executor.submit(() -> {
                try {
                    List<Annonce> result = annonceService.listPaginated(1, 10);
                    assertNotNull(result);
                    successes.incrementAndGet();
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            }));
        }

        // Attendre la fin
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        // Vérifier les résultats
        int totalRequests = NUM_THREADS * REQUESTS_PER_THREAD;
        assertEquals(totalRequests, successes.get(),
                "Toutes les lectures doivent réussir sous charge");
        assertEquals(0, errors.get(),
                "Aucune erreur ne doit survenir lors de lectures concurrentes");
    }

    @Test
    @DisplayName("POST concurrents - Créations simultanées")
    void concurrentCreates() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        int totalCreations = NUM_THREADS * 5; // 100 créations

        for (int i = 0; i < totalCreations; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                try {
                    Annonce a = new Annonce(
                            "Concurrent " + idx,
                            "Description concurrente",
                            "Adresse " + idx,
                            "test" + idx + "@test.com");
                    annonceService.createAnnonce(a, userId, null);
                    assertNotNull(a.getId());
                    successes.incrementAndGet();
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            }));
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        assertEquals(totalCreations, successes.get(),
                "Toutes les créations doivent réussir sous charge");
        assertEquals(0, errors.get(),
                "Aucune erreur ne doit survenir lors de créations concurrentes");

        // Vérifier que toutes les annonces existent en base
        long count = annonceService.countAnnonces();
        assertTrue(count >= totalCreations,
                "Le nombre d'annonces en base doit refléter toutes les créations");
    }

    @Test
    @DisplayName("Mixte concurrents - Lectures + Écritures simultanées")
    void concurrentMixed() throws Exception {
        // Créer quelques annonces initiales
        for (int i = 0; i < 3; i++) {
            Annonce a = new Annonce("Initial " + i, "Desc", "Addr", "init@test.com");
            annonceService.createAnnonce(a, userId, null);
        }

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        AtomicInteger readSuccesses = new AtomicInteger(0);
        AtomicInteger writeSuccesses = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);

        int totalOps = NUM_THREADS * REQUESTS_PER_THREAD;

        for (int i = 0; i < totalOps; i++) {
            final int idx = i;
            if (idx % 3 == 0) {
                // 1/3 écritures
                futures(executor, () -> {
                    try {
                        Annonce a = new Annonce("Mixed " + idx, "Desc", "Addr", "m@t.com");
                        annonceService.createAnnonce(a, userId, null);
                        writeSuccesses.incrementAndGet();
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                });
            } else {
                // 2/3 lectures
                futures(executor, () -> {
                    try {
                        annonceService.listPaginated(1, 10);
                        readSuccesses.incrementAndGet();
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                });
            }
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        assertTrue(readSuccesses.get() > 0, "Des lectures doivent avoir réussi");
        assertTrue(writeSuccesses.get() > 0, "Des écritures doivent avoir réussi");
        assertEquals(0, errors.get(), "Aucune erreur en mode mixte");
    }

    private void futures(ExecutorService executor, Runnable task) {
        executor.submit(task);
    }
}
