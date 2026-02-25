package com.masterannonce.application.dto;

import java.sql.Timestamp;

/**
 * DTO de réponse pour une Annonce (lecture).
 */
public record AnnonceDTO(
    Long id,
    String title,
    String description,
    String address,
    String mail,
    Timestamp createdAt,
    String status,
    Long authorId,
    String authorUsername,
    Long categoryId,
    String categoryLabel,
    Long version
) {}
