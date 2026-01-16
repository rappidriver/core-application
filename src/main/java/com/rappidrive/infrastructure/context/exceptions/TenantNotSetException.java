package com.rappidrive.infrastructure.context.exceptions;

/**
 * Exception thrown when attempting to access TenantContext without a tenant being set.
 * This typically indicates a programming error where tenant resolution was not executed
 * or the context was cleared prematurely.
 */
public class TenantNotSetException extends RuntimeException {
    
    public TenantNotSetException(String message) {
        super(message);
    }
    
    public TenantNotSetException(String message, Throwable cause) {
        super(message, cause);
    }
}
