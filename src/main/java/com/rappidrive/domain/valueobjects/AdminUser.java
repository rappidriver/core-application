package com.rappidrive.domain.valueobjects;

import com.rappidrive.domain.enums.AdminRole;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable value object representing an admin user in the approval system.
 * 
 * An admin user has a specific role that determines their permissions
 * for approving/rejecting driver applications.
 */
public final class AdminUser {
    private final UUID id;
    private final Email email;
    private final AdminRole role;
    private final String fullName;
    private final TenantId tenantId;
    private final LocalDateTime createdAt;
    
    /**
     * Creates a new AdminUser instance.
     * 
     * @param id unique identifier for the admin
     * @param email admin's email address
     * @param role admin's role (determines permissions)
     * @param fullName admin's full name
     * @param tenantId tenant identifier for multi-tenancy isolation
     * @param createdAt timestamp when admin was created
     * @throws IllegalArgumentException if any parameter is null
     */
    public AdminUser(UUID id, Email email, AdminRole role, String fullName,
                     TenantId tenantId, LocalDateTime createdAt) {
        if (id == null) {
            throw new IllegalArgumentException("Admin ID cannot be null");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Created timestamp cannot be null");
        }
        
        this.id = id;
        this.email = email;
        this.role = role;
        this.fullName = fullName.trim();
        this.tenantId = tenantId;
        this.createdAt = createdAt;
    }
    
    // Accessors
    public UUID id() {
        return id;
    }
    
    public Email email() {
        return email;
    }
    
    public AdminRole role() {
        return role;
    }
    
    public String fullName() {
        return fullName;
    }

    public TenantId tenantId() {
        return tenantId;
    }
    
    public LocalDateTime createdAt() {
        return createdAt;
    }
    
    // Permission checks
    
    /**
     * Checks if this admin can approve drivers.
     * Only SUPER_ADMIN and COMPLIANCE_OFFICER can approve.
     * 
     * @return true if admin has approval permission
     */
    public boolean canApproveDrivers() {
        return role == AdminRole.SUPER_ADMIN || 
               role == AdminRole.COMPLIANCE_OFFICER;
    }
    
    /**
     * Checks if this admin can reject drivers.
     * Only SUPER_ADMIN and COMPLIANCE_OFFICER can reject.
     * 
     * @return true if admin has rejection permission
     */
    public boolean canRejectDrivers() {
        return role == AdminRole.SUPER_ADMIN || 
               role == AdminRole.COMPLIANCE_OFFICER;
    }
    
    /**
     * Checks if this admin has a specific role.
     * 
     * @param requiredRole the role to check
     * @return true if admin has the required role
     */
    public boolean hasRole(AdminRole requiredRole) {
        return this.role == requiredRole;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdminUser)) return false;
        AdminUser adminUser = (AdminUser) o;
        return Objects.equals(id, adminUser.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "AdminUser{" +
                "id=" + id +
                ", email=" + email +
                ", role=" + role +
            ", fullName='" + fullName + '\'' +
            ", tenantId=" + tenantId +
                '}';
    }
}
