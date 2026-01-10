package com.rappidrive.domain.enums;

/**
 * Role of an admin user in the approval workflow.
 * 
 * Roles determine what operations an admin can perform:
 * - SUPER_ADMIN: Full access (approve, reject, analytics)
 * - COMPLIANCE_OFFICER: Can approve/reject drivers
 * - SUPPORT_ADMIN: Read-only access for support queries
 */
public enum AdminRole {
    /**
     * Full system access.
     */
    SUPER_ADMIN,
    
    /**
     * Can analyze documentation and approve/reject drivers.
     */
    COMPLIANCE_OFFICER,
    
    /**
     * Read-only access for support operations.
     */
    SUPPORT_ADMIN
}
