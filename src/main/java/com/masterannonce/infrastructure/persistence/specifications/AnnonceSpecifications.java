package com.masterannonce.infrastructure.persistence.specifications;

import com.masterannonce.domain.model.Annonce;
import com.masterannonce.domain.model.AnnonceStatus;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Timestamp;

/**
 * Spécifications JPA pour la recherche dynamique multi-critères sur les Annonces.
 * Chaque méthode retourne un Specification composable via .and() / .or().
 */
public final class AnnonceSpecifications {

    private AnnonceSpecifications() {} // classe utilitaire

    /**
     * Recherche par mot-clé dans le titre ou la description (LIKE, insensible à la casse).
     */
    public static Specification<Annonce> titleOrDescriptionContains(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Filtre par statut.
     */
    public static Specification<Annonce> hasStatus(AnnonceStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /**
     * Filtre par catégorie.
     */
    public static Specification<Annonce> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Filtre par auteur.
     */
    public static Specification<Annonce> hasAuthorId(Long authorId) {
        return (root, query, cb) -> cb.equal(root.get("author").get("id"), authorId);
    }

    /**
     * Filtre par date de création (après fromDate incluse).
     */
    public static Specification<Annonce> createdAfter(Timestamp fromDate) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    /**
     * Filtre par date de création (avant toDate incluse).
     */
    public static Specification<Annonce> createdBefore(Timestamp toDate) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
    }
}
