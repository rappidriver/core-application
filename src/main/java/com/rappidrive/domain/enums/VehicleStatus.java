package com.rappidrive.domain.enums;

/**
 * Status possíveis de um veículo na plataforma.
 */
public enum VehicleStatus {
    /**
     * Veículo ativo e disponível para aceitar corridas
     */
    ACTIVE,
    
    /**
     * Veículo cadastrado mas não disponível para corridas
     */
    INACTIVE,
    
    /**
     * Veículo em manutenção, temporariamente indisponível
     */
    MAINTENANCE;
    
    /**
     * Verifica se o veículo pode aceitar corridas neste status.
     * 
     * @return true se status for ACTIVE
     */
    public boolean canAcceptRides() {
        return this == ACTIVE;
    }
}
