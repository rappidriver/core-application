package com.rappidrive.application.ports.input.notification;

import com.rappidrive.domain.entities.Notification;
import com.rappidrive.domain.enums.NotificationType;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.UserId;

import java.util.Map;

/**
 * Port de entrada para enviar notificações.
 */
public interface SendNotificationInputPort {
    
    /**
     * Envia notificação para usuário
     */
    Notification execute(SendNotificationCommand command);
    
    /**
     * Comando para enviar notificação
     */
    record SendNotificationCommand(
        UserId userId,
        NotificationType type,
        String title,
        String message,
        Map<String, String> data,
        String idempotencyKey,
        TenantId tenantId
    ) {}
}
