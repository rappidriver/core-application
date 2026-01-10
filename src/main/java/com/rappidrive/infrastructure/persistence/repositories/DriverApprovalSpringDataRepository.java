package com.rappidrive.infrastructure.persistence.repositories;

import com.rappidrive.infrastructure.persistence.entities.DriverApprovalJpaEntity;
import com.rappidrive.domain.enums.ApprovalStatus;
import com.rappidrive.domain.valueobjects.TenantId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverApprovalSpringDataRepository extends JpaRepository<DriverApprovalJpaEntity, UUID> {

    Optional<DriverApprovalJpaEntity> findByDriverId(UUID driverId);

    Page<DriverApprovalJpaEntity> findByTenantIdAndStatus(TenantId tenantId, ApprovalStatus status, Pageable pageable);

    long countByTenantIdAndStatus(TenantId tenantId, ApprovalStatus status);

    List<DriverApprovalJpaEntity> findByDriverIdAndStatus(UUID driverId, ApprovalStatus status);
}
