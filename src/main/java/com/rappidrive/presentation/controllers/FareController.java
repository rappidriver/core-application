package com.rappidrive.presentation.controllers;

import com.rappidrive.application.ports.input.payment.CalculateFareInputPort;
import com.rappidrive.domain.entities.Fare;
import com.rappidrive.presentation.dto.request.CalculateFareRequest;
import com.rappidrive.presentation.dto.response.FareResponse;
import com.rappidrive.presentation.mappers.PaymentDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fares")
public class FareController {
    
    private final CalculateFareInputPort calculateFareUseCase;
    private final PaymentDtoMapper mapper;
    
    public FareController(CalculateFareInputPort calculateFareUseCase,
                         PaymentDtoMapper mapper) {
        this.calculateFareUseCase = calculateFareUseCase;
        this.mapper = mapper;
    }
    
    @PostMapping("/calculate")
    public ResponseEntity<FareResponse> calculateFare(@Valid @RequestBody CalculateFareRequest request) {
        Fare fare = calculateFareUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.ok(mapper.toResponse(fare));
    }
}
