package org.j2e.rest.exception;

/**
 * Exception métier pour les conflits (409 Conflict).
 * Ex: tentative de suppression sans archivage, modification d'une annonce
 * publiée.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
