package com.rappidrive.domain.valueobjects;

import com.rappidrive.domain.enums.AdminRole;
import com.rappidrive.domain.valueobjects.TenantId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminUser Value Object Tests")
class AdminUserTest {
    
    private final UUID adminId = UUID.randomUUID();
    private final Email validEmail = new Email("admin@rappidrive.com");
    private final String validFullName = "João Administrator";
    private final TenantId tenantId = new TenantId(UUID.randomUUID());
    private final LocalDateTime now = LocalDateTime.now();
    
    @Test
    @DisplayName("should create admin user with valid parameters")
    void shouldCreateValidAdminUser() {
        // Arrange & Act
        AdminUser admin = new AdminUser(adminId, validEmail, AdminRole.SUPER_ADMIN,
                           validFullName, tenantId, now);
        
        // Assert
        assertEquals(adminId, admin.id());
        assertEquals(validEmail, admin.email());
        assertEquals(AdminRole.SUPER_ADMIN, admin.role());
        assertEquals(validFullName, admin.fullName());
        assertEquals(now, admin.createdAt());
    }
    
    @Test
    @DisplayName("should fail when ID is null")
    void shouldFailWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> new AdminUser(null, validEmail, AdminRole.SUPER_ADMIN, validFullName, tenantId, now),
            "Admin ID cannot be null"
        );
    }
    
    @Test
    @DisplayName("should fail when email is null")
    void shouldFailWhenEmailIsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> new AdminUser(adminId, null, AdminRole.SUPER_ADMIN, validFullName, tenantId, now),
            "Email cannot be null"
        );
    }
    
    @Test
    @DisplayName("should fail when role is null")
    void shouldFailWhenRoleIsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> new AdminUser(adminId, validEmail, null, validFullName, tenantId, now),
            "Role cannot be null"
        );
    }
    
    @Test
    @DisplayName("should fail when full name is null")
    void shouldFailWhenFullNameIsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> new AdminUser(adminId, validEmail, AdminRole.SUPER_ADMIN, null, tenantId, now),
            "Full name cannot be null or empty"
        );
    }
    
    @Test
    @DisplayName("should fail when full name is empty")
    void shouldFailWhenFullNameIsEmpty() {
        assertThrows(IllegalArgumentException.class,
            () -> new AdminUser(adminId, validEmail, AdminRole.SUPER_ADMIN, "   ", tenantId, now),
            "Full name cannot be null or empty"
        );
    }
    
    @Test
    @DisplayName("should fail when created timestamp is null")
    void shouldFailWhenCreatedAtIsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> new AdminUser(adminId, validEmail, AdminRole.SUPER_ADMIN, validFullName, tenantId, null),
            "Created timestamp cannot be null"
        );
    }

    @Test
    @DisplayName("should fail when tenantId is null")
    void shouldFailWhenTenantIsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> new AdminUser(adminId, validEmail, AdminRole.SUPER_ADMIN, validFullName, null, now),
            "TenantId cannot be null"
        );
    }
    
    @Test
    @DisplayName("SUPER_ADMIN should be able to approve drivers")
    void superAdminCanApproveDrivers() {
        AdminUser admin = new AdminUser(adminId, validEmail, AdminRole.SUPER_ADMIN,
                           validFullName, tenantId, now);
        assertTrue(admin.canApproveDrivers());
    }
    
    @Test
    @DisplayName("COMPLIANCE_OFFICER should be able to approve drivers")
    void complianceOfficerCanApproveDrivers() {
        AdminUser admin = new AdminUser(adminId, validEmail, AdminRole.COMPLIANCE_OFFICER,
                           validFullName, tenantId, now);
        assertTrue(admin.canApproveDrivers());
    }
    
    @Test
    @DisplayName("SUPPORT_ADMIN should NOT be able to approve drivers")
    void supportAdminCannotApproveDrivers() {
        AdminUser admin = new AdminUser(adminId, validEmail, AdminRole.SUPPORT_ADMIN,
                           validFullName, tenantId, now);
        assertFalse(admin.canApproveDrivers());
    }
    
    @Test
    @DisplayName("SUPER_ADMIN should be able to reject drivers")
    void superAdminCanRejectDrivers() {
        AdminUser admin = new AdminUser(adminId, validEmail, AdminRole.SUPER_ADMIN,
                           validFullName, tenantId, now);
        assertTrue(admin.canRejectDrivers());
    }
    
    @Test
    @DisplayName("COMPLIANCE_OFFICER should be able to reject drivers")
    void complianceOfficerCanRejectDrivers() {
        AdminUser admin = new AdminUser(adminId, validEmail, AdminRole.COMPLIANCE_OFFICER,
                           validFullName, tenantId, now);
        assertTrue(admin.canRejectDrivers());
    }
    
    @Test
    @DisplayName("SUPPORT_ADMIN should NOT be able to reject drivers")
    void supportAdminCannotRejectDrivers() {
        AdminUser admin = new AdminUser(adminId, validEmail, AdminRole.SUPPORT_ADMIN,
                           validFullName, tenantId, now);
        assertFalse(admin.canRejectDrivers());
    }
    
    @Test
    @DisplayName("should check role equality")
    void shouldCheckRoleEquality() {
        AdminUser admin = new AdminUser(adminId, validEmail, AdminRole.SUPER_ADMIN,
                           validFullName, tenantId, now);
        assertTrue(admin.hasRole(AdminRole.SUPER_ADMIN));
        assertFalse(admin.hasRole(AdminRole.COMPLIANCE_OFFICER));
    }
    
    @Test
    @DisplayName("value objects with same ID should be equal")
    void shouldBeEqualWithSameId() {
        AdminUser admin1 = new AdminUser(adminId, validEmail, AdminRole.SUPER_ADMIN,
                        validFullName, tenantId, now);
        AdminUser admin2 = new AdminUser(adminId, new Email("different@rappidrive.com"),
                        AdminRole.SUPPORT_ADMIN, "Different Name", tenantId, now);
        
        assertEquals(admin1, admin2);
        assertEquals(admin1.hashCode(), admin2.hashCode());
    }
    
    @Test
    @DisplayName("value objects with different ID should not be equal")
    void shouldNotBeEqualWithDifferentId() {
        AdminUser admin1 = new AdminUser(adminId, validEmail, AdminRole.SUPER_ADMIN,
                        validFullName, tenantId, now);
        AdminUser admin2 = new AdminUser(UUID.randomUUID(), validEmail, AdminRole.SUPER_ADMIN,
                        validFullName, tenantId, now);
        
        assertNotEquals(admin1, admin2);
    }
    
    @Test
    @DisplayName("full name should be trimmed")
    void shouldTrimFullName() {
        AdminUser admin = new AdminUser(adminId, validEmail, AdminRole.SUPER_ADMIN,
                           "  João Administrator  ", tenantId, now);
        assertEquals("João Administrator", admin.fullName());
    }
}
