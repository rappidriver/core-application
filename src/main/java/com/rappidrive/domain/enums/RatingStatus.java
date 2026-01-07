package com.rappidrive.domain.enums;

/**
 * Status de uma avaliação.
 */
public enum RatingStatus {
    /**
     * Avaliação ativa e visível.
     */
    ACTIVE,
    
    /**
     * Avaliação reportada como ofensiva/inadequada.
     */
    REPORTED,
    
    /**
     * Avaliação removida (soft-delete).
     */
    DELETED;
    
    /**
     * Verifica se a avaliação está ativa.
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    /**
     * Verifica se a avaliação foi reportada.
     */
    public boolean isReported() {
        return this == REPORTED;
    }
    
    /**
     * Verifica se a avaliação foi deletada.
     */
    public boolean isDeleted() {
        return this == DELETED;
    }
    
    /**
     * Verifica se a avaliação deve ser contabilizada em médias.
     */
    public boolean shouldCountInAverage() {
        return this == ACTIVE;
    }
}
