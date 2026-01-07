package com.rappidrive.application.ports.input.notification;

import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.UserId;

/**
 * Port de entrada para contar notificações não lidas.
 */
public interface GetUnreadCountInputPort {
    
    /**
     * Retorna quantidade de notificações não lidas de um usuário
     */
    Long execute(GetUnreadCountQuery query);
    
    /**
     * Query para contar não lidas
     */
    record GetUnreadCountQuery(
        UserId userId,
        TenantId tenantId
    ) {}
}
