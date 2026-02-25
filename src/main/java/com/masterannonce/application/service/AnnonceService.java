package com.masterannonce.application.service;

import com.masterannonce.application.dto.AnnoncePatchDTO;
import com.masterannonce.application.mapper.AnnonceMapper;
import com.masterannonce.domain.exception.BusinessException;
import com.masterannonce.domain.exception.ResourceNotFoundException;
import com.masterannonce.domain.exception.UnauthorizedActionException;
import com.masterannonce.domain.model.Annonce;
import com.masterannonce.domain.model.AnnonceStatus;
import com.masterannonce.domain.model.Category;
import com.masterannonce.domain.model.User;
import com.masterannonce.infrastructure.persistence.AnnonceRepository;
import com.masterannonce.infrastructure.persistence.CategoryRepository;
import com.masterannonce.infrastructure.persistence.UserRepository;
import com.masterannonce.infrastructure.persistence.specifications.AnnonceSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

/**
 * Service métier pour les Annonces.
 * Toute la logique métier et les règles d'autorisation sont centralisées ici
 * (jamais dans le controller).
 */
@Service
@Transactional
public class AnnonceService {

    private final AnnonceRepository annonceRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AnnonceMapper annonceMapper;

    public AnnonceService(AnnonceRepository annonceRepository,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          AnnonceMapper annonceMapper) {
        this.annonceRepository = annonceRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.annonceMapper = annonceMapper;
    }

    // ===== CRUD =====

    /**
     * Créer une nouvelle annonce (statut automatiquement DRAFT).
     */
    public Annonce createAnnonce(Annonce annonce, Long authorId, Long categoryId) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new ResourceNotFoundException("User", authorId));

        annonce.setAuthor(author);
        annonce.setStatus(AnnonceStatus.DRAFT);

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
            annonce.setCategory(category);
        }

        return annonceRepository.save(annonce);
    }

    /**
     * Mise à jour complète d'une annonce (PUT).
     * Vérifie : auteur + statut modifiable.
     */
    public Annonce updateAnnonce(Long id, Annonce updates, Long categoryId, Long userId) {
        Annonce existing = findByIdOrThrow(id);

        // Règles métier centralisées
        checkIsAuthor(existing, userId);
        checkCanBeModified(existing);

        existing.setTitle(updates.getTitle());
        existing.setDescription(updates.getDescription());
        existing.setAddress(updates.getAddress());
        existing.setMail(updates.getMail());
        existing.setVersion(updates.getVersion());

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
            existing.setCategory(category);
        }

        return annonceRepository.save(existing);
    }

    /**
     * Mise à jour partielle d'une annonce (PATCH).
     * Seuls les champs non-null sont appliqués via MapStruct.
     */
    public Annonce patchAnnonce(Long id, AnnoncePatchDTO patch, Long userId) {
        Annonce existing = findByIdOrThrow(id);

        checkIsAuthor(existing, userId);
        checkCanBeModified(existing);

        // MapStruct applique uniquement les champs non-null
        annonceMapper.patchEntity(patch, existing);

        if (patch.getCategoryId() != null) {
            Category category = categoryRepository.findById(patch.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", patch.getCategoryId()));
            existing.setCategory(category);
        }

        return annonceRepository.save(existing);
    }

    /**
     * Supprimer une annonce (doit être archivée d'abord).
     */
    public void deleteAnnonce(Long id, Long userId) {
        Annonce existing = findByIdOrThrow(id);

        checkIsAuthor(existing, userId);

        if (!existing.canBeDeleted()) {
            throw new BusinessException(
                "L'annonce doit être archivée avant d'être supprimée (statut actuel: " + existing.getStatus() + ")");
        }

        annonceRepository.delete(existing);
    }

    // ===== Transitions de statut =====

    /**
     * Publier une annonce (DRAFT → PUBLISHED).
     */
    public Annonce publishAnnonce(Long id, Long userId) {
        Annonce existing = findByIdOrThrow(id);
        checkIsAuthor(existing, userId);

        existing.publish(); // logique dans le domaine
        return annonceRepository.save(existing);
    }

    /**
     * Archiver une annonce (→ ARCHIVED).
     * Seul un ADMIN peut archiver (vérification dans le controller via @PreAuthorize).
     */
    public Annonce archiveAnnonce(Long id) {
        Annonce existing = findByIdOrThrow(id);

        existing.archive(); // logique dans le domaine
        return annonceRepository.save(existing);
    }

    // ===== Lecture =====

    @Transactional(readOnly = true)
    public Annonce getAnnonceById(Long id) {
        return annonceRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", id));
    }

    /**
     * Recherche paginée et dynamique multi-critères via Specifications.
     */
    @Transactional(readOnly = true)
    public Page<Annonce> searchAnnonces(String keyword, AnnonceStatus status,
                                         Long categoryId, Long authorId,
                                         Timestamp fromDate, Timestamp toDate,
                                         Pageable pageable) {
        Specification<Annonce> spec = Specification.where(null);

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(AnnonceSpecifications.titleOrDescriptionContains(keyword));
        }
        if (status != null) {
            spec = spec.and(AnnonceSpecifications.hasStatus(status));
        }
        if (categoryId != null) {
            spec = spec.and(AnnonceSpecifications.hasCategoryId(categoryId));
        }
        if (authorId != null) {
            spec = spec.and(AnnonceSpecifications.hasAuthorId(authorId));
        }
        if (fromDate != null) {
            spec = spec.and(AnnonceSpecifications.createdAfter(fromDate));
        }
        if (toDate != null) {
            spec = spec.and(AnnonceSpecifications.createdBefore(toDate));
        }

        return annonceRepository.findAll(spec, pageable);
    }

    // ===== Helpers privés (règles métier centralisées) =====

    private Annonce findByIdOrThrow(Long id) {
        return annonceRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", id));
    }

    private void checkIsAuthor(Annonce annonce, Long userId) {
        if (!annonce.isAuthor(userId)) {
            throw new UnauthorizedActionException("Seul l'auteur peut effectuer cette action");
        }
    }

    private void checkCanBeModified(Annonce annonce) {
        if (!annonce.canBeModified()) {
            throw new BusinessException("Une annonce publiée ne peut plus être modifiée");
        }
    }
}
