package com.rappidrive.application.usecases.notification;

import com.rappidrive.application.ports.input.notification.SendNotificationInputPort;
import com.rappidrive.application.ports.output.NotificationRepositoryPort;
import com.rappidrive.application.ports.output.NotificationServicePort;
import com.rappidrive.domain.entities.Notification;
import com.rappidrive.domain.valueobjects.NotificationContent;

import java.util.Optional;

/**
 * Use case para enviar notificações com idempotência.
 */
public class SendNotificationUseCase implements SendNotificationInputPort {
    
    private final NotificationRepositoryPort notificationRepository;
    private final NotificationServicePort notificationService;
    
    public SendNotificationUseCase(
            NotificationRepositoryPort notificationRepository,
            NotificationServicePort notificationService) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }
    
    @Override
    public Notification execute(SendNotificationCommand command) {
        // 1. Verificar idempotência (evitar duplicatas)
        if (command.idempotencyKey() != null && !command.idempotencyKey().isBlank()) {
            Optional<Notification> existing = notificationRepository
                .findByIdempotencyKey(command.idempotencyKey(), command.tenantId());
            
            if (existing.isPresent()) {
                // Log: "Duplicate notification prevented for key: " + command.idempotencyKey()
                return existing.get();
            }
        }
        
        // 2. Criar conteúdo da notificação
        NotificationContent content = NotificationContent.of(
            command.title(),
            command.message(),
            command.data()
        );
        
        // 3. Criar entidade Notification
        Notification notification = Notification.create(
            command.userId(),
            command.type(),
            content,
            command.tenantId(),
            command.idempotencyKey()
        );
        
        // 4. Salvar no repositório
        Notification savedNotification = notificationRepository.save(notification);
        
        // 5. Enviar push notification (se prioridade permite)
        if (savedNotification.getPriority().shouldSendPush() && notificationService.isAvailable()) {
            boolean sent = notificationService.sendPushNotification(savedNotification);
            if (sent) {
                savedNotification.markAsSent();
                notificationRepository.save(savedNotification);
            } else {
                savedNotification.markAsFailed();
                notificationRepository.save(savedNotification);
            }
        }
        
        return savedNotification;
    }
}
