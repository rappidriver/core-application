package com.rappidrive.domain.enums;

/**
 * Tipos de notificações suportadas pelo sistema.
 * Define categorias de eventos que geram notificações para usuários.
 */
public enum NotificationType {
    
    /**
     * Nova solicitação de viagem recebida (enviada para motorista)
     */
    TRIP_REQUEST,
    
    /**
     * Viagem foi aceita por motorista (enviada para passageiro)
     */
    TRIP_ACCEPTED,
    
    /**
     * Viagem foi iniciada (enviada para passageiro)
     */
    TRIP_STARTED,
    
    /**
     * Viagem foi concluída (enviada para ambos)
     */
    TRIP_COMPLETED,
    
    /**
     * Viagem foi cancelada (enviada para ambos)
     */
    TRIP_CANCELLED,
    
    /**
     * Pagamento foi processado com sucesso (enviada para passageiro)
     */
    PAYMENT_PROCESSED,
    
    /**
     * Falha no processamento do pagamento (enviada para passageiro)
     */
    PAYMENT_FAILED,
    
    /**
     * Nova avaliação recebida (enviada para motorista ou passageiro)
     */
    RATING_RECEIVED,
    
    /**
     * Promoção ou oferta disponível (enviada para ambos)
     */
    PROMOTION,
    
    /**
     * Alerta ou aviso do sistema (enviada para ambos)
     */
    SYSTEM_ALERT;
    
    /**
     * Verifica se é notificação relacionada a viagem
     */
    public boolean isTripRelated() {
        return this == TRIP_REQUEST 
            || this == TRIP_ACCEPTED 
            || this == TRIP_STARTED 
            || this == TRIP_COMPLETED 
            || this == TRIP_CANCELLED;
    }
    
    /**
     * Verifica se é notificação relacionada a pagamento
     */
    public boolean isPaymentRelated() {
        return this == PAYMENT_PROCESSED || this == PAYMENT_FAILED;
    }
    
    /**
     * Verifica se é notificação de alta prioridade
     */
    public boolean isHighPriority() {
        return this == TRIP_REQUEST || this == TRIP_CANCELLED || this == PAYMENT_FAILED;
    }
    
    /**
     * Verifica se é notificação de média prioridade
     */
    public boolean isMediumPriority() {
        return this == TRIP_ACCEPTED 
            || this == TRIP_STARTED 
            || this == TRIP_COMPLETED
            || this == PAYMENT_PROCESSED 
            || this == RATING_RECEIVED;
    }
    
    /**
     * Verifica se é notificação de baixa prioridade
     */
    public boolean isLowPriority() {
        return this == PROMOTION || this == SYSTEM_ALERT;
    }
}
