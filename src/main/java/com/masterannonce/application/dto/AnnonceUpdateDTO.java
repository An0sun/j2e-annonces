package com.masterannonce.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de mise à jour complète d'une annonce (PUT).
 */
public record AnnonceUpdateDTO(
    @NotNull(message = "Le titre est obligatoire")
    @Size(min = 1, max = 64, message = "Le titre doit contenir entre 1 et 64 caractères")
    String title,

    @Size(max = 256, message = "La description ne peut pas dépasser 256 caractères")
    String description,

    @Size(max = 64, message = "L'adresse ne peut pas dépasser 64 caractères")
    String address,

    @Email(message = "L'email doit être valide")
    @Size(max = 64, message = "L'email ne peut pas dépasser 64 caractères")
    String mail,

    Long categoryId,

    Long version
) {}
