package com.masterannonce.application.dto;

import java.util.List;

/**
 * Generic paginated response DTO.
 * Wraps Spring's Page into a clean, framework-agnostic contract.
 *
 * @param <T> the type of content items
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
     * Factory method to create a PageResponse from a Spring Page.
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
