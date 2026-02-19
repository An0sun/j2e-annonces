package org.j2e.rest.exception;

/**
 * Exception pour les ressources introuvables (404 Not Found).
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String entityName, Long id) {
        super(entityName + " introuvable (id=" + id + ")");
    }
}
