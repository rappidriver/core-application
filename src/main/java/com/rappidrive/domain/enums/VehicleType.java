package com.rappidrive.domain.enums;

/**
 * Tipos de veículos aceitos na plataforma.
 * Apenas veículos de passeio são permitidos (4 portas).
 */
public enum VehicleType {
    /**
     * Sedan - Veículo de passeio com 4 portas, confortável para 4-5 passageiros
     */
    SEDAN,
    
    /**
     * Hatchback - Veículo compacto com 4 portas, ideal para 4-5 passageiros
     */
    HATCHBACK,
    
    /**
     * SUV - Veículo espaçoso com 4 portas, comporta 5-7 passageiros
     */
    SUV;
    
    /**
     * Verifica se o tipo é um veículo de passeio válido.
     * Todos os tipos neste enum são veículos de passeio.
     * 
     * @return true sempre, pois VAN e PICKUP foram removidos
     */
    public boolean isPassengerVehicle() {
        return true;
    }
}
