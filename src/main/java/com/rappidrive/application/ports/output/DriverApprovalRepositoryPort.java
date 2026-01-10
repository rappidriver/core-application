package com.rappidrive.application.ports.output;

import com.rappidrive.domain.entities.DriverApproval;
import com.rappidrive.domain.valueobjects.TenantId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for driver approval persistence operations.
 */
public interface DriverApprovalRepositoryPort {

    DriverApproval save(DriverApproval request);

    Optional<DriverApproval> findById(UUID id);

    Optional<DriverApproval> findByDriverId(UUID driverId);

    PaginatedResult<DriverApproval> findPendingByTenant(TenantId tenantId,
        int pageNumber, int pageSize);

    long countPendingByTenant(TenantId tenantId);

    List<DriverApproval> findRejectedByDriver(UUID driverId);

    record PaginatedResult<T>(
        List<T> items,
        long totalCount,
        int pageNumber,
        int pageSize
    ) {
        public long getTotalPages() {
            return (totalCount + pageSize - 1) / pageSize;
        }
    }
}
