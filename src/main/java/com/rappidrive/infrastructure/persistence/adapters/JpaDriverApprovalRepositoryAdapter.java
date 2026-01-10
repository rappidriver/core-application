package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.application.ports.output.DriverApprovalRepositoryPort;
import com.rappidrive.domain.entities.DriverApproval;
import com.rappidrive.domain.enums.ApprovalStatus;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.entities.DriverApprovalJpaEntity;
import com.rappidrive.infrastructure.persistence.mappers.DriverApprovalMapper;
import com.rappidrive.infrastructure.persistence.repositories.DriverApprovalSpringDataRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class JpaDriverApprovalRepositoryAdapter implements DriverApprovalRepositoryPort {

    private final DriverApprovalSpringDataRepository repository;
    private final DriverApprovalMapper mapper;

    public JpaDriverApprovalRepositoryAdapter(DriverApprovalSpringDataRepository repository,
                                              DriverApprovalMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public DriverApproval save(DriverApproval request) {
        DriverApprovalJpaEntity entity = mapper.toEntity(request);
        DriverApprovalJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<DriverApproval> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<DriverApproval> findByDriverId(UUID driverId) {
        return repository.findByDriverId(driverId).map(mapper::toDomain);
    }

    @Override
    public PaginatedResult<DriverApproval> findPendingByTenant(TenantId tenantId, int pageNumber, int pageSize) {
        var page = repository.findByTenantIdAndStatus(tenantId, ApprovalStatus.PENDING, PageRequest.of(pageNumber, pageSize));
        List<DriverApproval> approvals = page.getContent().stream().map(mapper::toDomain).toList();
        return new PaginatedResult<>(approvals, page.getTotalElements(), pageNumber, pageSize);
    }

    @Override
    public long countPendingByTenant(TenantId tenantId) {
        return repository.countByTenantIdAndStatus(tenantId, ApprovalStatus.PENDING);
    }

    @Override
    public List<DriverApproval> findRejectedByDriver(UUID driverId) {
        return repository.findByDriverIdAndStatus(driverId, ApprovalStatus.REJECTED)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
}
