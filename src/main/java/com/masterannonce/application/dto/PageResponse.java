package com.masterannonce.application.dto;

import java.util.List;

/**
 * DTO générique de réponse paginée.
 * Encapsule le Page de Spring dans un contrat propre.
 *
 * @param <T> le type des éléments de contenu
 */
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {

    /**
     * Méthode factory pour créer un PageResponse depuis un Page Spring.
     */
    public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> springPage) {
        return new PageResponse<>(
            springPage.getContent(),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements(),
            springPage.getTotalPages(),
            springPage.hasNext(),
            springPage.hasPrevious()
        );
    }
}
