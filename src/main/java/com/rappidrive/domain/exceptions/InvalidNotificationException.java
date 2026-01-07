package com.rappidrive.domain.exceptions;

/**
 * Exceção lançada quando uma notificação possui dados inválidos.
 */
public class InvalidNotificationException extends DomainException {
    
    public InvalidNotificationException(String message) {
        super(message);
    }
    
    public InvalidNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
