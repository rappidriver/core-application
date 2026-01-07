package com.rappidrive.presentation.mappers;

import com.rappidrive.application.ports.input.notification.SendNotificationInputPort.SendNotificationCommand;
import com.rappidrive.domain.entities.Notification;
import com.rappidrive.domain.enums.NotificationPriority;
import com.rappidrive.domain.enums.NotificationStatus;
import com.rappidrive.domain.enums.NotificationType;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.UserId;
import com.rappidrive.presentation.dto.request.SendNotificationRequest;
import com.rappidrive.presentation.dto.response.NotificationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Mapper para conversão entre DTOs e domain entities de Notification.
 */
@Component
public class NotificationDtoMapper {
    
    /**
     * Converte SendNotificationRequest para SendNotificationCommand
     */
    public SendNotificationCommand toCommand(SendNotificationRequest request, TenantId tenantId) {
        return new SendNotificationCommand(
            new UserId(UUID.fromString(request.userId())),
            toNotificationType(request.type()),
            request.title(),
            request.message(),
            request.data(),
            request.idempotencyKey(),
            tenantId
        );
    }
    
    /**
     * Converte Notification para NotificationResponse
     */
    public NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
            notification.getId().toString(),
            notification.getUserId().getValue().toString(),
            toNotificationTypeDto(notification.getType()),
            toNotificationPriorityDto(notification.getPriority()),
            notification.getContent().getTitle(),
            notification.getContent().getMessage(),
            notification.getContent().getData(),
            toNotificationStatusDto(notification.getStatus()),
            notification.getCreatedAt(),
            notification.getSentAt(),
            notification.getReadAt()
        );
    }
    
    /**
     * Converte lista de notificações
     */
    public List<NotificationResponse> toNotificationResponseList(List<Notification> notifications) {
        return notifications.stream()
            .map(this::toNotificationResponse)
            .toList();
    }
    
    // Conversões de enum (Request DTO → Domain)
    private NotificationType toNotificationType(SendNotificationRequest.NotificationTypeDto dto) {
        return NotificationType.valueOf(dto.name());
    }
    
    // Conversões de enum (Domain → Response DTO)
    private NotificationResponse.NotificationTypeDto toNotificationTypeDto(NotificationType type) {
        return NotificationResponse.NotificationTypeDto.valueOf(type.name());
    }
    
    private NotificationResponse.NotificationPriorityDto toNotificationPriorityDto(NotificationPriority priority) {
        return NotificationResponse.NotificationPriorityDto.valueOf(priority.name());
    }
    
    private NotificationResponse.NotificationStatusDto toNotificationStatusDto(NotificationStatus status) {
        return NotificationResponse.NotificationStatusDto.valueOf(status.name());
    }
}
