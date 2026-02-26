package com.masterannonce.domain.exception;

/**
 * Exception levée quand une ressource est introuvable.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final transient Object resourceId;

    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(resourceName + " introuvable (id=" + resourceId + ")");
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    public String getResourceName() { return resourceName; }
    public Object getResourceId() { return resourceId; }
}
