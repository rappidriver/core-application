package com.rappidrive.infrastructure.persistence.mappers;

import com.rappidrive.domain.valueobjects.AdminUser;
import com.rappidrive.infrastructure.persistence.entities.AdminUserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AdminUserMapper {

    public AdminUser toDomain(AdminUserJpaEntity entity) {
        return new AdminUser(
            entity.getId(),
            entity.getEmail(),
            entity.getRole(),
            entity.getFullName(),
            entity.getTenantId(),
            entity.getCreatedAt()
        );
    }
}
