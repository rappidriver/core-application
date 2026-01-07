package com.rappidrive.presentation.controllers;

import com.rappidrive.application.ports.input.CompleteTripWithPaymentInputPort;
import com.rappidrive.application.ports.input.GetTripWithPaymentDetailsInputPort;
import com.rappidrive.application.ports.input.trip.*;
import com.rappidrive.domain.entities.Trip;
import com.rappidrive.presentation.dto.request.AssignDriverToTripRequest;
import com.rappidrive.presentation.dto.request.CompleteTripWithPaymentRequest;
import com.rappidrive.presentation.dto.request.CreateTripRequest;
import com.rappidrive.presentation.dto.response.TripResponse;
import com.rappidrive.presentation.dto.response.TripWithPaymentDetailsResponse;
import com.rappidrive.presentation.mappers.TripDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for trip management.
 */
@Tag(name = "Trips", description = "Trip management endpoints")
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private static final Logger log = LoggerFactory.getLogger(TripController.class);
    
    private final CreateTripInputPort createTripUseCase;
    private final GetTripInputPort getTripUseCase;
    private final AssignDriverToTripInputPort assignDriverUseCase;
    private final StartTripInputPort startTripUseCase;
    private final CompleteTripInputPort completeTripUseCase;
    private final CompleteTripWithPaymentInputPort completeTripWithPaymentUseCase;
    private final GetTripWithPaymentDetailsInputPort getTripWithPaymentDetailsUseCase;
    private final TripDtoMapper mapper;
    
    @Operation(summary = "Create a new trip")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Trip created successfully",
            content = @Content(schema = @Schema(implementation = TripResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody CreateTripRequest request) {
        log.info("Creating trip: passengerId={}", request.passengerId());
        
        CreateTripInputPort.CreateTripCommand command = 
            new CreateTripInputPort.CreateTripCommand(
                mapper.toTenantId(request.tenantId()),
                request.passengerId(),
                mapper.toLocation(request.origin()),
                mapper.toLocation(request.destination())
            );
        
        Trip trip = createTripUseCase.execute(command);
        TripResponse response = mapper.toResponse(trip);
        
        log.info("Trip created successfully: id={}", trip.getId().getValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "Get trip by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trip found",
            content = @Content(schema = @Schema(implementation = TripResponse.class))),
        @ApiResponse(responseCode = "404", description = "Trip not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTrip(@PathVariable UUID id) {
        log.info("Fetching trip: id={}", id);
        
        Trip trip = getTripUseCase.execute(id);
        TripResponse response = mapper.toResponse(trip);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Assign a driver to a trip")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Driver assigned successfully",
            content = @Content(schema = @Schema(implementation = TripResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid trip state or driver not available"),
        @ApiResponse(responseCode = "404", description = "Trip or driver not found")
    })
    @PutMapping("/{id}/assign-driver")
    public ResponseEntity<TripResponse> assignDriver(
            @PathVariable UUID id,
            @Valid @RequestBody AssignDriverToTripRequest request) {
        log.info("Assigning driver to trip: tripId={}, driverId={}", id, request.driverId());
        
        AssignDriverToTripInputPort.AssignDriverCommand command = 
            new AssignDriverToTripInputPort.AssignDriverCommand(id, request.driverId());
        
        Trip trip = assignDriverUseCase.execute(command);
        TripResponse response = mapper.toResponse(trip);
        
        log.info("Driver assigned successfully: tripId={}, driverId={}", id, request.driverId());
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Start a trip")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trip started successfully",
            content = @Content(schema = @Schema(implementation = TripResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid trip state"),
        @ApiResponse(responseCode = "404", description = "Trip not found")
    })
    @PutMapping("/{id}/start")
    public ResponseEntity<TripResponse> startTrip(@PathVariable UUID id) {
        log.info("Starting trip: id={}", id);
        
        Trip trip = startTripUseCase.execute(id);
        TripResponse response = mapper.toResponse(trip);
        
        log.info("Trip started successfully: id={}", id);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Complete a trip")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trip completed successfully",
            content = @Content(schema = @Schema(implementation = TripResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid trip state"),
        @ApiResponse(responseCode = "404", description = "Trip not found")
    })
    @PutMapping("/{id}/complete")
    public ResponseEntity<TripResponse> completeTrip(@PathVariable UUID id) {
        log.info("Completing trip: id={}", id);
        
        Trip trip = completeTripUseCase.execute(id);
        TripResponse response = mapper.toResponse(trip);
        
        log.info("Trip completed successfully: id={}, actualFare={}", 
            id, trip.getActualFare().map(money -> money.getAmount()).orElse(null));
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Complete trip with automatic fare calculation and payment processing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trip completed and payment processed",
            content = @Content(schema = @Schema(implementation = TripWithPaymentDetailsResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid trip state or payment failed"),
        @ApiResponse(responseCode = "404", description = "Trip not found")
    })
    @PostMapping("/{id}/complete-with-payment")
    public ResponseEntity<TripWithPaymentDetailsResponse> completeTripWithPayment(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteTripWithPaymentRequest request) {
        log.info("Completing trip with payment: tripId={}", id);
        
        CompleteTripWithPaymentInputPort.CompleteTripWithPaymentCommand command = 
            mapper.toCommand(id, request);
        
        CompleteTripWithPaymentInputPort.TripCompletionResult result = 
            completeTripWithPaymentUseCase.execute(command);
        
        TripWithPaymentDetailsResponse response = mapper.toTripWithPaymentDetailsResponse(result);
        
        log.info("Trip completed with payment: tripId={}, paymentSuccessful={}, fareAmount={}", 
            id, result.paymentSuccessful(), result.fare().getTotalAmount());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get trip details with fare and payment information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trip details with payment info retrieved",
            content = @Content(schema = @Schema(implementation = TripWithPaymentDetailsResponse.class))),
        @ApiResponse(responseCode = "404", description = "Trip not found")
    })
    @GetMapping("/{id}/payment-details")
    public ResponseEntity<TripWithPaymentDetailsResponse> getTripWithPaymentDetails(@PathVariable UUID id) {
        log.info("Fetching trip with payment details: tripId={}", id);
        
        GetTripWithPaymentDetailsInputPort.TripWithPaymentDetails details = 
            getTripWithPaymentDetailsUseCase.execute(id);
        
        TripWithPaymentDetailsResponse response = mapper.toTripWithPaymentDetailsResponse(details);
        
        log.info("Trip details retrieved: tripId={}, hasFare={}, hasPayment={}", 
            id, details.hasFare(), details.hasPayment());
        
        return ResponseEntity.ok(response);
    }
}
