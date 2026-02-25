package com.masterannonce.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO d'inscription.
 */
public record RegisterRequest(
    @NotNull(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    String username,

    @NotNull(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    String email,

    @NotNull(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
             message = "Le mot de passe doit contenir au moins 1 majuscule et 1 chiffre")
    String password
) {}
