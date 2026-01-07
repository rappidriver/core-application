package com.rappidrive.domain.enums;

/**
 * Prioridade de uma notificação, determinando urgência e forma de entrega.
 */
public enum NotificationPriority {
    
    /**
     * Alta prioridade - Crítica, exige ação imediata.
     * Push + som + vibração + in-app
     */
    HIGH,
    
    /**
     * Média prioridade - Importante, ação recomendada.
     * Push + in-app
     */
    MEDIUM,
    
    /**
     * Baixa prioridade - Informativa, sem urgência.
     * Apenas in-app (sem push)
     */
    LOW;
    
    /**
     * Verifica se é alta prioridade
     */
    public boolean isHigh() {
        return this == HIGH;
    }
    
    /**
     * Verifica se é média prioridade
     */
    public boolean isMedium() {
        return this == MEDIUM;
    }
    
    /**
     * Verifica se é baixa prioridade
     */
    public boolean isLow() {
        return this == LOW;
    }
    
    /**
     * Verifica se deve enviar push notification
     */
    public boolean shouldSendPush() {
        return this == HIGH || this == MEDIUM;
    }
    
    /**
     * Determina prioridade com base no tipo de notificação
     */
    public static NotificationPriority fromType(NotificationType type) {
        if (type.isHighPriority()) {
            return HIGH;
        } else if (type.isMediumPriority()) {
            return MEDIUM;
        } else {
            return LOW;
        }
    }
}
