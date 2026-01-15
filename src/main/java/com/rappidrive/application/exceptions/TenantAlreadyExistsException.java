package com.rappidrive.application.exceptions;

/**
 * Exception thrown when attempting to onboard a tenant that already exists.
 */
public class TenantAlreadyExistsException extends ApplicationException {
    
    public TenantAlreadyExistsException(String tenantId) {
        super("Tenant already exists with ID: " + tenantId);
    }
}
