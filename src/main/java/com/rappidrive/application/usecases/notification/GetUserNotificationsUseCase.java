package com.rappidrive.application.usecases.notification;

import com.rappidrive.application.ports.input.notification.GetUserNotificationsInputPort;
import com.rappidrive.application.ports.output.NotificationRepositoryPort;
import com.rappidrive.domain.entities.Notification;

import java.util.List;

public class GetUserNotificationsUseCase implements GetUserNotificationsInputPort {
    
    private final NotificationRepositoryPort notificationRepository;
    
    public GetUserNotificationsUseCase(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    @Override
    public List<Notification> execute(GetUserNotificationsQuery query) {
        // Se status fornecido, filtrar por status
        if (query.status() != null) {
            return notificationRepository.findByUserIdAndStatus(
                query.userId(),
                query.status(),
                query.tenantId(),
                query.page(),
                query.size()
            );
        }
        
        // Caso contrário, retornar todas
        return notificationRepository.findByUserId(
            query.userId(),
            query.tenantId(),
            query.page(),
            query.size()
        );
    }
}
