package com.masterannonce.application.dto;

/**
 * DTO de réponse après connexion réussie.
 */
public record LoginResponse(
    String token,
    String refreshToken,
    String username,
    Long userId,
    String role
) {}
