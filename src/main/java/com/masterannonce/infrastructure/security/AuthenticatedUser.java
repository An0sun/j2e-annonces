package com.masterannonce.infrastructure.security;

/**
 * Objet représentant l'utilisateur authentifié dans le SecurityContext.
 * Sert de principal pour l'authentification JWT.
 */
public record AuthenticatedUser(
    Long userId,
    String username,
    String role
) {}
