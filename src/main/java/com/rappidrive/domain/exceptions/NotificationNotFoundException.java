package com.rappidrive.domain.exceptions;

import java.util.UUID;

/**
 * Exceção lançada quando uma notificação não é encontrada.
 */
public class NotificationNotFoundException extends DomainException {
    
    public NotificationNotFoundException(UUID notificationId) {
        super("Notificação com ID " + notificationId + " não foi encontrada");
    }
    
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
