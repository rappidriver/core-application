package com.rappidrive.application.ports.input.notification;

import com.rappidrive.domain.entities.Notification;
import com.rappidrive.domain.enums.NotificationStatus;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.UserId;

import java.util.List;

/**
 * Port de entrada para buscar notificações de um usuário.
 */
public interface GetUserNotificationsInputPort {
    
    /**
     * Busca notificações de um usuário com filtros opcionais
     */
    List<Notification> execute(GetUserNotificationsQuery query);
    
    /**
     * Query para buscar notificações
     */
    record GetUserNotificationsQuery(
        UserId userId,
        NotificationStatus status, // null para todas
        TenantId tenantId,
        int page,
        int size
    ) {}
}
