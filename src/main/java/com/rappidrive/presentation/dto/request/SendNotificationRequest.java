package com.rappidrive.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * DTO de request para enviar notificação.
 */
public record SendNotificationRequest(
    
    @NotNull(message = "User ID é obrigatório")
    String userId,
    
    @NotNull(message = "Tipo de notificação é obrigatório")
    NotificationTypeDto type,
    
    @NotBlank(message = "Título é obrigatório")
    @Size(max = 100, message = "Título não pode exceder 100 caracteres")
    String title,
    
    @NotBlank(message = "Mensagem é obrigatória")
    @Size(max = 500, message = "Mensagem não pode exceder 500 caracteres")
    String message,
    
    Map<String, String> data,
    
    String idempotencyKey
) {
    /**
     * Enum espelhando NotificationType para DTOs
     */
    public enum NotificationTypeDto {
        TRIP_REQUEST,
        TRIP_ACCEPTED,
        TRIP_STARTED,
        TRIP_COMPLETED,
        TRIP_CANCELLED,
        PAYMENT_PROCESSED,
        PAYMENT_FAILED,
        RATING_RECEIVED,
        PROMOTION,
        SYSTEM_ALERT
    }
}
