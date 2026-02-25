package com.masterannonce.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de création de catégorie.
 */
public record CategoryCreateDTO(
    @NotNull(message = "Le libellé est obligatoire")
    @Size(min = 1, max = 50, message = "Le libellé doit contenir entre 1 et 50 caractères")
    String label
) {}
