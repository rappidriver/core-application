package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.application.ports.output.AdminUserRepositoryPort;
import com.rappidrive.domain.enums.AdminRole;
import com.rappidrive.domain.valueobjects.AdminUser;
import com.rappidrive.infrastructure.persistence.mappers.AdminUserMapper;
import com.rappidrive.infrastructure.persistence.entities.AdminUserJpaEntity;
import com.rappidrive.infrastructure.persistence.repositories.AdminUserSpringDataRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaAdminUserRepositoryAdapter implements AdminUserRepositoryPort {

    private final AdminUserSpringDataRepository repository;
    private final AdminUserMapper mapper;

    public JpaAdminUserRepositoryAdapter(AdminUserSpringDataRepository repository, AdminUserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<AdminUser> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<AdminUser> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean hasRole(UUID adminId, AdminRole requiredRole) {
        return repository.findById(adminId)
            .map(AdminUserJpaEntity -> AdminUserJpaEntity.getRole() == requiredRole)
            .orElse(false);
    }
}
