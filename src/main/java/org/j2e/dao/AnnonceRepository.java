package org.j2e.dao;

import org.j2e.bean.Annonce;
import org.j2e.bean.AnnonceStatus;
import org.j2e.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Repository JPA pour les Annonces.
 * Utilise exclusivement JPQL (pas de JDBC).
 */
public class AnnonceRepository {

    // ===== CRUD =====

    public void save(Annonce annonce) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (annonce.getId() == null) {
                em.persist(annonce);
            } else {
                em.merge(annonce);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Annonce.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Trouve une annonce par ID avec ses relations chargées (author, category).
     * Évite LazyInitializationException quand on accède aux relations hors session.
     */
    public Annonce findByIdWithRelations(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                "SELECT a FROM Annonce a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.category WHERE a.id = :id",
                Annonce.class
            );
            query.setParameter("id", id);
            List<Annonce> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public List<Annonce> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT a FROM Annonce a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.category ORDER BY a.date DESC",
                Annonce.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Annonce annonce = em.find(Annonce.class, id);
            if (annonce != null) {
                em.remove(annonce);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ===== Recherche & Filtrage =====

    /**
     * Recherche par mot-clé dans le titre ou la description.
     */
    public List<Annonce> searchByKeyword(String keyword) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                "SELECT a FROM Annonce a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.category " +
                "WHERE LOWER(a.title) LIKE LOWER(:kw) OR LOWER(a.description) LIKE LOWER(:kw) " +
                "ORDER BY a.date DESC",
                Annonce.class
            );
            query.setParameter("kw", "%" + keyword + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Filtrage par catégorie et/ou statut.
     */
    public List<Annonce> findByCategoryAndStatus(Long categoryId, AnnonceStatus status) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                "SELECT a FROM Annonce a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.category WHERE 1=1"
            );
            if (categoryId != null) {
                jpql.append(" AND a.category.id = :catId");
            }
            if (status != null) {
                jpql.append(" AND a.status = :status");
            }
            jpql.append(" ORDER BY a.date DESC");

            TypedQuery<Annonce> query = em.createQuery(jpql.toString(), Annonce.class);
            if (categoryId != null) query.setParameter("catId", categoryId);
            if (status != null) query.setParameter("status", status);

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // ===== Pagination =====

    /**
     * Liste paginée des annonces.
     * @param page numéro de page (commence à 1)
     * @param size nombre d'éléments par page
     */
    public List<Annonce> findPaginated(int page, int size) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Annonce> query = em.createQuery(
                "SELECT a FROM Annonce a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.category ORDER BY a.date DESC",
                Annonce.class
            );
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Nombre total d'annonces.
     */
    public long count() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(a) FROM Annonce a", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }
}
