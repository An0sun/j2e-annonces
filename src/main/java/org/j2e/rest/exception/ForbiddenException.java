package org.j2e.rest.exception;

/**
 * Exception pour les accès non autorisés (403 Forbidden).
 * Ex: tentative de modification par un non-auteur.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
