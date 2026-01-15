package com.rappidrive.presentation.controllers.admin;

import com.rappidrive.application.ports.input.tenant.OnboardNewTenantInputPort;
import com.rappidrive.presentation.dto.request.OnboardTenantRequest;
import com.rappidrive.presentation.dto.response.OnboardTenantResponse;
import com.rappidrive.presentation.mappers.OnboardTenantDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * SuperAdminController - Provides administrative endpoints for tenant management.
 * 
 * Security: All endpoints require ROLE_SUPER_ADMIN.
 * Only platform developers/owners should have this role.
 * 
 * Multi-tenancy: These operations are cross-tenant administrative functions.
 * They do not require X-Tenant-Id header (they are not scoped to a specific tenant).
 */
@RestController
@RequestMapping("/api/admin/tenants")
@Tag(name = "Super Admin - Tenant Management", description = "Administrative endpoints for tenant onboarding and management")
@SecurityRequirement(name = "bearer-jwt")
public class SuperAdminController {
    
    private static final Logger log = LoggerFactory.getLogger(SuperAdminController.class);
    
    private final OnboardNewTenantInputPort onboardTenantUseCase;
    private final OnboardTenantDtoMapper mapper;
    
    public SuperAdminController(OnboardNewTenantInputPort onboardTenantUseCase,
                               OnboardTenantDtoMapper mapper) {
        this.onboardTenantUseCase = onboardTenantUseCase;
        this.mapper = mapper;
    }
    
    /**
     * Onboard a new tenant into the RappiDrive platform.
     * 
     * This endpoint:
     * 1. Creates admin user in Keycloak
     * 2. Creates tenant group in Keycloak
     * 3. Saves initial fare configuration
     * 4. Saves service area (operating zone)
     * 5. Publishes event for notifications/email
     * 
     * Security: Requires ROLE_SUPER_ADMIN (platform owner only)
     * 
     * @param request Tenant onboarding details (validated)
     * @return OnboardTenantResponse with created IDs and temporary credentials
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Onboard a new tenant", 
            description = "Creates a new tenant (city) in the RappiDrive platform with all necessary infrastructure")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tenant created successfully",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = OnboardTenantResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ROLE_SUPER_ADMIN"),
            @ApiResponse(responseCode = "409", description = "Conflict - tenant already exists"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity - business rule validation failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<OnboardTenantResponse> onboardTenant(
            @Valid @RequestBody OnboardTenantRequest request) {
        
        log.info("SuperAdmin onboarding new tenant: {}", request.tenantId());
        
        try {
            // Convert request to use case command
            var command = mapper.toCommand(request);
            
            // Execute use case
            var result = onboardTenantUseCase.execute(command);
            
            // Convert result to response
            var response = mapper.toResponse(result);
            
            log.info("Tenant onboarded successfully: {}", request.tenantId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error onboarding tenant: {}", request.tenantId(), e);
            throw e;
        }
    }
    
    /**
     * Health check endpoint for admin operations.
     * Useful for verifying admin API is accessible.
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Health check for admin API")
    @ApiResponse(responseCode = "200", description = "Admin API is healthy")
    public ResponseEntity<java.util.Map<String, String>> health() {
        return ResponseEntity.ok(java.util.Map.of(
                "status", "healthy",
                "endpoint", "Admin API",
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}
