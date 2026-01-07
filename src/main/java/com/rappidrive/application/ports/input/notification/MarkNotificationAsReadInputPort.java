package com.rappidrive.application.ports.input.notification;

import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.UserId;

import java.util.UUID;

/**
 * Port de entrada para marcar notificação como lida.
 */
public interface MarkNotificationAsReadInputPort {
    
    /**
     * Marca notificação como lida
     */
    void execute(MarkAsReadCommand command);
    
    /**
     * Comando para marcar como lida
     */
    record MarkAsReadCommand(
        UUID notificationId,
        UserId userId, // Para validação de ownership
        TenantId tenantId
    ) {}
}
