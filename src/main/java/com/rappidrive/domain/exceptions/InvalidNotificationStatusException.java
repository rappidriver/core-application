package com.rappidrive.domain.exceptions;

/**
 * Exceção lançada quando ocorre tentativa de transição inválida de status de notificação.
 */
public class InvalidNotificationStatusException extends DomainException {
    
    public InvalidNotificationStatusException(String message) {
        super(message);
    }
    
    public InvalidNotificationStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
