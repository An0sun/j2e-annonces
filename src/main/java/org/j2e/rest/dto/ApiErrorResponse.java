package org.j2e.rest.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Format JSON normalisé pour toutes les réponses d'erreur de l'API.
 */
public class ApiErrorResponse {

    private int status;
    private String error;
    private String message;
    private List<String> details;
    private String timestamp;

    public ApiErrorResponse() {
        this.timestamp = LocalDateTime.now().toString();
    }

    public ApiErrorResponse(int status, String error, String message) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = Collections.emptyList();
    }

    public ApiErrorResponse(int status, String error, String message, List<String> details) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = details;
    }

    // Getters & Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
