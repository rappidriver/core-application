package com.rappidrive.domain.enums;

/**
 * Status de uma notificação no seu ciclo de vida.
 * Define os estados possíveis e transições válidas.
 */
public enum NotificationStatus {
    
    /**
     * Notificação criada, aguardando envio
     */
    PENDING,
    
    /**
     * Notificação enviada com sucesso (push notification)
     */
    SENT,
    
    /**
     * Notificação lida pelo usuário (in-app)
     */
    READ,
    
    /**
     * Falha no envio da notificação
     */
    FAILED;
    
    /**
     * Verifica se notificação está pendente
     */
    public boolean isPending() {
        return this == PENDING;
    }
    
    /**
     * Verifica se notificação foi enviada
     */
    public boolean isSent() {
        return this == SENT;
    }
    
    /**
     * Verifica se notificação foi lida
     */
    public boolean isRead() {
        return this == READ;
    }
    
    /**
     * Verifica se notificação falhou
     */
    public boolean isFailed() {
        return this == FAILED;
    }
    
    /**
     * Verifica se notificação não foi lida (PENDING ou SENT)
     */
    public boolean isUnread() {
        return this == PENDING || this == SENT;
    }
    
    /**
     * Verifica se pode transicionar para novo status
     */
    public boolean canTransitionTo(NotificationStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == SENT || newStatus == FAILED;
            case SENT -> newStatus == READ;
            case FAILED -> newStatus == PENDING; // retry
            case READ -> false; // read é estado final
        };
    }
}
