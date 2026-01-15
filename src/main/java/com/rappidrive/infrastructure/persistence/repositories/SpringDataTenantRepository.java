package com.rappidrive.infrastructure.persistence.repositories;

import com.rappidrive.infrastructure.persistence.entities.TenantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataTenantRepository extends JpaRepository<TenantJpaEntity, UUID> {
}
