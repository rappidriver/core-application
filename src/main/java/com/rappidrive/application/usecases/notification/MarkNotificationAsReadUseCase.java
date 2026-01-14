package com.rappidrive.application.usecases.notification;

import com.rappidrive.application.ports.input.notification.MarkNotificationAsReadInputPort;
import com.rappidrive.application.ports.output.NotificationRepositoryPort;
import com.rappidrive.domain.entities.Notification;
import com.rappidrive.domain.exceptions.NotificationNotFoundException;

/**
 * Use case para marcar notificação como lida.
 */
public class MarkNotificationAsReadUseCase implements MarkNotificationAsReadInputPort {
    
    private final NotificationRepositoryPort notificationRepository;
    
    public MarkNotificationAsReadUseCase(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    @Override
    public void execute(MarkAsReadCommand command) {
        Notification notification = notificationRepository
            .findById(command.notificationId())
            .orElseThrow(() -> new NotificationNotFoundException(command.notificationId()));
        
        if (!notification.getUserId().equals(command.userId())) {
            throw new NotificationNotFoundException(
                "Notificação não pertence ao usuário informado"
            );
        }
        
        if (!notification.getTenantId().equals(command.tenantId())) {
            throw new NotificationNotFoundException(
                "Notificação não pertence ao tenant informado"
            );
        }
        
        if (notification.isUnread()) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }
}
