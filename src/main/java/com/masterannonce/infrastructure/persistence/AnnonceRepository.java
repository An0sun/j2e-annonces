package com.masterannonce.infrastructure.persistence;

import com.masterannonce.domain.model.Annonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Spring Data JPA pour les Annonces.
 * Étend JpaSpecificationExecutor pour la recherche dynamique multi-critères.
 */
@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long>, JpaSpecificationExecutor<Annonce> {

    /**
     * Trouve une annonce par ID avec ses relations chargées (author, category).
     * Évite LazyInitializationException.
     */
    @Query("SELECT a FROM Annonce a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.category WHERE a.id = :id")
    Optional<Annonce> findByIdWithRelations(@Param("id") Long id);
}
