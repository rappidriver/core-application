package com.rappidrive.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de response para notificação.
 */
public record NotificationResponse(
    String id,
    String userId,
    NotificationTypeDto type,
    NotificationPriorityDto priority,
    String title,
    String message,
    Map<String, String> data,
    NotificationStatusDto status,
    LocalDateTime createdAt,
    LocalDateTime sentAt,
    LocalDateTime readAt
) {
    /**
     * Enum para tipo de notificação
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
    
    /**
     * Enum para prioridade
     */
    public enum NotificationPriorityDto {
        HIGH,
        MEDIUM,
        LOW
    }
    
    /**
     * Enum para status
     */
    public enum NotificationStatusDto {
        PENDING,
        SENT,
        READ,
        FAILED
    }
}
