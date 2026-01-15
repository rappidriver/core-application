package com.rappidrive.application.exceptions;

/**
 * Exception thrown when identity provisioning operations fail.
 */
public class IdentityProvisioningException extends ApplicationException {
    
    public IdentityProvisioningException(String message) {
        super(message);
    }
    
    public IdentityProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
