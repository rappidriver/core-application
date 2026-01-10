package com.rappidrive.application.ports.output;

import com.rappidrive.domain.enums.AdminRole;
import com.rappidrive.domain.valueobjects.AdminUser;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for admin user operations.
 */
public interface AdminUserRepositoryPort {

    Optional<AdminUser> findById(UUID id);

    Optional<AdminUser> findByEmail(String email);

    boolean hasRole(UUID adminId, AdminRole requiredRole);
}
