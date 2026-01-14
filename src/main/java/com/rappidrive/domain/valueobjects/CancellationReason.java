package com.rappidrive.domain.valueobjects;

public enum CancellationReason {
    PASSENGER_CHANGE_OF_PLANS("Mudança de planos"),
    PASSENGER_PRICE_TOO_HIGH("Preço muito alto"),
    PASSENGER_WAIT_TOO_LONG("Tempo de espera muito longo"),
    PASSENGER_WRONG_LOCATION("Localização incorreta"),
    PASSENGER_OTHER("Outro motivo"),
    
    DRIVER_PASSENGER_NOT_FOUND("Passageiro não encontrado"),
    DRIVER_UNSAFE_LOCATION("Localização insegura"),
    DRIVER_VEHICLE_ISSUE("Problema com veículo"),
    DRIVER_OTHER("Outro motivo"),
    
    SYSTEM_AUTO_REMEDIATION("Cancelamento automático por timeout");

    private final String description;

    CancellationReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPassengerReason() {
        return name().startsWith("PASSENGER_");
    }

    public boolean isDriverReason() {
        return name().startsWith("DRIVER_");
    }
}
