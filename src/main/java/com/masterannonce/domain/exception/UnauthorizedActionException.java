package com.masterannonce.domain.exception;

/**
 * Exception levée quand un utilisateur tente une action non autorisée.
 */
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}
