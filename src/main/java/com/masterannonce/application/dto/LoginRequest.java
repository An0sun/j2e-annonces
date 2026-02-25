package com.masterannonce.application.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de requête de connexion.
 */
public record LoginRequest(
    @NotNull(message = "Le nom d'utilisateur est obligatoire")
    String username,

    @NotNull(message = "Le mot de passe est obligatoire")
    String password
) {}
