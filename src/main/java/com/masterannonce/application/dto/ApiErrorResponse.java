package com.masterannonce.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Réponse d'erreur normalisée pour toutes les exceptions.
 */
public record ApiErrorResponse(
    int status,
    String error,
    String message,
    List<String> details,
    LocalDateTime timestamp
) {
    public ApiErrorResponse(int status, String error, String message) {
        this(status, error, message, List.of(), LocalDateTime.now());
    }

    public ApiErrorResponse(int status, String error, String message, List<String> details) {
        this(status, error, message, details, LocalDateTime.now());
    }
}
