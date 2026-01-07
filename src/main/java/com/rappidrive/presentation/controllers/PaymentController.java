package com.rappidrive.presentation.controllers;

import com.rappidrive.application.ports.input.payment.GetPaymentInputPort;
import com.rappidrive.application.ports.input.payment.ProcessPaymentInputPort;
import com.rappidrive.application.ports.input.payment.RefundPaymentInputPort;
import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.presentation.dto.request.ProcessPaymentRequest;
import com.rappidrive.presentation.dto.request.RefundPaymentRequest;
import com.rappidrive.presentation.dto.response.PaymentResponse;
import com.rappidrive.presentation.mappers.PaymentDtoMapper;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for payment management.
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    
    private final ProcessPaymentInputPort processPaymentUseCase;
    private final GetPaymentInputPort getPaymentUseCase;
    private final RefundPaymentInputPort refundPaymentUseCase;
    private final PaymentDtoMapper mapper;
    
    public PaymentController(ProcessPaymentInputPort processPaymentUseCase,
                            GetPaymentInputPort getPaymentUseCase,
                            RefundPaymentInputPort refundPaymentUseCase,
                            PaymentDtoMapper mapper) {
        this.processPaymentUseCase = processPaymentUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
        this.refundPaymentUseCase = refundPaymentUseCase;
        this.mapper = mapper;
    }
    
    /**
     * POST /api/v1/payments - Processes a payment
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        Payment payment = processPaymentUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(payment));
    }
    
    /**
     * GET /api/v1/payments/{id} - Gets payment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id) {
        Payment payment = getPaymentUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(payment));
    }
    
    /**
     * GET /api/v1/payments/trip/{tripId} - Gets payment by trip ID
     */
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<PaymentResponse> getPaymentByTrip(@PathVariable UUID tripId) {
        List<Payment> payments = getPaymentUseCase.findByTrip(tripId);
        if (payments.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toResponse(payments.get(0)));
    }
    
    /**
     * GET /api/v1/payments/tenant/{tenantId}/period - Lists payments by tenant and date range
     */
    @GetMapping("/tenant/{tenantId}/period")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByTenantAndPeriod(
            @PathVariable UUID tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Payment> payments = getPaymentUseCase.findByTenant(
            new TenantId(tenantId), startDate, endDate);
        List<PaymentResponse> response = payments.stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/v1/payments/refund - Refunds a payment
     */
    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@Valid @RequestBody RefundPaymentRequest request) {
        Payment payment = refundPaymentUseCase.execute(request.paymentId(), request.reason());
        return ResponseEntity.ok(mapper.toResponse(payment));
    }
}
