package com.masterannonce.infrastructure.persistence;

import com.masterannonce.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Spring Data JPA pour les Catégories.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByLabel(String label);

    boolean existsByLabel(String label);
}
