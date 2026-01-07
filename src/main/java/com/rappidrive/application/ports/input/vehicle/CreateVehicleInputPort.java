package com.rappidrive.application.ports.input.vehicle;

import com.rappidrive.domain.entities.Vehicle;
import com.rappidrive.domain.enums.VehicleType;
import com.rappidrive.domain.valueobjects.LicensePlate;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.VehicleYear;

/**
 * Port (interface) para criação de veículos.
 */
public interface CreateVehicleInputPort {
    
    /**
     * Cria um novo veículo.
     * 
     * @param command dados do veículo a ser criado
     * @return veículo criado
     */
    Vehicle execute(CreateVehicleCommand command);
    
    /**
     * Command record para criação de veículo.
     * Veículo é criado com status INACTIVE e sem motorista associado.
     */
    record CreateVehicleCommand(
        TenantId tenantId,
        LicensePlate licensePlate,
        String brand,
        String model,
        VehicleYear year,
        String color,
        VehicleType type,
        int numberOfDoors,
        int seats
    ) {
        public CreateVehicleCommand {
            if (tenantId == null) {
                throw new IllegalArgumentException("Tenant ID cannot be null");
            }
            if (licensePlate == null) {
                throw new IllegalArgumentException("License plate cannot be null");
            }
            if (brand == null || brand.isBlank()) {
                throw new IllegalArgumentException("Brand cannot be null or empty");
            }
            if (model == null || model.isBlank()) {
                throw new IllegalArgumentException("Model cannot be null or empty");
            }
            if (year == null) {
                throw new IllegalArgumentException("Year cannot be null");
            }
            if (color == null || color.isBlank()) {
                throw new IllegalArgumentException("Color cannot be null or empty");
            }
            if (type == null) {
                throw new IllegalArgumentException("Type cannot be null");
            }
            if (numberOfDoors != 4) {
                throw new IllegalArgumentException("Number of doors must be 4");
            }
            if (seats < 1 || seats > 12) {
                throw new IllegalArgumentException("Seats must be between 1 and 12");
            }
        }
    }
}
