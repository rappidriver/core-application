package com.rappidrive.presentation.mappers;

import com.rappidrive.application.ports.input.vehicle.AssignVehicleToDriverInputPort.AssignVehicleCommand;
import com.rappidrive.application.ports.input.vehicle.CreateVehicleInputPort.CreateVehicleCommand;
import com.rappidrive.application.ports.input.vehicle.UpdateVehicleInputPort.UpdateVehicleCommand;
import com.rappidrive.domain.entities.Vehicle;
import com.rappidrive.domain.valueobjects.LicensePlate;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.VehicleYear;
import com.rappidrive.presentation.dto.request.AssignVehicleRequest;
import com.rappidrive.presentation.dto.request.CreateVehicleRequest;
import com.rappidrive.presentation.dto.request.UpdateVehicleRequest;
import com.rappidrive.presentation.dto.response.VehicleResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper para conversão entre DTOs e Commands/Entities de veículo.
 */
@Component
public class VehicleDtoMapper {
    
    /**
     * Converte CreateVehicleRequest para CreateVehicleCommand.
     */
    public CreateVehicleCommand toCommand(CreateVehicleRequest request) {
        return new CreateVehicleCommand(
            new TenantId(request.tenantId()),
            new LicensePlate(request.licensePlate()),
            request.brand(),
            request.model(),
            new VehicleYear(request.year()),
            request.color(),
            request.type(),
            request.numberOfDoors(),
            request.seats()
        );
    }
    
    /**
     * Converte UpdateVehicleRequest para UpdateVehicleCommand.
     */
    public UpdateVehicleCommand toCommand(UUID vehicleId, UpdateVehicleRequest request) {
        return new UpdateVehicleCommand(
            vehicleId,
            request.color(),
            request.status()
        );
    }
    
    /**
     * Converte AssignVehicleRequest para AssignVehicleCommand.
     */
    public AssignVehicleCommand toCommand(UUID vehicleId, AssignVehicleRequest request) {
        return new AssignVehicleCommand(
            vehicleId,
            request.driverId()
        );
    }
    
    /**
     * Converte Vehicle para VehicleResponse.
     */
    public VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
            vehicle.getId(),
            vehicle.getTenantId().getValue(),
            vehicle.getDriverId(),
            vehicle.getLicensePlate().getFormatted(),
            vehicle.getBrand(),
            vehicle.getModel(),
            vehicle.getYear().getValue(),
            vehicle.getColor(),
            vehicle.getType(),
            vehicle.getNumberOfDoors(),
            vehicle.getSeats(),
            vehicle.getStatus(),
            vehicle.getCreatedAt(),
            vehicle.getUpdatedAt()
        );
    }
}
