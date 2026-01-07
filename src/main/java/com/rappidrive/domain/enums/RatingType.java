package com.rappidrive.domain.enums;

/**
 * Tipo de avaliação indicando quem avalia quem.
 * DRIVER_BY_PASSENGER: Passageiro avaliando motorista
 * PASSENGER_BY_DRIVER: Motorista avaliando passageiro
 */
public enum RatingType {
    /**
     * Passageiro avalia motorista após viagem.
     */
    DRIVER_BY_PASSENGER,
    
    /**
     * Motorista avalia passageiro após viagem.
     */
    PASSENGER_BY_DRIVER;
    
    /**
     * Verifica se este tipo é uma avaliação de motorista.
     */
    public boolean isDriverRating() {
        return this == DRIVER_BY_PASSENGER;
    }
    
    /**
     * Verifica se este tipo é uma avaliação de passageiro.
     */
    public boolean isPassengerRating() {
        return this == PASSENGER_BY_DRIVER;
    }
}
