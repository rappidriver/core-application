package com.rappidrive.application.usecases.notification;

import com.rappidrive.application.ports.input.notification.GetUnreadCountInputPort;
import com.rappidrive.application.ports.output.NotificationRepositoryPort;

/**
 * Use case para contar notificações não lidas.
 */
public class GetUnreadCountUseCase implements GetUnreadCountInputPort {
    
    private final NotificationRepositoryPort notificationRepository;
    
    public GetUnreadCountUseCase(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    @Override
    public Long execute(GetUnreadCountQuery query) {
        return notificationRepository.countUnreadByUserId(
            query.userId(),
            query.tenantId()
        );
    }
}
