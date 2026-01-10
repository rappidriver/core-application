package com.rappidrive.domain.entities;

import com.rappidrive.domain.enums.ApprovalStatus;
import com.rappidrive.domain.exceptions.InvalidApprovalStateException;
import com.rappidrive.domain.valueobjects.TenantId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DriverApproval Aggregate Tests")
class DriverApprovalTest {
    
    private UUID approvalId;
    private UUID driverId;
    private TenantId tenantId;
    private String validDocuments;
    private UUID adminId;
    
    @BeforeEach
    void setUp() {
        approvalId = UUID.randomUUID();
        driverId = UUID.randomUUID();
        tenantId = new TenantId(UUID.randomUUID());
        validDocuments = "[\"cnh_url\", \"comprovante_url\"]";
        adminId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("should create approval request with PENDING status")
    void shouldCreateWithPendingStatus() {
        // Act
        DriverApproval approval = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        
        // Assert
        assertEquals(approvalId, approval.id());
        assertEquals(driverId, approval.driverId());
        assertEquals(tenantId, approval.tenantId());
        assertEquals(ApprovalStatus.PENDING, approval.status());
        assertEquals(validDocuments, approval.submittedDocuments());
        assertTrue(approval.isPending());
        assertFalse(approval.isApproved());
        assertFalse(approval.isRejected());
        assertFalse(approval.isFinalized());
    }
    
    @Test
    @DisplayName("should fail when ID is null")
    void shouldFailWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> new DriverApproval(null, driverId, tenantId, validDocuments),
            "Approval request ID cannot be null"
        );
    }
    
    @Test
    @DisplayName("should fail when driver ID is null")
    void shouldFailWhenDriverIdIsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> new DriverApproval(approvalId, null, tenantId, validDocuments),
            "Driver ID cannot be null"
        );
    }
    
    @Test
    @DisplayName("should fail when tenant ID is null")
    void shouldFailWhenTenantIdIsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> new DriverApproval(approvalId, driverId, null, validDocuments),
            "Tenant ID cannot be null"
        );
    }
    
    @Test
    @DisplayName("should fail when documents are null")
    void shouldFailWhenDocumentsAreNull() {
        assertThrows(IllegalArgumentException.class,
            () -> new DriverApproval(approvalId, driverId, tenantId, null),
            "Submitted documents cannot be null or empty"
        );
    }
    
    @Test
    @DisplayName("should fail when documents are empty")
    void shouldFailWhenDocumentsAreEmpty() {
        assertThrows(IllegalArgumentException.class,
            () -> new DriverApproval(approvalId, driverId, tenantId, "   "),
            "Submitted documents cannot be null or empty"
        );
    }
    
    // Approve behavior tests
    
    @Test
    @DisplayName("should approve PENDING request and transition to APPROVED")
    void shouldApproveFromPending() {
        // Arrange
        DriverApproval approval = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        
        // Act
        approval.approve(adminId);
        
        // Assert
        assertEquals(ApprovalStatus.APPROVED, approval.status());
        assertTrue(approval.isApproved());
        assertFalse(approval.isPending());
        assertTrue(approval.isFinalized());
        assertEquals(adminId, approval.reviewedByAdminId());
        assertNotNull(approval.reviewedAt());
        assertNull(approval.rejectionReason());
    }
    
    @Test
    @DisplayName("should fail to approve when already APPROVED")
    void shouldFailToApproveWhenAlreadyApproved() {
        // Arrange
        DriverApproval approval = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        approval.approve(adminId);
        
        // Act & Assert
        InvalidApprovalStateException exception = assertThrows(InvalidApprovalStateException.class,
            () -> approval.approve(UUID.randomUUID()),
            "Should not allow approving an already approved request"
        );
        assertTrue(exception.getMessage().contains("Cannot approve request with status"));
    }
    
    @Test
    @DisplayName("should fail to approve when REJECTED")
    void shouldFailToApproveWhenRejected() {
        // Arrange
        DriverApproval approval = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        approval.reject(adminId, "Documents invalid");
        
        // Act & Assert
        InvalidApprovalStateException exception = assertThrows(InvalidApprovalStateException.class,
            () -> approval.approve(UUID.randomUUID()),
            "Should not allow approving a rejected request"
        );
        assertTrue(exception.getMessage().contains("Cannot approve request with status"));
    }
    
    @Test
    @DisplayName("should fail to approve with null admin ID")
    void shouldFailToApproveWithNullAdminId() {
        // Arrange
        DriverApproval approval = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> approval.approve(null),
            "Admin ID cannot be null"
        );
    }
    
    // Reject behavior tests
    
    @Test
    @DisplayName("should reject PENDING request and transition to REJECTED")
    void shouldRejectFromPending() {
        // Arrange
        DriverApproval approval = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        String reason = "Documentation invalid";
        
        // Act
        approval.reject(adminId, reason);
        
        // Assert
        assertEquals(ApprovalStatus.REJECTED, approval.status());
        assertTrue(approval.isRejected());
        assertFalse(approval.isPending());
        assertTrue(approval.isFinalized());
        assertEquals(adminId, approval.reviewedByAdminId());
        assertNotNull(approval.reviewedAt());
        assertEquals(reason, approval.rejectionReason());
    }
    
    @Test
    @DisplayName("should fail to reject when already REJECTED")
    void shouldFailToRejectWhenAlreadyRejected() {
        // Arrange
        DriverApproval approval = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        approval.reject(adminId, "Invalid docs");
        
        // Act & Assert
        InvalidApprovalStateException exception = assertThrows(InvalidApprovalStateException.class,
            () -> approval.reject(UUID.randomUUID(), "Another reason"),
            "Should not allow rejecting an already rejected request"
        );
        assertTrue(exception.getMessage().contains("Cannot reject request with status"));
    }
    
    @Test
    @DisplayName("should fail to reject when APPROVED")
    void shouldFailToRejectWhenApproved() {
        // Arrange
        DriverApproval approval = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        approval.approve(adminId);
        
        // Act & Assert
        InvalidApprovalStateException exception = assertThrows(InvalidApprovalStateException.class,
            () -> approval.reject(UUID.randomUUID(), "Some reason"),
            "Should not allow rejecting an approved request"
        );
        assertTrue(exception.getMessage().contains("Cannot reject request with status"));
    }
    
    @Test
    @DisplayName("should fail to reject with null admin ID")
    void shouldFailToRejectWithNullAdminId() {
        // Arrange
        DriverApproval approval = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> approval.reject(null, "Some reason"),
            "Admin ID cannot be null"
        );
    }
    
    @Test
    @DisplayName("should fail to reject with null reason")
    void shouldFailToRejectWithNullReason() {
        // Arrange
        DriverApproval approval = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> approval.reject(adminId, null),
            "Rejection reason cannot be null or empty"
        );
    }
    
    @Test
    @DisplayName("should fail to reject with empty reason")
    void shouldFailToRejectWithEmptyReason() {
        // Arrange
        DriverApproval approval = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> approval.reject(adminId, "   "),
            "Rejection reason cannot be null or empty"
        );
    }
    
    @Test
    @DisplayName("should trim rejection reason")
    void shouldTrimRejectionReason() {
        // Arrange
        DriverApproval approval = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        
        // Act
        approval.reject(adminId, "  Invalid documentation  ");
        
        // Assert
        assertEquals("Invalid documentation", approval.rejectionReason());
    }
    
    // Equality tests

    @Test
    @DisplayName("aggregates with same ID should be equal")
    void shouldBeEqualWithSameId() {
        DriverApproval approval1 = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        DriverApproval approval2 = new DriverApproval(
            approvalId, UUID.randomUUID(), tenantId, "[other docs]"
        );
        
        assertEquals(approval1, approval2);
        assertEquals(approval1.hashCode(), approval2.hashCode());
    }
    
    @Test
    @DisplayName("aggregates with different ID should not be equal")
    void shouldNotBeEqualWithDifferentId() {
        DriverApproval approval1 = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments
        );
        DriverApproval approval2 = new DriverApproval(
            UUID.randomUUID(), driverId, tenantId, validDocuments
        );
        
        assertNotEquals(approval1, approval2);
    }
    
    @Test
    @DisplayName("should reconstruct from persistence layer")
    void shouldReconstructFromPersistence() {
        // Arrange
        LocalDateTime submittedAt = LocalDateTime.now().minusHours(2);
        LocalDateTime reviewedAt = LocalDateTime.now().minusHours(1);
        
        // Act
        DriverApproval approval = new DriverApproval(
            approvalId, driverId, tenantId, validDocuments,
            ApprovalStatus.REJECTED, submittedAt, reviewedAt,
            adminId, "Invalid CNH"
        );
        
        // Assert
        assertEquals(approvalId, approval.id());
        assertEquals(ApprovalStatus.REJECTED, approval.status());
        assertEquals(submittedAt, approval.submittedAt());
        assertEquals(reviewedAt, approval.reviewedAt());
        assertEquals(adminId, approval.reviewedByAdminId());
        assertEquals("Invalid CNH", approval.rejectionReason());
        assertTrue(approval.isRejected());
    }
}
