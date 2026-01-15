package com.rappidrive.application.ports.output;

import com.rappidrive.domain.entities.Tenant;
import com.rappidrive.domain.valueobjects.TenantId;

import java.util.Optional;

public interface TenantRepositoryPort {
    Tenant save(Tenant tenant);
    boolean existsById(TenantId tenantId);
    Optional<Tenant> findById(TenantId tenantId);
    void deleteById(TenantId tenantId);
}