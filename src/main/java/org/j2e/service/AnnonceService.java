package org.j2e.service;

import org.j2e.bean.Annonce;
import org.j2e.bean.AnnonceStatus;
import org.j2e.bean.Category;
import org.j2e.bean.User;
import org.j2e.dao.AnnonceRepository;
import org.j2e.dao.CategoryRepository;
import org.j2e.dao.UserRepository;
import java.sql.Timestamp;
import java.util.List;

/**
 * Service métier pour les Annonces.
 * Les transactions sont gérées ici (jamais dans les Servlets).
 */
public class AnnonceService {

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
        User author = userRepository.findById(authorId);
        if (author == null) {
            throw new IllegalArgumentException("Utilisateur introuvable (id=" + authorId + ")");
        }

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId);
            if (category == null) {
                throw new IllegalArgumentException("Catégorie introuvable (id=" + categoryId + ")");
            }
            annonce.setCategory(category);
        }

        annonce.setAuthor(author);
        annonce.setStatus(AnnonceStatus.DRAFT);
        annonce.setDate(new Timestamp(System.currentTimeMillis()));

        annonceRepository.save(annonce);
    }

    /**
     * Modifier une annonce existante.
     */
    public void updateAnnonce(Annonce annonce, Long categoryId) {
        // En JPA avec transaction, un objet récupéré via find() est "géré".
        // Les modifications dessus sont automatiquement persistées au commit.
        // MAIS ici on veut passer par le Repository.
        // Le Repository findById ferme l'EM, donc l'objet est détaché.
        // On doit le modifier puis appeler save() (qui fera un merge).
        
        Annonce existing = annonceRepository.findById(annonce.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Annonce introuvable (id=" + annonce.getId() + ")");
        }

        existing.setTitle(annonce.getTitle());
        existing.setDescription(annonce.getDescription());
        existing.setAdress(annonce.getAdress());
        existing.setMail(annonce.getMail());

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId);
            if (category == null) {
                // On pourrait throw ou ignorer.
                 throw new IllegalArgumentException("Catégorie introuvable (id=" + categoryId + ")");
            }
            existing.setCategory(category);
        }

        annonceRepository.save(existing);
    }

    /**
     * Publier une annonce (DRAFT → PUBLISHED).
     */
    public void publishAnnonce(Long id) {
        Annonce annonce = annonceRepository.findById(id);
        if (annonce == null) {
            throw new IllegalArgumentException("Annonce introuvable");
        }
        annonce.setStatus(AnnonceStatus.PUBLISHED);
        annonceRepository.save(annonce);
    }

    /**
     * Archiver une annonce (→ ARCHIVED).
     */
    public void archiveAnnonce(Long id) {
        Annonce annonce = annonceRepository.findById(id);
        if (annonce == null) {
            throw new IllegalArgumentException("Annonce introuvable");
        }
        annonce.setStatus(AnnonceStatus.ARCHIVED);
        annonceRepository.save(annonce);
    }

    /**
     * Supprimer une annonce.
     */
    public void deleteAnnonce(Long id) {
        annonceRepository.delete(id);
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
