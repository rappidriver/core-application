package com.rappidrive.infrastructure.persistence.repositories;

import com.rappidrive.infrastructure.persistence.entities.AdminUserJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUserSpringDataRepository extends JpaRepository<AdminUserJpaEntity, UUID> {
    Optional<AdminUserJpaEntity> findByEmail(String email);
}
