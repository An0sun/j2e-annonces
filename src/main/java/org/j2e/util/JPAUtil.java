package org.j2e.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Classe utilitaire pour obtenir un EntityManager.
 * Gère un singleton EntityManagerFactory.
 */
public class JPAUtil {

    private static final String PERSISTENCE_UNIT = "MasterAnnoncePU";
    private static EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
            System.out.println("EntityManagerFactory créé avec succès !");
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'EntityManagerFactory : " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Retourne un nouvel EntityManager.
     * Chaque appel crée un nouveau contexte de persistance.
     * L'appelant est responsable de le fermer.
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Ferme l'EntityManagerFactory.
     * À appeler lors de l'arrêt de l'application.
     */
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("EntityManagerFactory fermé.");
        }
    }
}
