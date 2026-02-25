package com.masterannonce.domain.model;

/**
 * Statut d'une annonce dans son cycle de vie.
 * Transitions autorisées : DRAFT → PUBLISHED → ARCHIVED
 */
public enum AnnonceStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}
