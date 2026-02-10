package org.j2e.service;

import org.j2e.bean.Annonce;
import org.j2e.bean.AnnonceStatus;
import org.j2e.bean.Category;
import org.j2e.bean.User;
import org.j2e.dao.AnnonceRepository;
import org.j2e.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;

/**
 * Service métier pour les Annonces.
 * Les transactions sont gérées ici (jamais dans les Servlets).
 */
public class AnnonceService {

    private final AnnonceRepository annonceRepository = new AnnonceRepository();

    /**
     * Créer une nouvelle annonce.
     * Le statut est automatiquement mis à DRAFT.
     */
    public void createAnnonce(Annonce annonce, Long authorId, Long categoryId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Charger les entités liées dans le même contexte
            User author = em.find(User.class, authorId);
            if (author == null) {
                throw new IllegalArgumentException("Utilisateur introuvable (id=" + authorId + ")");
            }

            if (categoryId != null) {
                Category category = em.find(Category.class, categoryId);
                if (category == null) {
                    throw new IllegalArgumentException("Catégorie introuvable (id=" + categoryId + ")");
                }
                annonce.setCategory(category);
            }

            annonce.setAuthor(author);
            annonce.setStatus(AnnonceStatus.DRAFT);
            annonce.setDate(new Timestamp(System.currentTimeMillis()));

            em.persist(annonce);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Modifier une annonce existante.
     */
    public void updateAnnonce(Annonce annonce, Long categoryId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Annonce existing = em.find(Annonce.class, annonce.getId());
            if (existing == null) {
                throw new IllegalArgumentException("Annonce introuvable (id=" + annonce.getId() + ")");
            }

            existing.setTitle(annonce.getTitle());
            existing.setDescription(annonce.getDescription());
            existing.setAdress(annonce.getAdress());
            existing.setMail(annonce.getMail());

            if (categoryId != null) {
                Category category = em.find(Category.class, categoryId);
                existing.setCategory(category);
            }

            // merge est implicite car existing est managed
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Publier une annonce (DRAFT → PUBLISHED).
     */
    public void publishAnnonce(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Annonce annonce = em.find(Annonce.class, id);
            if (annonce == null) {
                throw new IllegalArgumentException("Annonce introuvable");
            }
            annonce.setStatus(AnnonceStatus.PUBLISHED);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Archiver une annonce (→ ARCHIVED).
     */
    public void archiveAnnonce(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Annonce annonce = em.find(Annonce.class, id);
            if (annonce == null) {
                throw new IllegalArgumentException("Annonce introuvable");
            }
            annonce.setStatus(AnnonceStatus.ARCHIVED);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Supprimer une annonce.
     */
    public void deleteAnnonce(Long id) {
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

    /**
     * Récupérer une annonce par ID (avec relations chargées).
     */
    public Annonce getAnnonceById(Long id) {
        return annonceRepository.findByIdWithRelations(id);
    }

    /**
     * Lister toutes les annonces.
     */
    public List<Annonce> getAllAnnonces() {
        return annonceRepository.findAll();
    }

    /**
     * Listing paginé.
     * @return liste d'annonces pour la page demandée
     */
    public List<Annonce> listPaginated(int page, int size) {
        return annonceRepository.findPaginated(page, size);
    }

    /**
     * Nombre total d'annonces (pour calculer le nombre de pages).
     */
    public long countAnnonces() {
        return annonceRepository.count();
    }

    /**
     * Recherche par mot-clé.
     */
    public List<Annonce> search(String keyword) {
        return annonceRepository.searchByKeyword(keyword);
    }

    /**
     * Filtrage par catégorie et statut.
     */
    public List<Annonce> filter(Long categoryId, AnnonceStatus status) {
        return annonceRepository.findByCategoryAndStatus(categoryId, status);
    }
}
