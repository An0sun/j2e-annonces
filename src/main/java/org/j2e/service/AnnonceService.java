package org.j2e.service;

import org.j2e.bean.Annonce;
import org.j2e.bean.AnnonceStatus;
import org.j2e.bean.Category;
import org.j2e.bean.User;
import org.j2e.dao.AnnonceRepository;
import org.j2e.dao.CategoryRepository;
import org.j2e.dao.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;

/**
 * Service métier pour les Annonces.
 * Les transactions sont gérées ici (jamais dans les Servlets).
 */
public class AnnonceService {

    private static final Logger log = LoggerFactory.getLogger(AnnonceService.class);
    
    private AnnonceRepository annonceRepository;
    private UserRepository userRepository;
    private CategoryRepository categoryRepository;

    public AnnonceService() {
        this.annonceRepository = new AnnonceRepository();
        this.userRepository = new UserRepository();
        this.categoryRepository = new CategoryRepository();
    }

    public AnnonceService(AnnonceRepository annonceRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.annonceRepository = annonceRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Créer une nouvelle annonce.
     * Le statut est automatiquement mis à DRAFT.
     */
    public void createAnnonce(Annonce annonce, Long authorId, Long categoryId) {
        log.info("Création d'annonce: title='{}', authorId={}, categoryId={}", annonce.getTitle(), authorId, categoryId);

        User author = userRepository.findById(authorId);
        if (author == null) {
            log.error("Utilisateur introuvable: id={}", authorId);
            throw new IllegalArgumentException("Utilisateur introuvable (id=" + authorId + ")");
        }

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId);
            if (category == null) {
                log.error("Catégorie introuvable: id={}", categoryId);
                throw new IllegalArgumentException("Catégorie introuvable (id=" + categoryId + ")");
            }
            annonce.setCategory(category);
        }

        annonce.setAuthor(author);
        annonce.setStatus(AnnonceStatus.DRAFT);
        annonce.setDate(new Timestamp(System.currentTimeMillis()));

        annonceRepository.save(annonce);
        log.info("Annonce créée avec succès: id={}", annonce.getId());
    }

    /**
     * Modifier une annonce existante.
     */
    public void updateAnnonce(Annonce annonce, Long categoryId) {
        log.info("Mise à jour de l'annonce: id={}, categoryId={}", annonce.getId(), categoryId);

        Annonce existing = annonceRepository.findById(annonce.getId());
        if (existing == null) {
            log.warn("Annonce introuvable pour modification: id={}", annonce.getId());
            throw new IllegalArgumentException("Annonce introuvable (id=" + annonce.getId() + ")");
        }

        existing.setTitle(annonce.getTitle());
        existing.setDescription(annonce.getDescription());
        existing.setAdress(annonce.getAdress());
        existing.setMail(annonce.getMail());

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId);
            if (category == null) {
                 log.warn("Catégorie introuvable lors de l'update: id={}", categoryId);
                 throw new IllegalArgumentException("Catégorie introuvable (id=" + categoryId + ")");
            }
            existing.setCategory(category);
        }

        annonceRepository.save(existing);
        log.debug("Annonce mise à jour avec succès: id={}", annonce.getId());
    }

    /**
     * Publier une annonce (DRAFT → PUBLISHED).
     */
    public void publishAnnonce(Long id) {
        log.info("Publication de l'annonce: id={}", id);
        Annonce annonce = annonceRepository.findById(id);
        if (annonce == null) {
            log.warn("Annonce introuvable pour publication: id={}", id);
            throw new IllegalArgumentException("Annonce introuvable");
        }
        annonce.setStatus(AnnonceStatus.PUBLISHED);
        annonceRepository.save(annonce);
        log.info("Annonce publiée: id={}, status=PUBLISHED", id);
    }

    /**
     * Archiver une annonce (→ ARCHIVED).
     */
    public void archiveAnnonce(Long id) {
        log.info("Archivage de l'annonce: id={}", id);
        Annonce annonce = annonceRepository.findById(id);
        if (annonce == null) {
            log.warn("Annonce introuvable pour archivage: id={}", id);
            throw new IllegalArgumentException("Annonce introuvable");
        }
        annonce.setStatus(AnnonceStatus.ARCHIVED);
        annonceRepository.save(annonce);
        log.info("Annonce archivée: id={}, status=ARCHIVED", id);
    }

    /**
     * Supprimer une annonce.
     */
    public void deleteAnnonce(Long id) {
        log.info("Suppression de l'annonce: id={}", id);
        annonceRepository.delete(id);
        log.info("Annonce supprimée: id={}", id);
    }

    /**
     * Récupérer une annonce par ID (avec relations chargées).
     */
    public Annonce getAnnonceById(Long id) {
        log.debug("Récupération annonce: id={}", id);
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
        log.debug("Listing paginé: page={}, size={}", page, size);
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
        log.debug("Recherche annonces: keyword='{}'", keyword);
        return annonceRepository.searchByKeyword(keyword);
    }

    /**
     * Filtrage par catégorie et statut.
     */
    public List<Annonce> filter(Long categoryId, AnnonceStatus status) {
        return annonceRepository.findByCategoryAndStatus(categoryId, status);
    }
}
