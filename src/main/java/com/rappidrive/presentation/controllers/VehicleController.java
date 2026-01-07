package com.rappidrive.presentation.controllers;

import com.rappidrive.application.ports.input.vehicle.*;
import com.rappidrive.domain.entities.Vehicle;
import com.rappidrive.presentation.dto.request.AssignVehicleRequest;
import com.rappidrive.presentation.dto.request.CreateVehicleRequest;
import com.rappidrive.presentation.dto.request.UpdateVehicleRequest;
import com.rappidrive.presentation.dto.response.VehicleResponse;
import com.rappidrive.presentation.mappers.VehicleDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller para gerenciamento de veículos.
 */
@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {
    
    private final CreateVehicleInputPort createVehicleUseCase;
    private final GetVehicleInputPort getVehicleUseCase;
    private final UpdateVehicleInputPort updateVehicleUseCase;
    private final AssignVehicleToDriverInputPort assignVehicleToDriverUseCase;
    private final ActivateVehicleInputPort activateVehicleUseCase;
    private final VehicleDtoMapper mapper;
    
    public VehicleController(CreateVehicleInputPort createVehicleUseCase,
                            GetVehicleInputPort getVehicleUseCase,
                            UpdateVehicleInputPort updateVehicleUseCase,
                            AssignVehicleToDriverInputPort assignVehicleToDriverUseCase,
                            ActivateVehicleInputPort activateVehicleUseCase,
                            VehicleDtoMapper mapper) {
        this.createVehicleUseCase = createVehicleUseCase;
        this.getVehicleUseCase = getVehicleUseCase;
        this.updateVehicleUseCase = updateVehicleUseCase;
        this.assignVehicleToDriverUseCase = assignVehicleToDriverUseCase;
        this.activateVehicleUseCase = activateVehicleUseCase;
        this.mapper = mapper;
    }
    
    /**
     * POST /api/v1/vehicles - Cria um novo veículo
     */
    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        Vehicle vehicle = createVehicleUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(vehicle));
    }
    
    /**
     * GET /api/v1/vehicles/{id} - Busca veículo por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicle(@PathVariable UUID id) {
        Vehicle vehicle = getVehicleUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(vehicle));
    }
    
    /**
     * GET /api/v1/vehicles/driver/{driverId} - Lista veículos do motorista
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByDriver(@PathVariable UUID driverId) {
        List<Vehicle> vehicles = getVehicleUseCase.findByDriver(driverId);
        List<VehicleResponse> response = vehicles.stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/vehicles/driver/{driverId}/active - Busca veículo ativo do motorista
     */
    @GetMapping("/driver/{driverId}/active")
    public ResponseEntity<VehicleResponse> getActiveVehicleByDriver(@PathVariable UUID driverId) {
        return getVehicleUseCase.findActiveByDriver(driverId)
            .map(mapper::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * PUT /api/v1/vehicles/{id} - Atualiza veículo
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVehicleRequest request) {
        Vehicle vehicle = updateVehicleUseCase.execute(mapper.toCommand(id, request));
        return ResponseEntity.ok(mapper.toResponse(vehicle));
    }
    
    /**
     * PUT /api/v1/vehicles/{id}/assign - Associa veículo a motorista
     */
    @PutMapping("/{id}/assign")
    public ResponseEntity<VehicleResponse> assignVehicleToDriver(
            @PathVariable UUID id,
            @Valid @RequestBody AssignVehicleRequest request) {
        Vehicle vehicle = assignVehicleToDriverUseCase.execute(mapper.toCommand(id, request));
        return ResponseEntity.ok(mapper.toResponse(vehicle));
    }
    
    /**
     * PUT /api/v1/vehicles/{id}/activate - Ativa veículo
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<VehicleResponse> activateVehicle(@PathVariable UUID id) {
        Vehicle vehicle = activateVehicleUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(vehicle));
    }
}
